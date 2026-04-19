import type { APIRoute } from "astro";
import { trackVisit } from "../../../lib/telemetry";

export const POST: APIRoute = async ({ request }) => {
  try {
    await trackVisit(request);
    return new Response(JSON.stringify({ success: true }), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    });
  } catch {
    return new Response(JSON.stringify({ error: "Error al registrar visita" }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
};
