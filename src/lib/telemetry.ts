import { getDb, generateId, bufferToHex, toDbId } from "./db";
import type { TelemetryVisitRow, TelemetryLoginRow, TelemetryStats, TelemetryPage } from "./types";

const MAX_VISITS_PER_HOUR = 120;
const DEDUP_VISITOR_INTERVAL = 60 * 60 * 1000; // 1 hour
const DEDUP_IP_UA_INTERVAL = 20 * 1000; // 20 seconds
const GEO_LOOKUP_TIMEOUT_MS = 1500;
const GEO_BACKFILL_LIMIT = 10;
const GEO_BACKFILL_SCAN_LIMIT = 100;
const BOT_USER_AGENT_PATTERNS = [
  "bot",
  "crawler",
  "spider",
  "slurp",
  "bingpreview",
  "facebookexternalhit",
  "whatsapp",
  "telegrambot",
  "discordbot",
  "linkedinbot",
  "twitterbot",
  "bytespider",
  "semrush",
  "ahrefs",
  "mj12bot",
  "dotbot",
  "petalbot",
  "yandex",
  "baiduspider",
  "duckduckbot",
  "applebot",
  "google-inspectiontool",
  "uptime",
  "monitoring",
  "headless",
  "lighthouse",
];

type GeoLocation = {
  country: string;
  region: string;
  city: string;
};

const LOCALHOST_GEO: GeoLocation = { country: "localhost", region: "", city: "" };
type TelemetryTable = "telemetry_visits" | "telemetry_logins";

type TrackVisitOptions = {
  path?: string;
  query?: string;
  source?: string;
};

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

function isBotUserAgent(userAgent: string): boolean {
  const ua = userAgent.toLowerCase();
  return BOT_USER_AGENT_PATTERNS.some((pattern) => ua.includes(pattern));
}

function botFilterSql(column: string = "user_agent"): string {
  return BOT_USER_AGENT_PATTERNS
    .map((pattern) => `LOWER(COALESCE(${column}, '')) NOT LIKE '%${pattern}%'`)
    .join(" AND ");
}

function countryCodeToName(country: string): string {
  const value = country.trim();
  if (!/^[A-Z]{2}$/i.test(value)) return value;

  try {
    return new Intl.DisplayNames(["en"], { type: "region" }).of(value.toUpperCase()) || value;
  } catch {
    return value;
  }
}

function normalizeGeo(geo: GeoLocation): GeoLocation {
  return {
    country: countryCodeToName(geo.country).substring(0, 64),
    region: geo.region.substring(0, 128),
    city: geo.city.substring(0, 128),
  };
}

function hasAnyGeo(geo: GeoLocation): boolean {
  return hasGeoValue(geo.country) || hasGeoValue(geo.region) || hasGeoValue(geo.city);
}

function cleanHeaderValue(value: string | null): string {
  if (!value || value === "XX") return "";

  try {
    return decodeURIComponent(value).trim().substring(0, 128);
  } catch {
    return value.trim().substring(0, 128);
  }
}

function extractGeoFromHeaders(headers: Headers): GeoLocation {
  return {
    country:
      cleanHeaderValue(headers.get("cf-ipcountry")) ||
      cleanHeaderValue(headers.get("x-vercel-ip-country")) ||
      cleanHeaderValue(headers.get("x-appengine-country")) ||
      "",
    region:
      cleanHeaderValue(headers.get("cf-region")) ||
      cleanHeaderValue(headers.get("cf-ipregion")) ||
      cleanHeaderValue(headers.get("x-vercel-ip-country-region")) ||
      cleanHeaderValue(headers.get("x-appengine-region")) ||
      "",
    city:
      cleanHeaderValue(headers.get("cf-ipcity")) ||
      cleanHeaderValue(headers.get("x-vercel-ip-city")) ||
      cleanHeaderValue(headers.get("x-appengine-city")) ||
      "",
  };
}

function normalizeIpAddress(ipAddress: string): string {
  const cleanIp = ipAddress.trim().toLowerCase();
  return cleanIp.startsWith("::ffff:") ? cleanIp.substring("::ffff:".length) : cleanIp;
}

function ipAddressVariants(ipAddress: string): string[] {
  const cleanIp = ipAddress.trim().toLowerCase();
  const normalizedIp = normalizeIpAddress(cleanIp);
  const variants = [cleanIp, normalizedIp];
  if (/^\d{1,3}(?:\.\d{1,3}){3}$/.test(normalizedIp)) {
    variants.push(`::ffff:${normalizedIp}`);
  }
  return Array.from(new Set(variants.filter(Boolean)));
}

