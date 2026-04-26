import type { APIRoute } from "astro";
import { createTelemetryChallenge } from "../../../lib/telemetryChallenge";

export const POST: APIRoute = async () => {
  return new Response(JSON.stringify({ token: createTelemetryChallenge() }), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Cache-Control": "no-store",
    },
  });
};
