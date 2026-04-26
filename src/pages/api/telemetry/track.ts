import type { APIRoute } from "astro";
import { trackVisit } from "../../../lib/telemetry";
import { consumeTelemetryChallenge } from "../../../lib/telemetryChallenge";

type TrackPayload = {
  token?: unknown;
  path?: unknown;
  query?: unknown;
  source?: unknown;
  signals?: {
    language?: unknown;
    timezone?: unknown;
    screen?: unknown;
    visibilityState?: unknown;
    webdriver?: unknown;
  };
};

export const POST: APIRoute = async ({ request, clientAddress }) => {
  try {
    const payload = await request.json().catch(() => ({})) as TrackPayload;
    if (!consumeTelemetryChallenge(payload.token, payload.signals || {})) {
      return new Response(null, { status: 204 });
    }

    await trackVisit(request, clientAddress, {
      path: typeof payload.path === "string" ? payload.path : undefined,
      query: typeof payload.query === "string" ? payload.query : undefined,
      source: typeof payload.source === "string" ? payload.source : undefined,
    });
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
