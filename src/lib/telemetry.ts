import { getDb, generateId, bufferToHex, toDbId } from "./db";
import type { TelemetryVisitRow, TelemetryLoginRow, TelemetryStats } from "./types";

const MAX_VISITS_PER_HOUR = 120;
const DEDUP_VISITOR_INTERVAL = 60 * 60 * 1000; // 1 hour
const DEDUP_IP_UA_INTERVAL = 20 * 1000; // 20 seconds

function detectBrowser(ua: string): string {
  if (!ua) return "Unknown";
  if (ua.includes("Firefox")) return "Firefox";
  if (ua.includes("Edg")) return "Edge";
  if (ua.includes("Chrome")) return "Chrome";
  if (ua.includes("Safari")) return "Safari";
  if (ua.includes("Opera") || ua.includes("OPR")) return "Opera";
  return "Other";
}

function detectOS(ua: string): string {
  if (!ua) return "Unknown";
  if (ua.includes("Windows")) return "Windows";
  if (ua.includes("Mac OS")) return "macOS";
  if (ua.includes("Linux")) return "Linux";
  if (ua.includes("Android")) return "Android";
  if (ua.includes("iPhone") || ua.includes("iPad")) return "iOS";
  return "Other";
}

function detectDeviceType(ua: string): string {
  if (!ua) return "Unknown";
  if (ua.includes("Mobile") || ua.includes("Android")) return "Mobile";
  if (ua.includes("Tablet") || ua.includes("iPad")) return "Tablet";
  return "Desktop";
}

function extractGeoFromHeaders(headers: Headers): {
  country: string;
  region: string;
  city: string;
} {
  return {
    country:
      headers.get("cf-ipcountry") ||
      headers.get("x-vercel-ip-country") ||
      headers.get("x-appengine-country") ||
      "",
    region:
      headers.get("x-vercel-ip-country-region") ||
      headers.get("x-appengine-region") ||
      "",
    city:
      headers.get("x-vercel-ip-city") ||
      headers.get("x-appengine-city") ||
      "",
  };
}

function getClientIp(headers: Headers): string {
  return (
    headers.get("cf-connecting-ip") ||
    headers.get("x-real-ip") ||
    headers.get("x-forwarded-for")?.split(",")[0]?.trim() ||
    "unknown"
  );
}

export async function trackVisit(request: Request): Promise<void> {
  const db = getDb();
  const headers = request.headers;
  const url = new URL(request.url);

  const ipAddress = getClientIp(headers);
  const userAgent = headers.get("user-agent") || "";
  const visitorId =
    headers.get("x-visitor-id") || `${ipAddress}-${userAgent}`.substring(0, 64);
  const now = new Date().toISOString();

  // Rate limiting: max visits per IP per hour
  const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000).toISOString();
  const recentCount = await db.execute({
    sql: `SELECT COUNT(*) as cnt FROM telemetry_visits
          WHERE ip_address = ? AND visited_at > ?`,
    args: [ipAddress, oneHourAgo],
  });
  if ((recentCount.rows[0].cnt as number) >= MAX_VISITS_PER_HOUR) return;

  // Dedup: same visitor within interval
  const visitorDedupTime = new Date(
    Date.now() - DEDUP_VISITOR_INTERVAL
  ).toISOString();
  const recentVisitor = await db.execute({
    sql: `SELECT id FROM telemetry_visits
          WHERE visitor_id = ? AND visited_at > ?
          LIMIT 1`,
    args: [visitorId, visitorDedupTime],
  });
  if (recentVisitor.rows.length > 0) return;

  // Dedup: same IP+UA within short interval
  const ipUaDedupTime = new Date(
    Date.now() - DEDUP_IP_UA_INTERVAL
  ).toISOString();
  const recentIpUa = await db.execute({
    sql: `SELECT id FROM telemetry_visits
          WHERE ip_address = ? AND user_agent = ? AND visited_at > ?
          LIMIT 1`,
    args: [ipAddress, userAgent, ipUaDedupTime],
  });
  if (recentIpUa.rows.length > 0) return;

  const geo = extractGeoFromHeaders(headers);

  await db.execute({
    sql: `INSERT INTO telemetry_visits
          (id, visitor_id, ip_address, country, region, city, browser, device_type,
           operating_system, user_agent, path, query, accept_language, visited_at, source)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    args: [
      generateId(),
      visitorId,
      ipAddress,
      geo.country,
      geo.region,
      geo.city,
      detectBrowser(userAgent),
      detectDeviceType(userAgent),
      detectOS(userAgent),
      userAgent.substring(0, 2000),
      url.pathname,
      url.search.substring(0, 1000),
      (headers.get("accept-language") || "").substring(0, 255),
      now,
      (headers.get("referer") || "").substring(0, 512),
    ],
  });
}

export async function trackLogin(
  username: string,
  userId: unknown,
  request: Request
): Promise<void> {
  const db = getDb();
  const headers = request.headers;
  const ipAddress = getClientIp(headers);
  const geo = extractGeoFromHeaders(headers);
  const now = new Date().toISOString();

  await db.execute({
    sql: `INSERT INTO telemetry_logins
          (id, username, user_id, ip_address, country, region, city, logged_at)
          VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
    args: [
      generateId(),
      username,
      userId,
      ipAddress,
      geo.country,
      geo.region,
      geo.city,
      now,
    ],
  });
}

