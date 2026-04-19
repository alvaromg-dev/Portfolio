import type { MiddlewareHandler } from "astro";
import { getSession } from "./lib/auth";
import { logger } from "./lib/logger";
import { canAdminister, canEditPortfolio } from "./lib/roles";

export const onRequest: MiddlewareHandler = async (context, next) => {
  const locals = context.locals as App.Locals;
  const startedAt = Date.now();
  const method = context.request.method;
  const path = context.url.pathname;

  function logCompleted(status: number, reason?: string): void {
    logger.info("http.request", {
      method,
      path,
      status,
      durationMs: Date.now() - startedAt,
      user: locals.user?.name || null,
      reason: reason || null,
    });
  }

  // Check session cookie
  const sessionCookie = context.cookies.get("session");
  if (sessionCookie) {
    const session = getSession(sessionCookie.value);
    if (session) {
      locals.user = {
        id: session.id,
        name: session.name,
        roles: session.roles,
      };
    }
  }

  // Protected routes
  const user = locals.user;

  const cvEditorPaths = ["/telemetry"];
  const adminPaths = ["/users", "/languages"];

  const isProtected =
    cvEditorPaths.some((p) => path === p || path.startsWith(p + "/")) ||
    adminPaths.some((p) => path === p || path.startsWith(p + "/"));

  const isApiProtected =
    path.startsWith("/api/portfolio") ||
    path.startsWith("/api/telemetry/delete") ||
    path.startsWith("/api/users") ||
    path.startsWith("/api/languages");

  if (isProtected || isApiProtected) {
    if (!user) {
      if (isApiProtected) {
        const response = new Response(JSON.stringify({ error: "Unauthorized" }), {
          status: 401,
          headers: { "Content-Type": "application/json" },
        });
        logCompleted(response.status, "unauthorized");
        return response;
      }
      const response = context.redirect("/login");
      logCompleted(response.status, "redirect_login");
      return response;
    }

    const needsCvEditor = cvEditorPaths.some(
      (p) => path === p || path.startsWith(p + "/")
    ) || path.startsWith("/api/telemetry") || path.startsWith("/api/portfolio");
    const needsAdmin = adminPaths.some(
      (p) => path === p || path.startsWith(p + "/")
    ) || path.startsWith("/api/users") || path.startsWith("/api/languages");

    if (needsAdmin && !canAdminister(user.roles)) {
      if (isApiProtected) {
        const response = new Response(JSON.stringify({ error: "Forbidden" }), {
          status: 403,
          headers: { "Content-Type": "application/json" },
        });
        logCompleted(response.status, "forbidden_admin");
        return response;
      }
      const response = context.redirect("/");
      logCompleted(response.status, "redirect_forbidden_admin");
      return response;
    }

    if (needsCvEditor && !canEditPortfolio(user.roles)) {
      if (isApiProtected) {
        const response = new Response(JSON.stringify({ error: "Forbidden" }), {
          status: 403,
          headers: { "Content-Type": "application/json" },
        });
        logCompleted(response.status, "forbidden_editor");
        return response;
      }
      const response = context.redirect("/");
      logCompleted(response.status, "redirect_forbidden_editor");
      return response;
    }
  }

  try {
    const response = await next();
    response.headers.set("X-Frame-Options", "DENY");
    response.headers.set("X-Content-Type-Options", "nosniff");
    response.headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
    response.headers.set("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
    logCompleted(response.status);
    return response;
  } catch (error) {
    logger.error("http.request.failed", {
      method,
      path,
      durationMs: Date.now() - startedAt,
      user: locals.user?.name || null,
      error: error instanceof Error ? error.message : String(error),
    });
    throw error;
  }
};
