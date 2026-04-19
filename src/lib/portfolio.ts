import { getDb, generateId, bufferToHex } from "./db";
import type {
  PortfolioData,
  PortfolioBasics,
  PortfolioWork,
  PortfolioProject,
  PortfolioSkill,
  PortfolioEducation,
  LanguageOption,
} from "./types";

const LABEL_PREFIX = "[[label:";
const LABEL_SUFFIX = "]]";

function decodeLabelMetadata(rawSummary: string): { label: string; summary: string } {
  const value = String(rawSummary || "");
  if (!value.startsWith(LABEL_PREFIX)) {
    return { label: "", summary: value };
  }

  const suffixIndex = value.indexOf(LABEL_SUFFIX);
  if (suffixIndex === -1) {
    return { label: "", summary: value };
  }

  const encodedLabel = value.slice(LABEL_PREFIX.length, suffixIndex);
  const summary = value.slice(suffixIndex + LABEL_SUFFIX.length).replace(/^\n/, "");

  try {
    return {
      label: Buffer.from(encodedLabel, "base64").toString("utf-8"),
      summary,
    };
  } catch {
    return { label: "", summary: value };
  }
}

export async function getAvailableLanguages(): Promise<LanguageOption[]> {
  const db = getDb();
  const result = await db.execute(
    "SELECT code, name FROM languages WHERE enabled = true ORDER BY is_default DESC, code"
  );
  return result.rows.map((r) => ({
    code: r.code as string,
    name: r.name as string,
  }));
}

export async function getDefaultLanguageCode(): Promise<string> {
  const db = getDb();
  const result = await db.execute(
    "SELECT code FROM languages WHERE is_default = true LIMIT 1"
  );
  if (result.rows.length > 0) return result.rows[0].code as string;

  const fallback = await db.execute(
    "SELECT code FROM languages WHERE enabled = true ORDER BY code LIMIT 1"
  );
  return fallback.rows.length > 0 ? (fallback.rows[0].code as string) : "es";
}

export async function resolveLanguageCode(langParam: string | null): Promise<string> {
  if (langParam) {
    const db = getDb();
    const result = await db.execute({
      sql: "SELECT code FROM languages WHERE code = ? AND enabled = true",
      args: [langParam],
    });
    if (result.rows.length > 0) return result.rows[0].code as string;
  }
  return getDefaultLanguageCode();
}

export async function getPortfolioData(languageCode: string): Promise<PortfolioData> {
  const db = getDb();

  const lang = await db.execute({
    sql: "SELECT id FROM languages WHERE code = ?",
    args: [languageCode],
  });

  if (lang.rows.length === 0) {
    return { basics: null, aboutMe: "", work: [], projects: [], skills: [], education: [] };
  }

  const langId = lang.rows[0].id;

  const aboutMeResult = await db.execute({
    sql: "SELECT summary FROM aboutme WHERE language_id = ? LIMIT 1",
    args: [langId],
  });
  let aboutMe = aboutMeResult.rows.length > 0 ? (aboutMeResult.rows[0].summary as string) : "";

  // Basics
  const basicsResult = await db.execute({
    sql: "SELECT * FROM basics WHERE language_id = ?",
    args: [langId],
  });

  let basics: PortfolioBasics | null = null;
  if (basicsResult.rows.length > 0) {
    const b = basicsResult.rows[0];
    const profiles = [] as PortfolioBasics["profiles"];
    if (b.linkedin) {
      profiles.push({ network: "LinkedIn", url: b.linkedin as string });
    }
    if (b.github) {
      profiles.push({ network: "GitHub", url: b.github as string });
    }

    let label = b.summary as string;
    if (!aboutMe) {
      const decodedBasics = decodeLabelMetadata(b.summary as string);
      if (decodedBasics.label) {
        label = decodedBasics.label;
        aboutMe = decodedBasics.summary;
      } else {
        // New model: basics.summary is only hero short text.
        // If aboutme row does not exist, About section must remain empty.
        label = b.summary as string;
      }
    }

    basics = {
      name: b.name as string,
      label,
      image: b.image as string,
      email: b.email as string,
      status: b.status as string,
      profiles,
    };
  }

  // Work
  const workResult = await db.execute({
    sql: "SELECT * FROM works WHERE language_id = ?",
    args: [langId],
  });

  const work: PortfolioWork[] = [];
  for (const w of workResult.rows) {
    const highlightsResult = await db.execute({
      sql: "SELECT highlight FROM work_highlights WHERE work_id = ?",
      args: [w.id],
    });
    work.push({
      name: w.name as string,
      position: w.position as string,
      url: (w.url as string) || null,
      startDate: w.start_date as string,
      endDate: (w.end_date as string) || null,
      summary: w.summary as string,
      highlights: highlightsResult.rows.map((h) => h.highlight as string),
    });
  }

  // Projects
  const projectsResult = await db.execute({
    sql: "SELECT * FROM projects WHERE language_id = ?",
    args: [langId],
  });

  const projects: PortfolioProject[] = [];
  for (const p of projectsResult.rows) {
    const highlightsResult = await db.execute({
      sql: "SELECT highlight FROM project_highlights WHERE project_id = ?",
      args: [p.id],
    });
    projects.push({
      name: p.name as string,
      description: p.summary as string,
      url: (p.url as string) || null,
      highlights: highlightsResult.rows.map((h) => h.highlight as string),
    });
  }

  // Skills
  const skillsResult = await db.execute({
    sql: "SELECT name FROM skills WHERE language_id = ? ORDER BY name",
    args: [langId],
  });
  const skills: PortfolioSkill[] = skillsResult.rows.map((s) => ({
    name: s.name as string,
  }));

  // Education
  const educationResult = await db.execute({
    sql: "SELECT * FROM education WHERE language_id = ?",
    args: [langId],
  });

  const education: PortfolioEducation[] = [];
  for (const e of educationResult.rows) {
    const coursesResult = await db.execute({
      sql: "SELECT highlight FROM education_highlights WHERE education_id = ?",
      args: [e.id],
    });
    education.push({
      institution: e.institution as string,
      area: e.area as string,
      url: (e.url as string) || null,
      startDate: e.start_date as string,
      endDate: (e.end_date as string) || null,
      courses: coursesResult.rows.map((c) => c.highlight as string),
    });
  }

  return { basics, aboutMe, work, projects, skills, education };
}

