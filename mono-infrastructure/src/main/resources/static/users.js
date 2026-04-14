document.addEventListener("DOMContentLoaded", () => {
  const confirmText = document.body.dataset.deleteConfirm || "Delete this user?";
  const deleteButtons = document.querySelectorAll(".users-delete-btn");

  deleteButtons.forEach((button) => {
    button.addEventListener("click", (event) => {
      if (window.confirm(confirmText)) {
        return;
      }
      event.preventDefault();
    });
  });
});

