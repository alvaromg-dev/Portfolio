import type { APIRoute } from "astro";
import { deleteAllLogins, deleteAllVisits } from "../../../lib/telemetry";
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
    const table = body.table as string;

    if (table === "visits") {
      const result = await deleteAllVisits();
      return new Response(JSON.stringify(result), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    }

    if (table === "logins") {
      const result = await deleteAllLogins();
      return new Response(JSON.stringify(result), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    }

    return new Response(JSON.stringify({ error: "Invalid telemetry table" }), {
      status: 400,
      headers: { "Content-Type": "application/json" },
    });
  } catch {
    return new Response(JSON.stringify({ error: "Error al eliminar telemetría" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
};
