document.addEventListener("DOMContentLoaded", () => {
  const confirmText = document.body.dataset.deleteConfirm || "Delete this visit?";
  const buttons = document.querySelectorAll(".telemetry-delete-btn");

  buttons.forEach((button) => {
    button.addEventListener("click", async () => {
      const id = button.dataset.id;
      if (!id) {
        return;
      }

      if (!window.confirm(confirmText)) {
        return;
      }

      button.disabled = true;
      try {
        const response = await fetch(`/api/mono/telemetry/delete?id=${encodeURIComponent(id)}`, {
          method: "POST"
        });

        if (!response.ok) {
          throw new Error(`Delete failed (${response.status})`);
        }

        window.location.reload();
      } catch (error) {
        console.error(error);
        alert("No se pudo eliminar el registro.");
        button.disabled = false;
      }
    });
  });
});
