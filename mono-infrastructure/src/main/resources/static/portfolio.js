document.addEventListener("DOMContentLoaded", () => {
  trackVisitWithThrottle();
  setupAvatarSpin();
  setupEditor();
});

const VISIT_STORAGE_KEY = "portfolio_last_visit_at";
const VISIT_THROTTLE_MS = 60 * 60 * 1000;

function trackVisitWithThrottle() {
  const now = Date.now();
  let lastVisitAt = 0;

  try {
    const raw = window.localStorage.getItem(VISIT_STORAGE_KEY);
    lastVisitAt = raw ? Number(raw) : 0;
  } catch (error) {
    lastVisitAt = 0;
  }

  if (Number.isFinite(lastVisitAt) && lastVisitAt > 0 && now - lastVisitAt < VISIT_THROTTLE_MS) {
    return;
  }

  const payload = {
    path: window.location.pathname || "/",
    query: (window.location.search || "").replace(/^\?/, ""),
    source: document.referrer || ""
  };

  fetch("/api/mono/telemetry/track", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload),
    keepalive: true
  })
    .then((response) => {
      if (!response.ok) {
        return;
      }
      try {
        window.localStorage.setItem(VISIT_STORAGE_KEY, String(now));
      } catch (error) {
        // Ignore storage failures.
      }
    })
    .catch(() => {
      // Ignore telemetry errors on client.
    });
}

function setupAvatarSpin() {
  const img = document.getElementById("me");
  if (!img) {
    return;
  }

  let timeoutId;
  img.addEventListener("click", () => {
    if (document.body.classList.contains("editing")) {
      return;
    }

    img.classList.remove("rotating");
    void img.offsetWidth;
    img.classList.add("rotating");

    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => {
      img.classList.remove("rotating");
    }, 1000);
  });
}

