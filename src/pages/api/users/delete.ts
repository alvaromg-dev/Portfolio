import type { APIRoute } from "astro";
import { deleteUser } from "../../../lib/users";

export const POST: APIRoute = async ({ request, redirect }) => {
  const formData = await request.formData();
  const userId = formData.get("userId") as string;

  if (!userId) {
    return redirect("/users?error=User+ID+required");
  }

  const result = await deleteUser(userId);

  if (!result.success) {
    return redirect(`/users?error=${encodeURIComponent(result.error!)}`);
  }

  return redirect("/users?success=User+deleted");
};
