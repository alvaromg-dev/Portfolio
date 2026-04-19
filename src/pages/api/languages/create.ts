import type { APIRoute } from "astro";
import { createLanguage } from "../../../lib/languages";

export const POST: APIRoute = async ({ request, redirect }) => {
  const formData = await request.formData();
  const code = (formData.get("code") as string)?.trim().toLowerCase();
  const name = (formData.get("name") as string)?.trim();

  const result = await createLanguage(code, name);

  if (!result.success) {
    return redirect(`/languages?error=${encodeURIComponent(result.error!)}`);
  }

  return redirect("/languages?success=Language+created");
};
