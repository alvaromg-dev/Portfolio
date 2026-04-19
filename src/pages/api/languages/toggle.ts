import type { APIRoute } from "astro";
import { toggleLanguageEnabled } from "../../../lib/languages";

export const POST: APIRoute = async ({ request, redirect }) => {
  const formData = await request.formData();
  const languageId = formData.get("languageId") as string;

  if (!languageId) {
    return redirect("/languages?error=Language+ID+required");
  }

  const result = await toggleLanguageEnabled(languageId);

  if (!result.success) {
    return redirect(`/languages?error=${encodeURIComponent(result.error!)}`);
  }

  return redirect("/languages?success=Language+updated");
};