function isPublicIp(ipAddress: string): boolean {
  const cleanIp = normalizeIpAddress(ipAddress);
  if (!cleanIp || cleanIp === "unknown") return false;
  if (cleanIp.includes(":")) {
    return !(
      cleanIp === "::1" ||
      cleanIp.startsWith("fc") ||
      cleanIp.startsWith("fd") ||
      cleanIp.startsWith("fe80:")
    );
  }

  const parts = cleanIp.split(".").map((part) => Number(part));
  if (parts.length !== 4 || parts.some((part) => !Number.isInteger(part) || part < 0 || part > 255)) {
    return false;
  }

  const [a, b] = parts;
  return !(
    a === 10 ||
    a === 127 ||
    (a === 172 && b >= 16 && b <= 31) ||
    (a === 192 && b === 168) ||
    (a === 169 && b === 254)
  );
}

function isLocalhostIp(ipAddress: string): boolean {
  const cleanIp = normalizeIpAddress(ipAddress);
  return cleanIp === "127.0.0.1" || cleanIp === "::1";
}

async function fetchJsonWithTimeout<T>(url: string, init: RequestInit = {}): Promise<T | null> {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), GEO_LOOKUP_TIMEOUT_MS);

  try {
    const response = await fetch(url, { ...init, signal: controller.signal });
    return response.ok ? await response.json() as T : null;
  } catch {
    return null;
  } finally {
    clearTimeout(timeout);
  }
}

async function lookupStoredLocationByIp(ipAddress: string): Promise<GeoLocation> {
  const db = getDb();
  const ipVariants = ipAddressVariants(ipAddress);
  const result = await db.execute({
    sql: `SELECT country, region, city, at
          FROM (
            SELECT country, region, city, visited_at AS at
            FROM telemetry_visits
            WHERE ip_address = ANY(?)
              AND country IS NOT NULL AND region IS NOT NULL AND city IS NOT NULL
              AND TRIM(country) <> '' AND TRIM(region) <> '' AND TRIM(city) <> ''
              AND LOWER(country) NOT IN ('unknown', 'null')
              AND LOWER(region) NOT IN ('unknown', 'null')
              AND LOWER(city) NOT IN ('unknown', 'null')
            UNION ALL
            SELECT country, region, city, logged_at AS at
            FROM telemetry_logins
            WHERE ip_address = ANY(?)
              AND country IS NOT NULL AND region IS NOT NULL AND city IS NOT NULL
              AND TRIM(country) <> '' AND TRIM(region) <> '' AND TRIM(city) <> ''
              AND LOWER(country) NOT IN ('unknown', 'null')
              AND LOWER(region) NOT IN ('unknown', 'null')
              AND LOWER(city) NOT IN ('unknown', 'null')
          ) known_locations
          ORDER BY at DESC
          LIMIT 1`,
    args: [ipVariants, ipVariants],
  });

  const row = result.rows[0];
  return row ? normalizeGeo({
    country: String(row.country || ""),
    region: String(row.region || ""),
    city: String(row.city || ""),
  }) : { country: "", region: "", city: "" };
}

export async function resolveLocationByIp(ipAddress: string): Promise<GeoLocation> {
  const lookupIp = normalizeIpAddress(ipAddress);
  if (isLocalhostIp(lookupIp)) return LOCALHOST_GEO;
  if (!isPublicIp(lookupIp)) return { country: "", region: "", city: "" };

  const storedGeo = await lookupStoredLocationByIp(ipAddress);
  if (hasAnyGeo(storedGeo)) return storedGeo;

  const ipInfoToken = process.env.IPINFO_TOKEN;
  const ipInfoData = await fetchJsonWithTimeout<{
    country?: string;
    region?: string;
    city?: string;
  }>(
    `https://ipinfo.io/${encodeURIComponent(lookupIp)}/json${ipInfoToken ? `?token=${encodeURIComponent(ipInfoToken)}` : ""}`,
    { headers: { "User-Agent": "Mozilla/5.0" } }
  );
  if (ipInfoData) {
    const ipInfoGeo = normalizeGeo({
      country: ipInfoData.country || "",
      region: ipInfoData.region || "",
      city: ipInfoData.city || "",
    });
    if (hasAnyGeo(ipInfoGeo)) return ipInfoGeo;
  }

  const ipApiIsData = await fetchJsonWithTimeout<{
    location?: {
      country?: string;
      state?: string;
      city?: string;
    };
  }>(`https://api.ipapi.is/?q=${encodeURIComponent(lookupIp)}`);
  if (ipApiIsData) {
    const ipApiIsGeo = normalizeGeo({
      country: ipApiIsData.location?.country || "",
      region: ipApiIsData.location?.state || "",
      city: ipApiIsData.location?.city || "",
    });
    if (hasAnyGeo(ipApiIsGeo)) return ipApiIsGeo;
  }

  const geoJsData = await fetchJsonWithTimeout<{
    country?: string;
    region?: string;
    city?: string;
  }>(`https://get.geojs.io/v1/ip/geo/${encodeURIComponent(lookupIp)}.json`);
  if (geoJsData) {
    const geoJsGeo = normalizeGeo({
      country: geoJsData.country || "",
      region: geoJsData.region || "",
      city: geoJsData.city || "",
    });
    if (hasAnyGeo(geoJsGeo)) return geoJsGeo;
  }

  const ipApiData = await fetchJsonWithTimeout<{
    status?: string;
    country?: string;
    regionName?: string;
    city?: string;
  }>(`http://ip-api.com/json/${encodeURIComponent(lookupIp)}?fields=status,country,regionName,city`);
  if (ipApiData?.status === "success") {
    const ipApiGeo = normalizeGeo({
      country: ipApiData.country || "",
      region: ipApiData.regionName || "",
      city: ipApiData.city || "",
    });
    if (hasAnyGeo(ipApiGeo)) return ipApiGeo;
  }

  return { country: "", region: "", city: "" };
}

