import type { APIRoute } from "astro";
import { deleteVisit } from "../../../lib/telemetry";
import { canEditPortfolio } from "../../../lib/roles";

export const POST: APIRoute = async ({ request, locals }) => {
  if (!canEditPortfolio(locals.user?.roles)) {
    return new Response(JSON.stringify({ error: "Forbidden" }), {
      status: 403,
      headers: { "Content-Type": "application/json" },
    });
  }

  try {
    const body = await request.json();
    const visitId = body.visitId as string;

    if (!visitId) {
      return new Response(JSON.stringify({ error: "Visit ID required" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }

    const result = await deleteVisit(visitId);
    if (!result.success) {
      return new Response(JSON.stringify({ error: result.error }), {
        status: 404,
        headers: { "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch {
    return new Response(JSON.stringify({ error: "Error al eliminar visita" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
};