export async function getRecentVisits(
  limit: number = 50
): Promise<TelemetryVisitRow[]> {
  const db = getDb();
  const result = await db.execute({
    sql: `SELECT id, visitor_id, ip_address, country, region, city,
                 browser, device_type, operating_system, path, visited_at
          FROM telemetry_visits
          ORDER BY visited_at DESC
          LIMIT ?`,
    args: [limit],
  });

  return result.rows.map((r) => ({
    id: bufferToHex(r.id as ArrayBuffer),
    visitorId: r.visitor_id as string,
    ipAddress: r.ip_address as string,
    country: r.country as string,
    region: r.region as string,
    city: r.city as string,
    browser: r.browser as string,
    deviceType: r.device_type as string,
    operatingSystem: r.operating_system as string,
    path: r.path as string,
    visitedAt: r.visited_at as string,
  }));
}

export async function getRecentLogins(
  limit: number = 50
): Promise<TelemetryLoginRow[]> {
  const db = getDb();
  const result = await db.execute({
    sql: `SELECT id, username, ip_address, country, region, city, logged_at
          FROM telemetry_logins
          ORDER BY logged_at DESC
          LIMIT ?`,
    args: [limit],
  });

  return result.rows.map((r) => ({
    id: bufferToHex(r.id as ArrayBuffer),
    username: r.username as string,
    ipAddress: r.ip_address as string,
    country: r.country as string,
    region: r.region as string,
    city: r.city as string,
    loggedAt: r.logged_at as string,
  }));
}

export async function getVisitStats(): Promise<TelemetryStats> {
  const db = getDb();
  const now = new Date();

  const dayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000).toISOString();
  const weekAgo = new Date(
    now.getTime() - 7 * 24 * 60 * 60 * 1000
  ).toISOString();
  const monthAgo = new Date(
    now.getTime() - 30 * 24 * 60 * 60 * 1000
  ).toISOString();
  const yearAgo = new Date(
    now.getTime() - 365 * 24 * 60 * 60 * 1000
  ).toISOString();

  const [dayResult, weekResult, monthResult, yearResult] = await Promise.all([
    db.execute({
      sql: "SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ?",
      args: [dayAgo],
    }),
    db.execute({
      sql: "SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ?",
      args: [weekAgo],
    }),
    db.execute({
      sql: "SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ?",
      args: [monthAgo],
    }),
    db.execute({
      sql: "SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ?",
      args: [yearAgo],
    }),
  ]);

  return {
    day: dayResult.rows[0].cnt as number,
    week: weekResult.rows[0].cnt as number,
    month: monthResult.rows[0].cnt as number,
    year: yearResult.rows[0].cnt as number,
  };
}

export async function deleteVisit(
  visitId: string
): Promise<{ success: boolean; error?: string }> {
  const db = getDb();
  const visitDbId = toDbId(visitId);

  const result = await db.execute({
    sql: "DELETE FROM telemetry_visits WHERE id = ?",
    args: [visitDbId],
  });

  if (result.rowsAffected === 0) {
    return { success: false, error: "Visita no encontrada" };
  }

  return { success: true };
}
