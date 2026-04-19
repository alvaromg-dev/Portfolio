import type { APIRoute } from "astro";
import { createUser } from "../../../lib/users";

export const POST: APIRoute = async ({ request, redirect }) => {
  const formData = await request.formData();
  const username = (formData.get("email") as string)?.trim();
  const password = formData.get("password") as string;
  const roleCodes = formData.getAll("roles") as string[];

  const result = await createUser(username, password, "", "", roleCodes);

  if (!result.success) {
    return redirect(`/users?error=${encodeURIComponent(result.error!)}`);
  }

  return redirect("/users?success=User+created");
};
