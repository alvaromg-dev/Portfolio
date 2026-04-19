import type { APIRoute } from "astro";
import { updateUser } from "../../../lib/users";

export const POST: APIRoute = async ({ request, redirect }) => {
  const formData = await request.formData();
  const userId = formData.get("userId") as string;
  const username = (formData.get("givenNames") as string)?.trim();
  const newPassword = (formData.get("newPassword") as string)?.trim() || undefined;
  const roleCodes = formData.getAll("roles") as string[];

  if (!userId) {
    return redirect("/users?error=User+ID+required");
  }

  const result = await updateUser(userId, username, "", roleCodes, newPassword || undefined);

  if (!result.success) {
    return redirect(`/users?error=${encodeURIComponent(result.error!)}`);
  }

  return redirect("/users?success=User+updated");
};