async function resolveGeo(headers: Headers, ipAddress: string): Promise<GeoLocation> {
  if (isLocalhostIp(ipAddress)) return LOCALHOST_GEO;

  const headerGeo = extractGeoFromHeaders(headers);
  if (headerGeo.country && headerGeo.region && headerGeo.city) return headerGeo;

  const ipGeo = await resolveLocationByIp(ipAddress);
  return {
    country: headerGeo.country || ipGeo.country,
    region: headerGeo.region || ipGeo.region,
    city: headerGeo.city || ipGeo.city,
  };
}

function hasGeoValue(value: unknown): boolean {
  const clean = String(value || "").trim().toLowerCase();
  return clean !== "" && clean !== "unknown" && clean !== "null";
}

function needsGeoBackfill(row: Record<string, unknown>): boolean {
  return !hasGeoValue(row.country) || !hasGeoValue(row.region) || !hasGeoValue(row.city);
}

function needsRowLocation(row: TelemetryVisitRow | TelemetryLoginRow): boolean {
  return !hasGeoValue(row.country) || !hasGeoValue(row.region) || !hasGeoValue(row.city);
}

function mergeGeo(row: TelemetryVisitRow | TelemetryLoginRow, geo: GeoLocation): GeoLocation {
  return {
    country: hasGeoValue(row.country) ? row.country : geo.country,
    region: hasGeoValue(row.region) ? row.region : geo.region,
    city: hasGeoValue(row.city) ? row.city : geo.city,
  };
}

async function updateLocationForIp(table: TelemetryTable, ipAddress: string, geo: GeoLocation): Promise<void> {
  const db = getDb();
  const ipVariants = ipAddressVariants(ipAddress);
  await db.execute({
    sql: `UPDATE ${table}
          SET country = CASE
                WHEN country IS NULL OR TRIM(country) = '' OR LOWER(country) IN ('unknown', 'null') THEN ?
                ELSE country
              END,
              region = CASE
                WHEN region IS NULL OR TRIM(region) = '' OR LOWER(region) IN ('unknown', 'null') THEN ?
                ELSE region
              END,
              city = CASE
                WHEN city IS NULL OR TRIM(city) = '' OR LOWER(city) IN ('unknown', 'null') THEN ?
                ELSE city
              END
          WHERE ip_address = ANY(?)
            AND (
              country IS NULL OR region IS NULL OR city IS NULL OR
              TRIM(country) = '' OR TRIM(region) = '' OR TRIM(city) = '' OR
              LOWER(country) = 'unknown' OR LOWER(region) = 'unknown' OR LOWER(city) = 'unknown' OR
              LOWER(country) = 'null' OR LOWER(region) = 'null' OR LOWER(city) = 'null'
            )`,
    args: [geo.country, geo.region, geo.city, ipVariants],
  });
}

