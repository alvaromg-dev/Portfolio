import { getDb, generateId, bufferToHex, toDbId } from "./db";
import type { LanguageRow } from "./types";

const CODE_PATTERN = /^[a-z]{2,5}$/;

function asBoolean(value: unknown): boolean {
  return value === true || value === 1 || value === "1";
}

export async function getLanguages(): Promise<LanguageRow[]> {
  const db = getDb();

  const result = await db.execute(
    "SELECT id, code, name, enabled, is_default FROM languages ORDER BY is_default DESC, code"
  );

  const languages: LanguageRow[] = [];
  for (const row of result.rows) {
    const [basicsCount, aboutMeCount] = await Promise.all([
      db.execute({
        sql: "SELECT COUNT(*) as cnt FROM basics WHERE language_id = ?",
        args: [row.id],
      }),
      db.execute({
        sql: "SELECT COUNT(*) as cnt FROM aboutme WHERE language_id = ?",
        args: [row.id],
      }),
    ]);
    const hasData =
      Number(basicsCount.rows[0].cnt || 0) > 0 ||
      Number(aboutMeCount.rows[0].cnt || 0) > 0;

    languages.push({
      id: bufferToHex(row.id as ArrayBuffer),
      code: row.code as string,
      name: row.name as string,
      enabled: asBoolean(row.enabled),
      isDefault: asBoolean(row.is_default),
      hasData,
    });
  }

  return languages;
}

export async function createLanguage(
  code: string,
  name: string
): Promise<{ success: boolean; error?: string }> {
  if (!code || !CODE_PATTERN.test(code)) {
    return { success: false, error: "Invalid code (2-5 lowercase letters)" };
  }

  if (!name || name.length > 30) {
    return { success: false, error: "Invalid name (max 30 characters)" };
  }

  const db = getDb();

  const existing = await db.execute({
    sql: "SELECT id FROM languages WHERE code = ?",
    args: [code],
  });
  if (existing.rows.length > 0) {
    return { success: false, error: "Language already exists" };
  }

  await db.execute({
    sql: "INSERT INTO languages (id, code, name, is_default, enabled) VALUES (?, ?, ?, false, true)",
    args: [generateId(), code, name],
  });

  return { success: true };
}

export async function toggleLanguageEnabled(
  languageId: string
): Promise<{ success: boolean; error?: string }> {
  const db = getDb();
  const langId = toDbId(languageId);

  const lang = await db.execute({
    sql: "SELECT is_default, enabled FROM languages WHERE id = ?",
    args: [langId],
  });

  if (lang.rows.length === 0) {
    return { success: false, error: "Language not found" };
  }

  if (asBoolean(lang.rows[0].is_default)) {
    return { success: false, error: "Cannot disable the default language" };
  }

  const newEnabled = !asBoolean(lang.rows[0].enabled);
  await db.execute({
    sql: "UPDATE languages SET enabled = ? WHERE id = ?",
    args: [newEnabled, langId],
  });

  return { success: true };
}

export async function setDefaultLanguage(
  languageId: string
): Promise<{ success: boolean; error?: string }> {
  const db = getDb();
  const langId = toDbId(languageId);

  const lang = await db.execute({
    sql: "SELECT enabled FROM languages WHERE id = ?",
    args: [langId],
  });

  if (lang.rows.length === 0) {
    return { success: false, error: "Language not found" };
  }

  if (!asBoolean(lang.rows[0].enabled)) {
    return { success: false, error: "Language must be enabled to be set as default" };
  }

  await db.execute("UPDATE languages SET is_default = false");
  await db.execute({
    sql: "UPDATE languages SET is_default = true WHERE id = ?",
    args: [langId],
  });

  return { success: true };
}

export async function deleteLanguage(
  languageId: string
): Promise<{ success: boolean; error?: string }> {
  const db = getDb();
  const langId = toDbId(languageId);

  const lang = await db.execute({
    sql: "SELECT is_default FROM languages WHERE id = ?",
    args: [langId],
  });

  if (lang.rows.length === 0) {
    return { success: false, error: "Language not found" };
  }

  if (asBoolean(lang.rows[0].is_default)) {
    return { success: false, error: "Cannot delete the default language" };
  }

  // Delete portfolio data for this language
  const work = await db.execute({
    sql: "SELECT id FROM works WHERE language_id = ?",
    args: [langId],
  });
  for (const w of work.rows) {
    await db.execute({ sql: "DELETE FROM work_highlights WHERE work_id = ?", args: [w.id] });
  }

  const projects = await db.execute({
    sql: "SELECT id FROM projects WHERE language_id = ?",
    args: [langId],
  });
  for (const p of projects.rows) {
    await db.execute({ sql: "DELETE FROM project_highlights WHERE project_id = ?", args: [p.id] });
  }

  const education = await db.execute({
    sql: "SELECT id FROM education WHERE language_id = ?",
    args: [langId],
  });
  for (const e of education.rows) {
    await db.execute({ sql: "DELETE FROM education_highlights WHERE education_id = ?", args: [e.id] });
  }

  await db.execute({ sql: "DELETE FROM aboutme WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM basics WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM works WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM projects WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM skills WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM education WHERE language_id = ?", args: [langId] });
  await db.execute({ sql: "DELETE FROM languages WHERE id = ?", args: [langId] });

  return { success: true };
}