function setupEditor() {
  const toggleButton = document.getElementById("portfolio-edit-toggle");
  if (!toggleButton) {
    return;
  }

  const body = document.body;
  const lang = (body.dataset.lang || "es").toLowerCase();
  const editLabel = body.dataset.editLabel || (lang === "en" ? "Edit" : "Editar");
  const saveLabel = body.dataset.saveLabel || (lang === "en" ? "Save" : "Guardar");
  const addLabel = body.dataset.addLabel || (lang === "en" ? "Add" : "Añadir");

  const state = {
    editing: false,
    work: readWorkItemsFromView(),
    projects: readProjectItemsFromView(),
    skills: readSkillItemsFromView(),
    education: readEducationItemsFromView()
  };

  setupImagePicker(lang);
  attachAddButtons(state, addLabel);
  attachRemoveHandlers(state, addLabel);

  toggleButton.addEventListener("click", async () => {
    if (!state.editing) {
      state.editing = true;
      body.classList.add("editing");
      toggleButton.textContent = saveLabel;
      return;
    }

    toggleButton.disabled = true;
    try {
      const payload = buildPayload();
      const response = await fetch(`/api/mono/portfolio?lang=${encodeURIComponent(lang)}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorBody = await response.text();
        throw new Error(`Save failed with status ${response.status}: ${errorBody}`);
      }

      window.location.href = `/?lang=${encodeURIComponent(lang)}`;
    } catch (error) {
      console.error(error);
      alert(lang === "en" ? "Could not save changes." : "No se pudieron guardar los cambios.");
    } finally {
      toggleButton.disabled = false;
      if (!state.editing) {
        toggleButton.textContent = editLabel;
      } else {
        toggleButton.textContent = saveLabel;
      }
    }
  });

  function buildPayload() {
    const basics = {
      name: valueOf("editor-name"),
      label: valueOf("editor-label"),
      image: valueOf("editor-image"),
      email: valueOf("editor-email"),
      summary: valueOf("editor-summary"),
      status: valueOf("editor-status"),
      profiles: []
    };

    const linkedin = valueOf("editor-linkedin");
    const github = valueOf("editor-github");

    if (linkedin) {
      basics.profiles.push({ network: "LinkedIn", url: linkedin });
    }
    if (github) {
      basics.profiles.push({ network: "GitHub", url: github });
    }

    return {
      basics,
      work: readWorkItemsFromEditor(),
      projects: readProjectItemsFromEditor(),
      skills: readSkillItemsFromEditor(),
      education: readEducationItemsFromEditor()
    };
  }

  function attachAddButtons(editorState, buttonLabel) {
    const workEditor = document.getElementById("work-editor");
    const projectsEditor = document.getElementById("projects-editor");
    const skillsEditor = document.getElementById("skills-editor");
    const educationEditor = document.getElementById("education-editor");

    setupSectionAdd(workEditor, () => {
      editorState.work = readWorkItemsFromEditor();
      editorState.work.push(blankWork());
      renderWorkEditor(editorState.work);
      attachRemoveHandlers(editorState, buttonLabel);
    }, buttonLabel);

    setupSectionAdd(projectsEditor, () => {
      editorState.projects = readProjectItemsFromEditor();
      editorState.projects.push(blankProject());
      renderProjectsEditor(editorState.projects);
      attachRemoveHandlers(editorState, buttonLabel);
    }, buttonLabel);

    setupSectionAdd(skillsEditor, () => {
      editorState.skills = readSkillItemsFromEditor();
      editorState.skills.push(blankSkill());
      renderSkillsEditor(editorState.skills);
      attachRemoveHandlers(editorState, buttonLabel);
    }, buttonLabel);

    setupSectionAdd(educationEditor, () => {
      editorState.education = readEducationItemsFromEditor();
      editorState.education.push(blankEducation());
      renderEducationEditor(editorState.education);
      attachRemoveHandlers(editorState, buttonLabel);
    }, buttonLabel);

    renderWorkEditor(editorState.work);
    renderProjectsEditor(editorState.projects);
    renderSkillsEditor(editorState.skills);
    renderEducationEditor(editorState.education);
  }

  function attachRemoveHandlers(editorState) {
    document.querySelectorAll("[data-remove-work]").forEach((button) => {
      button.onclick = () => {
        const index = Number(button.dataset.removeWork);
        editorState.work = readWorkItemsFromEditor();
        editorState.work.splice(index, 1);
        renderWorkEditor(editorState.work);
        attachRemoveHandlers(editorState);
      };
    });

    document.querySelectorAll("[data-remove-project]").forEach((button) => {
      button.onclick = () => {
        const index = Number(button.dataset.removeProject);
        editorState.projects = readProjectItemsFromEditor();
        editorState.projects.splice(index, 1);
        renderProjectsEditor(editorState.projects);
        attachRemoveHandlers(editorState);
      };
    });

    document.querySelectorAll("[data-remove-skill]").forEach((button) => {
      button.onclick = () => {
        const index = Number(button.dataset.removeSkill);
        editorState.skills = readSkillItemsFromEditor();
        editorState.skills.splice(index, 1);
        renderSkillsEditor(editorState.skills);
        attachRemoveHandlers(editorState);
      };
    });

    document.querySelectorAll("[data-remove-education]").forEach((button) => {
      button.onclick = () => {
        const index = Number(button.dataset.removeEducation);
        editorState.education = readEducationItemsFromEditor();
        editorState.education.splice(index, 1);
        renderEducationEditor(editorState.education);
        attachRemoveHandlers(editorState);
      };
    });
  }

  function setupSectionAdd(container, onAdd, buttonLabel) {
    if (!container) {
      return;
    }
    const addButton = container.querySelector(".editor-add");
    if (!addButton) {
      return;
    }
    addButton.title = buttonLabel;
    addButton.ariaLabel = buttonLabel;
    addButton.onclick = onAdd;
  }
}

function valueOf(id) {
  const element = document.getElementById(id);
  if (!element) {
    return "";
  }
  return element.value ? element.value.trim() : "";
}

function setupImagePicker(lang) {
  const pickerButton = document.getElementById("editor-image-picker");
  const fileInput = document.getElementById("editor-image-file");
  const hiddenImageInput = document.getElementById("editor-image");
  const previewImage = document.getElementById("me");

  if (!pickerButton || !fileInput || !hiddenImageInput || !previewImage) {
    return;
  }

  const openPicker = () => fileInput.click();

  pickerButton.addEventListener("click", openPicker);
  previewImage.addEventListener("click", () => {
    if (!document.body.classList.contains("editing")) {
      return;
    }
    openPicker();
  });

  fileInput.addEventListener("change", async () => {
    const file = fileInput.files && fileInput.files[0];
    if (!file) {
      return;
    }

    const isJpegType = file.type === "image/jpeg" || file.type === "image/jpg";
    const isJpegName = /\.(jpe?g)$/i.test(file.name || "");
    if (!isJpegType && !isJpegName) {
      alert(lang === "en" ? "Select a JPG/JPEG image." : "Selecciona una imagen JPG/JPEG.");
      fileInput.value = "";
      return;
    }

    try {
      const base64 = await fileToDataUrl(file);
      const dimensions = await readImageDimensions(base64);
      if (dimensions.width !== dimensions.height) {
        alert(lang === "en" ? "Image must be square (1:1)." : "La imagen debe ser cuadrada (1:1).");
        fileInput.value = "";
        return;
      }

      hiddenImageInput.value = base64;
      previewImage.src = base64;
    } catch (error) {
      console.error(error);
      alert(lang === "en" ? "Could not process image." : "No se pudo procesar la imagen.");
    } finally {
      fileInput.value = "";
    }
  });
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result || ""));
    reader.onerror = () => reject(new Error("file_read_error"));
    reader.readAsDataURL(file);
  });
}

function readImageDimensions(dataUrl) {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => resolve({ width: img.naturalWidth, height: img.naturalHeight });
    img.onerror = () => reject(new Error("image_decode_error"));
    img.src = dataUrl;
  });
}

function readWorkItemsFromView() {
  return Array.from(document.querySelectorAll("#work-list-view > li")).map((item) => ({
    name: item.dataset.name || "",
    position: item.dataset.position || "",
    url: item.dataset.url || "",
    startDate: item.dataset.startDate || "",
    endDate: item.dataset.endDate || "",
    summary: item.dataset.summary || "",
    highlights: splitList(item.dataset.highlights)
  }));
}

function readProjectItemsFromView() {
  return Array.from(document.querySelectorAll("#project-list-view > li")).map((item) => ({
    name: item.dataset.name || "",
    url: item.dataset.url || "",
    description: item.dataset.description || "",
    highlights: splitList(item.dataset.highlights)
  }));
}

function readSkillItemsFromView() {
  return Array.from(document.querySelectorAll("#skills-list-view > li")).map((item) => ({
    name: item.dataset.name || ""
  }));
}

function readEducationItemsFromView() {
  return Array.from(document.querySelectorAll("#education-list-view > li")).map((item) => ({
    institution: item.dataset.institution || "",
    area: item.dataset.area || "",
    url: item.dataset.url || "",
    startDate: item.dataset.startDate || "",
    endDate: item.dataset.endDate || "",
    courses: splitList(item.dataset.courses)
  }));
}

function renderWorkEditor(items) {
  const container = document.querySelector("#work-editor .editor-items");
  if (!container) {
    return;
  }
  container.innerHTML = "";
  items.forEach((item, index) => {
    const node = document.createElement("div");
    node.className = "editor-item";
    node.innerHTML = `
      <button type="button" class="editor-remove" data-remove-work="${index}">×</button>
      <label class="editor-field"><span>Company</span><input type="text" data-field="name" value="${escapeHtml(item.name)}"></label>
      <label class="editor-field"><span>Position</span><input type="text" data-field="position" value="${escapeHtml(item.position)}"></label>
      <label class="editor-field"><span>Start Date</span><input type="text" data-field="startDate" value="${escapeHtml(item.startDate)}" placeholder="2026-01-01"></label>
      <label class="editor-field"><span>End Date</span><input type="text" data-field="endDate" value="${escapeHtml(item.endDate)}" placeholder="2026-12-31"></label>
      <label class="editor-field"><span>URL</span><input type="text" data-field="url" value="${escapeHtml(item.url)}"></label>
      <label class="editor-field editor-field-full"><span>Summary</span><textarea data-field="summary" rows="3">${escapeHtml(item.summary)}</textarea></label>
      <label class="editor-field editor-field-full"><span>Highlights (comma separated)</span><input type="text" data-field="highlights" value="${escapeHtml((item.highlights || []).join(", "))}"></label>
    `;
    container.appendChild(node);
  });
}

function renderProjectsEditor(items) {
  const container = document.querySelector("#projects-editor .editor-items");
  if (!container) {
    return;
  }
  container.innerHTML = "";
  items.forEach((item, index) => {
    const node = document.createElement("div");
    node.className = "editor-item";
    node.innerHTML = `
      <button type="button" class="editor-remove" data-remove-project="${index}">×</button>
      <label class="editor-field"><span>Name</span><input type="text" data-field="name" value="${escapeHtml(item.name)}"></label>
      <label class="editor-field"><span>URL</span><input type="text" data-field="url" value="${escapeHtml(item.url)}"></label>
      <label class="editor-field editor-field-full"><span>Description</span><textarea data-field="description" rows="3">${escapeHtml(item.description)}</textarea></label>
      <label class="editor-field editor-field-full"><span>Highlights (comma separated)</span><input type="text" data-field="highlights" value="${escapeHtml((item.highlights || []).join(", "))}"></label>
    `;
    container.appendChild(node);
  });
}

function renderSkillsEditor(items) {
  const container = document.querySelector("#skills-editor .editor-items");
  if (!container) {
    return;
  }
  container.innerHTML = "";
  items.forEach((item, index) => {
    const node = document.createElement("div");
    node.className = "editor-item";
    node.innerHTML = `
      <button type="button" class="editor-remove" data-remove-skill="${index}">×</button>
      <label class="editor-field editor-field-full"><span>Skill</span><input type="text" data-field="name" value="${escapeHtml(item.name)}"></label>
    `;
    container.appendChild(node);
  });
}

function renderEducationEditor(items) {
  const container = document.querySelector("#education-editor .editor-items");
  if (!container) {
    return;
  }
  container.innerHTML = "";
  items.forEach((item, index) => {
    const node = document.createElement("div");
    node.className = "editor-item";
    node.innerHTML = `
      <button type="button" class="editor-remove" data-remove-education="${index}">×</button>
      <label class="editor-field"><span>Institution</span><input type="text" data-field="institution" value="${escapeHtml(item.institution)}"></label>
      <label class="editor-field"><span>Area</span><input type="text" data-field="area" value="${escapeHtml(item.area)}"></label>
      <label class="editor-field"><span>Start Date</span><input type="text" data-field="startDate" value="${escapeHtml(item.startDate)}" placeholder="2026-01-01"></label>
      <label class="editor-field"><span>End Date</span><input type="text" data-field="endDate" value="${escapeHtml(item.endDate)}" placeholder="2026-12-31"></label>
      <label class="editor-field"><span>URL</span><input type="text" data-field="url" value="${escapeHtml(item.url)}"></label>
      <label class="editor-field editor-field-full"><span>Courses (comma separated)</span><input type="text" data-field="courses" value="${escapeHtml((item.courses || []).join(", "))}"></label>
    `;
    container.appendChild(node);
  });
}

function readWorkItemsFromEditor() {
  return Array.from(document.querySelectorAll("#work-editor .editor-item")).map((item) => ({
    name: readField(item, "name"),
    position: readField(item, "position"),
    url: readField(item, "url"),
    startDate: readField(item, "startDate"),
    endDate: readField(item, "endDate"),
    summary: readField(item, "summary"),
    highlights: splitComma(readField(item, "highlights"))
  }));
}

function readProjectItemsFromEditor() {
  return Array.from(document.querySelectorAll("#projects-editor .editor-item")).map((item) => ({
    name: readField(item, "name"),
    description: readField(item, "description"),
    url: readField(item, "url"),
    highlights: splitComma(readField(item, "highlights"))
  }));
}

function readSkillItemsFromEditor() {
  return Array.from(document.querySelectorAll("#skills-editor .editor-item")).map((item) => ({
    name: readField(item, "name")
  }));
}

function readEducationItemsFromEditor() {
  return Array.from(document.querySelectorAll("#education-editor .editor-item")).map((item) => ({
    institution: readField(item, "institution"),
    area: readField(item, "area"),
    url: readField(item, "url"),
    startDate: readField(item, "startDate"),
    endDate: readField(item, "endDate"),
    courses: splitComma(readField(item, "courses"))
  }));
}

function readField(container, field) {
  const element = container.querySelector(`[data-field="${field}"]`);
  if (!element || !element.value) {
    return "";
  }
  return element.value.trim();
}

function splitComma(value) {
  if (!value) {
    return [];
  }
  return value
    .split(",")
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
}

function splitList(value) {
  if (!value) {
    return [];
  }
  return value
    .split("||")
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
}

function blankWork() {
  return {
    name: "",
    position: "",
    url: "",
    startDate: "",
    endDate: "",
    summary: "",
    highlights: []
  };
}

function blankProject() {
  return {
    name: "",
    description: "",
    url: "",
    highlights: []
  };
}

function blankSkill() {
  return { name: "" };
}

function blankEducation() {
  return {
    institution: "",
    area: "",
    url: "",
    startDate: "",
    endDate: "",
    courses: []
  };
}

function escapeHtml(value) {
  return (value || "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}
