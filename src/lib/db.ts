import { Pool } from "pg";
import { randomBytes, randomUUID } from "node:crypto";
import { logger } from "./logger";

type DbArgs = unknown[];

type DbRequest =
  | string
  | {
      sql: string;
      args?: DbArgs;
    };

type DbResponse = {
  rows: Record<string, unknown>[];
  rowsAffected: number;
};

type DbClient = {
  execute(request: DbRequest): Promise<DbResponse>;
};

const globalRef = globalThis as unknown as {
  __dbClient?: DbClient;
  __dbPool?: Pool;
};

const dbIdMode = (process.env.DB_ID_MODE || "uuid").toLowerCase();

function toPgPlaceholders(sql: string): string {
  let index = 0;
  return sql.replace(/\?/g, () => {
    index += 1;
    return `$${index}`;
  });
}

function getPool(): Pool {
  if (globalRef.__dbPool) return globalRef.__dbPool;

  const ssl = process.env.PGSSL === "true" ? { rejectUnauthorized: false } : undefined;
  const databaseUrl = process.env.DATABASE_URL;

  if (!databaseUrl && (!process.env.PGHOST || !process.env.PGUSER || !process.env.PGPASSWORD || !process.env.PGDATABASE)) {
    throw new Error("Missing required database environment variables (DATABASE_URL or PGHOST/PGUSER/PGPASSWORD/PGDATABASE)");
  }

  globalRef.__dbPool = databaseUrl
    ? new Pool({ connectionString: databaseUrl, ssl })
    : new Pool({
        host: process.env.PGHOST,
        port: Number(process.env.PGPORT || "5432"),
        user: process.env.PGUSER,
        password: process.env.PGPASSWORD,
        database: process.env.PGDATABASE,
        ssl,
      });

  return globalRef.__dbPool;
}

function normalizeId(value: string): string {
  const clean = value.trim().toLowerCase().replace(/-/g, "");
  if (/^[0-9a-f]{32}$/.test(clean)) {
    return `${clean.slice(0, 8)}-${clean.slice(8, 12)}-${clean.slice(12, 16)}-${clean.slice(16, 20)}-${clean.slice(20)}`;
  }
  return value;
}

export function getDb(): DbClient {
  if (globalRef.__dbClient) return globalRef.__dbClient;

  const pool = getPool();

  globalRef.__dbClient = {
    async execute(request: DbRequest): Promise<DbResponse> {
      if (typeof request === "string") {
        try {
          const result = await pool.query(request);
          return {
            rows: result.rows as Record<string, unknown>[],
            rowsAffected: result.rowCount ?? 0,
          };
        } catch (error) {
          logger.error("db.query.failed", {
            sql: request,
            argsCount: 0,
            error: error instanceof Error ? error.message : String(error),
          });
          throw error;
        }
      }

      const args = request.args || [];
      const sql = toPgPlaceholders(request.sql);
      try {
        const result = await pool.query(sql, args);
        return {
          rows: result.rows as Record<string, unknown>[],
          rowsAffected: result.rowCount ?? 0,
        };
      } catch (error) {
        logger.error("db.query.failed", {
          sql,
          argsCount: args.length,
          error: error instanceof Error ? error.message : String(error),
        });
        throw error;
      }
    },
  };

  return globalRef.__dbClient;
}

export function generateId(): string | Buffer {
  if (dbIdMode === "bytea") {
    return Buffer.from(randomBytes(16));
  }
  return randomUUID();
}

export function bufferToHex(buf: unknown): string {
  if (typeof buf === "string") {
    return buf.replace(/-/g, "").toLowerCase();
  }
  return Buffer.from(buf as ArrayBuffer | Buffer | Uint8Array).toString("hex");
}

export function hexToBuffer(hex: string): Buffer {
  return Buffer.from(hex.replace(/-/g, ""), "hex");
}

export function toDbId(id: string): string | Buffer {
  if (dbIdMode === "bytea") {
    return hexToBuffer(id);
  }
  return normalizeId(id);
}

let initialized = false;

export async function initializeDatabase(): Promise<void> {
  const db = getDb();
  await db.execute("SELECT 1");
}

export async function ensureInitialized(): Promise<void> {
  if (initialized) return;
  await initializeDatabase();
  initialized = true;
}
