import type { APIRoute } from "astro";
import { savePortfolioData } from "../../lib/portfolio";
import type { PortfolioData } from "../../lib/types";
import { logger } from "../../lib/logger";
import { canEditPortfolio } from "../../lib/roles";

export const POST: APIRoute = async ({ request, locals }) => {
  const startedAt = Date.now();

  if (!canEditPortfolio(locals.user?.roles)) {
    logger.warn("portfolio.save.denied", {
      user: locals.user?.name || null,
      roles: locals.user?.roles || [],
    });
    return new Response(JSON.stringify({ error: "Sin permisos" }), {
      status: 403,
      headers: { "Content-Type": "application/json" },
    });
  }

  try {
    const body = await request.json();
    const languageCode = body.languageCode as string;
    const data = body.data as PortfolioData;

    if (!languageCode || !data) {
      logger.warn("portfolio.save.invalid_payload", {
        user: locals.user?.name || null,
        languageCode: languageCode || null,
      });
      return new Response(JSON.stringify({ error: "Datos inválidos" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }

    logger.info("portfolio.save.started", {
      user: locals.user?.name || null,
      languageCode,
      hasBasics: Boolean(data.basics),
      workCount: data.work?.length || 0,
      projectsCount: data.projects?.length || 0,
      skillsCount: data.skills?.length || 0,
      educationCount: data.education?.length || 0,
    });

    await savePortfolioData(languageCode, data);

    logger.info("portfolio.save.succeeded", {
      user: locals.user?.name || null,
      languageCode,
      durationMs: Date.now() - startedAt,
    });

    return new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Error interno";
    logger.error("portfolio.save.failed", {
      user: locals.user?.name || null,
      durationMs: Date.now() - startedAt,
      error: message,
      stack: error instanceof Error ? error.stack : undefined,
    });
    return new Response(JSON.stringify({ error: message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
};