export async function savePortfolioData(
  languageCode: string,
  data: PortfolioData
): Promise<void> {
  const db = getDb();

  const lang = await db.execute({
    sql: "SELECT id FROM languages WHERE code = ?",
    args: [languageCode],
  });
  if (lang.rows.length === 0) throw new Error("Language not found");

  const langId = lang.rows[0].id;

  // Delete existing data for this language (in correct order for FK constraints)
  const existingBasics = await db.execute({
    sql: "SELECT id FROM basics WHERE language_id = ?",
    args: [langId],
  });

  const existingWork = await db.execute({
    sql: "SELECT id FROM works WHERE language_id = ?",
    args: [langId],
  });
  for (const w of existingWork.rows) {
    await db.execute({
      sql: "DELETE FROM work_highlights WHERE work_id = ?",
      args: [w.id],
    });
  }

  const existingProjects = await db.execute({
    sql: "SELECT id FROM projects WHERE language_id = ?",
    args: [langId],
  });
  for (const p of existingProjects.rows) {
    await db.execute({
      sql: "DELETE FROM project_highlights WHERE project_id = ?",
      args: [p.id],
    });
  }

  const existingEducation = await db.execute({
    sql: "SELECT id FROM education WHERE language_id = ?",
    args: [langId],
  });
  for (const e of existingEducation.rows) {
    await db.execute({
      sql: "DELETE FROM education_highlights WHERE education_id = ?",
      args: [e.id],
    });
  }

  await db.execute({ sql: "DELETE FROM aboutme WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM basics WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM works WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM projects WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM skills WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM education WHERE language_id = ?", args: [langId] });

  // Insert new data
  if (data.basics) {
    const basics = {
      name: String(data.basics.name || ""),
      label: String(data.basics.label || ""),
      image: String(data.basics.image || ""),
      email: String(data.basics.email || ""),
      summary: String(data.basics.label || ""),
      status: String(data.basics.status || ""),
    };
    const linkedIn = data.basics.profiles.find((profile) => profile.network === "LinkedIn")?.url || null;
    const github = data.basics.profiles.find((profile) => profile.network === "GitHub")?.url || null;

    const basicsId = generateId();
    await db.execute({
      sql: `INSERT INTO basics (id, name, image, summary, status, email, linkedin, github, language_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      args: [basicsId, basics.name, basics.image, basics.summary,
             basics.status, basics.email, linkedIn, github, langId],
    });
  }

  if (data.aboutMe && String(data.aboutMe).trim()) {
    await db.execute({
      sql: `INSERT INTO aboutme (id, summary, language_id)
            VALUES (?, ?, ?)`,
      args: [generateId(), String(data.aboutMe), langId],
    });
  }

  for (let i = 0; i < data.work.length; i++) {
    const w = data.work[i];
    const workId = generateId();
    await db.execute({
      sql: `INSERT INTO works (id, name, url, position, summary, start_date, end_date, language_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      args: [workId, w.name, w.url, w.position, w.summary, w.startDate, w.endDate, langId],
    });
    for (const highlight of w.highlights || []) {
      await db.execute({
        sql: "INSERT INTO work_highlights (work_id, highlight) VALUES (?, ?)",
        args: [workId, highlight],
      });
    }
  }

  for (let i = 0; i < data.projects.length; i++) {
    const p = data.projects[i];
    const projectId = generateId();
    await db.execute({
      sql: `INSERT INTO projects (id, name, url, summary, language_id)
            VALUES (?, ?, ?, ?, ?)`,
      args: [projectId, p.name, p.url, p.description, langId],
    });
    for (const highlight of p.highlights || []) {
      await db.execute({
        sql: "INSERT INTO project_highlights (project_id, highlight) VALUES (?, ?)",
        args: [projectId, highlight],
      });
    }
  }

  for (let i = 0; i < data.skills.length; i++) {
    await db.execute({
      sql: `INSERT INTO skills (id, name, language_id)
            VALUES (?, ?, ?)`,
      args: [generateId(), data.skills[i].name, langId],
    });
  }

  for (let i = 0; i < data.education.length; i++) {
    const e = data.education[i];
    const educationId = generateId();
    await db.execute({
      sql: `INSERT INTO education (id, institution, url, area, start_date, end_date, language_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)`,
      args: [educationId, e.institution, e.url, e.area, e.startDate, e.endDate, langId],
    });
    for (const course of e.courses || []) {
      await db.execute({
        sql: "INSERT INTO education_highlights (education_id, highlight) VALUES (?, ?)",
        args: [educationId, course],
      });
    }
  }
}

export async function seedPortfolioFromJson(): Promise<void> {
  const db = getDb();

  // Check if portfolio data already exists
  const existingBasics = await db.execute("SELECT COUNT(*) as cnt FROM basics");
  if ((existingBasics.rows[0].cnt as number) > 0) return;

  // Get default language
  const langResult = await db.execute(
    "SELECT id, code FROM languages WHERE is_default = true LIMIT 1"
  );
  if (langResult.rows.length === 0) return;

  const defaultLangCode = langResult.rows[0].code as string;

  // Read cv.json
  const { readFileSync } = await import("node:fs");
  const { join, dirname } = await import("node:path");
  const { fileURLToPath } = await import("node:url");

  let cvJson: any;
  try {
    const currentDir = dirname(fileURLToPath(import.meta.url));
    const cvPath = join(currentDir, "..", "..", "public", "cv.json");
    cvJson = JSON.parse(readFileSync(cvPath, "utf-8"));
  } catch {
    return; // No cv.json available, skip seeding
  }

  const portfolioData: PortfolioData = {
    basics: cvJson.basics
      ? {
          name: cvJson.basics.name || "",
          label: cvJson.basics.label || "",
          image: cvJson.basics.image || "",
          email: cvJson.basics.email || "",
          status: cvJson.basics.status || "",
          profiles: (cvJson.basics.profiles || []).map((p: any) => ({
            network: p.network,
            url: p.url,
          })),
        }
      : null,
    aboutMe: cvJson.basics?.summary || "",
    work: (cvJson.work || []).map((w: any) => ({
      name: w.name,
      position: w.position,
      url: w.url || null,
      startDate: w.startDate,
      endDate: w.endDate || null,
      summary: w.summary,
      highlights: w.highlights || [],
    })),
    projects: (cvJson.projects || []).map((p: any) => ({
      name: p.name,
      description: p.description,
      url: p.url || null,
      highlights: p.highlights || [],
    })),
    skills: (cvJson.skills || []).map((s: any) => ({
      name: s.name,
    })),
    education: (cvJson.education || []).map((e: any) => ({
      institution: e.institution,
      area: e.area,
      url: e.url || null,
      startDate: e.startDate,
      endDate: e.endDate || null,
      courses: e.courses || [],
    })),
  };

  await savePortfolioData(defaultLangCode, portfolioData);
}