async function reprocessMissingLocationsForRows(
  table: TelemetryTable,
  rows: Array<TelemetryVisitRow | TelemetryLoginRow>
): Promise<void> {
  const geoByIp = new Map<string, Promise<GeoLocation>>();

  await Promise.all(rows.map(async (row) => {
    if (!needsRowLocation(row)) return;

    const ipAddress = row.ipAddress;
    if (!isLocalhostIp(ipAddress) && !isPublicIp(ipAddress)) return;

    if (!geoByIp.has(ipAddress)) {
      geoByIp.set(ipAddress, resolveLocationByIp(ipAddress));
    }

    const geo = await geoByIp.get(ipAddress)!;
    if (!hasAnyGeo(geo)) return;

    const merged = mergeGeo(row, geo);
    if (
      row.country === merged.country &&
      row.region === merged.region &&
      row.city === merged.city
    ) {
      return;
    }

    row.country = merged.country;
    row.region = merged.region;
    row.city = merged.city;

    await updateLocationForIp(table, ipAddress, geo);
  }));
}

async function backfillTableLocations(table: TelemetryTable, orderColumn: "visited_at" | "logged_at"): Promise<void> {
  const db = getDb();
  const result = await db.execute({
    sql: `SELECT ip_address, country, region, city
          FROM ${table}
          WHERE (
            country IS NULL OR region IS NULL OR city IS NULL OR
            TRIM(country) = '' OR TRIM(region) = '' OR TRIM(city) = '' OR
            LOWER(country) = 'unknown' OR LOWER(region) = 'unknown' OR LOWER(city) = 'unknown' OR
            LOWER(country) = 'null' OR LOWER(region) = 'null' OR LOWER(city) = 'null'
          )
          ORDER BY ${orderColumn} DESC
          LIMIT ?`,
    args: [GEO_BACKFILL_SCAN_LIMIT],
  });

  const rowsByIp = new Map<string, Record<string, unknown>>();
  for (const row of result.rows) {
    if (rowsByIp.size >= GEO_BACKFILL_LIMIT) break;

    const ipAddress = String(row.ip_address || "");
    if (isLocalhostIp(ipAddress)) {
      if (row.country !== LOCALHOST_GEO.country && !rowsByIp.has(ipAddress)) {
        rowsByIp.set(ipAddress, row);
      }
      continue;
    }

    if (isPublicIp(ipAddress) && needsGeoBackfill(row) && !rowsByIp.has(ipAddress)) {
      rowsByIp.set(ipAddress, row);
    }
  }

  await Promise.all(Array.from(rowsByIp).map(async ([ipAddress, row]) => {
    const geo = await resolveLocationByIp(ipAddress);
    if (!hasAnyGeo(geo)) return;
    await updateLocationForIp(table, ipAddress, {
      country: hasGeoValue(row.country) ? String(row.country) : geo.country,
      region: hasGeoValue(row.region) ? String(row.region) : geo.region,
      city: hasGeoValue(row.city) ? String(row.city) : geo.city,
    });
  }));
}

export async function backfillMissingTelemetryLocations(): Promise<void> {
  await backfillTableLocations("telemetry_visits", "visited_at");
  await backfillTableLocations("telemetry_logins", "logged_at");
}

function getClientIp(headers: Headers, clientAddress?: string): string {
  return (
    headers.get("cf-connecting-ip") ||
    headers.get("x-real-ip") ||
    headers.get("x-forwarded-for")?.split(",")[0]?.trim() ||
    clientAddress ||
    "unknown"
  );
}

function cleanPath(path: unknown): string | undefined {
  if (typeof path !== "string" || !path.startsWith("/") || path.startsWith("/api/")) {
    return undefined;
  }
  return path.substring(0, 512);
}

function cleanQuery(query: unknown): string | undefined {
  if (typeof query !== "string") return undefined;
  return query.startsWith("?") ? query.substring(0, 1000) : "";
}

function cleanSource(source: unknown): string | undefined {
  return typeof source === "string" ? source.substring(0, 512) : undefined;
}

export async function trackVisit(
  request: Request,
  clientAddress?: string,
  options: TrackVisitOptions = {}
): Promise<void> {
  const db = getDb();
  const headers = request.headers;
  const url = new URL(request.url);

  const ipAddress = getClientIp(headers, clientAddress);
  const userAgent = headers.get("user-agent") || "";
  if (isBotUserAgent(userAgent)) return;

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

  const geo = await resolveGeo(headers, ipAddress);

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
      cleanPath(options.path) || url.pathname,
      cleanQuery(options.query) ?? url.search.substring(0, 1000),
      (headers.get("accept-language") || "").substring(0, 255),
      now,
      cleanSource(options.source) || (headers.get("referer") || "").substring(0, 512),
    ],
  });
}

