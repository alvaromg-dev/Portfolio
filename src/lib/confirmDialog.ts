type ConfirmDialogOptions = {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
};

let activeDialog: HTMLElement | null = null;

export function confirmDialog({
  title,
  message,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
}: ConfirmDialogOptions): Promise<boolean> {
  activeDialog?.remove();

  return new Promise((resolve) => {
    const backdrop = document.createElement("div");
    backdrop.className = "confirm-dialog-backdrop";
    backdrop.setAttribute("role", "presentation");

    const dialog = document.createElement("section");
    dialog.className = "confirm-dialog";
    dialog.setAttribute("role", "dialog");
    dialog.setAttribute("aria-modal", "true");
    dialog.setAttribute("aria-labelledby", "confirm-dialog-title");
    dialog.setAttribute("aria-describedby", "confirm-dialog-message");

    const heading = document.createElement("h2");
    heading.id = "confirm-dialog-title";
    heading.textContent = title;

    const body = document.createElement("p");
    body.id = "confirm-dialog-message";
    body.textContent = message;

    const actions = document.createElement("div");
    actions.className = "confirm-dialog-actions";

    const cancelButton = document.createElement("button");
    cancelButton.type = "button";
    cancelButton.className = "confirm-dialog-cancel";
    cancelButton.textContent = cancelLabel;

    const confirmButton = document.createElement("button");
    confirmButton.type = "button";
    confirmButton.className = "confirm-dialog-confirm";
    confirmButton.textContent = confirmLabel;

    actions.append(cancelButton, confirmButton);
    dialog.append(heading, body, actions);
    backdrop.append(dialog);
    document.body.append(backdrop);
    activeDialog = backdrop;

    const previousFocus = document.activeElement instanceof HTMLElement ? document.activeElement : null;

    const close = (result: boolean) => {
      document.removeEventListener("keydown", onKeyDown);
      backdrop.remove();
      activeDialog = null;
      previousFocus?.focus();
      resolve(result);
    };

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") close(false);
    };

    cancelButton.addEventListener("click", () => close(false));
    confirmButton.addEventListener("click", () => close(true));
    backdrop.addEventListener("click", (event) => {
      if (event.target === backdrop) close(false);
    });
    document.addEventListener("keydown", onKeyDown);
    confirmButton.focus();
  });
}
