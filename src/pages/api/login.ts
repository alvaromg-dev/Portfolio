import type { APIRoute } from "astro";
import {
  authenticateUser,
  isLoginBlocked,
  registerLoginFailure,
  registerLoginSuccess,
  getSessionMaxAge,
  isSecureCookie,
} from "../../lib/auth";
import { trackLogin } from "../../lib/telemetry";
import { getDb } from "../../lib/db";

export const POST: APIRoute = async ({ request, cookies, redirect }) => {
  const formData = await request.formData();
  const username = (formData.get("email") as string)?.trim();
  const password = formData.get("password") as string;

  if (!username || !password) {
    return redirect("/login?error=true");
  }

  const throttleKey = username.toLowerCase();
  if (isLoginBlocked(throttleKey)) {
    return redirect("/login?locked=true");
  }

  const session = await authenticateUser(username, password);
  if (!session) {
    registerLoginFailure(throttleKey);
    await trackLogin(username, null, request);
    return redirect("/login?error=true");
  }

  registerLoginSuccess(throttleKey);

  // Get user ID for telemetry
  const db = getDb();
  const userResult = await db.execute({
    sql: "SELECT id FROM users WHERE name = ?",
    args: [username],
  });
  const userId = userResult.rows.length > 0 ? (userResult.rows[0].id as ArrayBuffer) : null;
  await trackLogin(username, userId, request);

  cookies.set("session", session.id, {
    path: "/",
    httpOnly: true,
    sameSite: "lax",
    secure: isSecureCookie(),
    maxAge: getSessionMaxAge(),
  });

  return redirect("/");
};