export async function trackLogin(
  username: string,
  userId: unknown,
  request: Request,
  clientAddress?: string
): Promise<void> {
  const db = getDb();
  const headers = request.headers;
  const ipAddress = getClientIp(headers, clientAddress);
  const geo = await resolveGeo(headers, ipAddress);
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
  limit: number = 50,
  offset: number = 0,
  since?: string
): Promise<TelemetryVisitRow[]> {
  const db = getDb();
  const humanVisitFilter = botFilterSql("user_agent");
  const dateFilter = since ? " AND visited_at > ?" : "";
  const result = await db.execute({
    sql: `SELECT id, visitor_id, ip_address, country, region, city,
                 browser, device_type, operating_system, path, visited_at
          FROM telemetry_visits
          WHERE ${humanVisitFilter}${dateFilter}
          ORDER BY visited_at DESC
          LIMIT ? OFFSET ?`,
    args: since ? [since, limit, offset] : [limit, offset],
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
  limit: number = 50,
  offset: number = 0,
  since?: string
): Promise<TelemetryLoginRow[]> {
  const db = getDb();
  const dateFilter = since ? "WHERE logged_at > ?" : "";
  const result = await db.execute({
    sql: `SELECT id, username, ip_address, country, region, city, logged_at
          FROM telemetry_logins
          ${dateFilter}
          ORDER BY logged_at DESC
          LIMIT ? OFFSET ?`,
    args: since ? [since, limit, offset] : [limit, offset],
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

function normalizePage(page: number): number {
  return Number.isInteger(page) && page > 0 ? page : 1;
}

function normalizePageSize(pageSize: number): number {
  if (!Number.isInteger(pageSize) || pageSize < 1) return 25;
  return Math.min(pageSize, 100);
}

export async function getVisitsPage(
  page: number = 1,
  pageSize: number = 25,
  since?: string
): Promise<TelemetryPage<TelemetryVisitRow>> {
  const db = getDb();
  const safePageSize = normalizePageSize(pageSize);
  const dateFilter = since ? " AND visited_at > ?" : "";
  const countResult = await db.execute({
    sql: `SELECT COUNT(*) as cnt FROM telemetry_visits WHERE ${botFilterSql("user_agent")}${dateFilter}`,
    args: since ? [since] : [],
  });
  const total = Number(countResult.rows[0].cnt || 0);
  const totalPages = Math.max(1, Math.ceil(total / safePageSize));
  const safePage = Math.min(normalizePage(page), totalPages);
  const rows = await getRecentVisits(safePageSize, (safePage - 1) * safePageSize, since);
  await reprocessMissingLocationsForRows("telemetry_visits", rows);

  return {
    rows,
    page: safePage,
    pageSize: safePageSize,
    total,
    totalPages,
  };
}

export async function getLoginsPage(
  page: number = 1,
  pageSize: number = 25,
  since?: string
): Promise<TelemetryPage<TelemetryLoginRow>> {
  const db = getDb();
  const safePageSize = normalizePageSize(pageSize);
  const countResult = await db.execute({
    sql: `SELECT COUNT(*) as cnt FROM telemetry_logins${since ? " WHERE logged_at > ?" : ""}`,
    args: since ? [since] : [],
  });
  const total = Number(countResult.rows[0].cnt || 0);
  const totalPages = Math.max(1, Math.ceil(total / safePageSize));
  const safePage = Math.min(normalizePage(page), totalPages);
  const rows = await getRecentLogins(safePageSize, (safePage - 1) * safePageSize, since);
  await reprocessMissingLocationsForRows("telemetry_logins", rows);

  return {
    rows,
    page: safePage,
    pageSize: safePageSize,
    total,
    totalPages,
  };
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
      sql: `SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ? AND ${botFilterSql("user_agent")}`,
      args: [dayAgo],
    }),
    db.execute({
      sql: `SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ? AND ${botFilterSql("user_agent")}`,
      args: [weekAgo],
    }),
    db.execute({
      sql: `SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ? AND ${botFilterSql("user_agent")}`,
      args: [monthAgo],
    }),
    db.execute({
      sql: `SELECT COUNT(*) as cnt FROM telemetry_visits WHERE visited_at > ? AND ${botFilterSql("user_agent")}`,
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

export async function deleteAllVisits(): Promise<{ success: boolean; deleted: number }> {
  const db = getDb();
  const result = await db.execute("DELETE FROM telemetry_visits");
  return { success: true, deleted: result.rowsAffected };
}

export async function deleteAllLogins(): Promise<{ success: boolean; deleted: number }> {
  const db = getDb();
  const result = await db.execute("DELETE FROM telemetry_logins");
  return { success: true, deleted: result.rowsAffected };
}
