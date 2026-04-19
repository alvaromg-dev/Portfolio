import { config } from "dotenv";
import { existsSync } from "node:fs";
import { resolve } from "node:path";

const profile = process.argv[2] || "pro";
const validProfiles = new Set(["dev", "pre", "pro"]);

if (!validProfiles.has(profile)) {
  console.error(`Invalid environment profile: ${profile}`);
  console.error("Use one of: dev, pre, pro");
  process.exit(1);
}

const envFile = resolve(process.cwd(), `.env.${profile}`);

if (!existsSync(envFile)) {
  console.error(`Environment file not found: ${envFile}`);
  process.exit(1);
}

config({ path: envFile, override: true });

await import(resolve(process.cwd(), "dist/server/entry.mjs"));