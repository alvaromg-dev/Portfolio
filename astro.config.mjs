// @ts-check
import { defineConfig } from "astro/config";
import node from "@astrojs/node";
import { config as loadEnv } from "dotenv";
import { existsSync } from "node:fs";
import { resolve } from "node:path";

function getModeFromArgs() {
    const modeIndex = process.argv.indexOf("--mode");
    if (modeIndex >= 0 && process.argv[modeIndex + 1]) {
        return process.argv[modeIndex + 1];
    }

    const modeArg = process.argv.find((arg) => arg.startsWith("--mode="));
    return modeArg?.split("=")[1] || "dev";
}

function loadProfileEnv(mode = getModeFromArgs()) {
    const profile = ["dev", "pre", "pro"].includes(mode) ? mode : "dev";
    const envFile = resolve(process.cwd(), `.env.${profile}`);

    if (existsSync(envFile)) {
        loadEnv({ path: envFile, override: true, quiet: true });
    }
}

loadProfileEnv();

const allowedHosts = (process.env.ALLOWED_HOSTS || "localhost,127.0.0.1,alvaromg.com,www.alvaromg.com")
    .split(",")
    .map((host) => host.trim())
    .filter(Boolean);

// https://astro.build/config
export default defineConfig({
    output: "server",
    adapter: node({ mode: "standalone" }),
    devToolbar: {
        enabled: false
    },
    build: {
        inlineStylesheets: 'always',
        assets: '_astro'
    },
    compressHTML: true,
    vite: {
        build: {
            cssMinify: true,
            minify: 'esbuild',
            cssCodeSplit: false,
            rollupOptions: {
                output: {
                    manualChunks: undefined,
                }
            }
        }
    },
    server: {
        port: Number(process.env.PORT) || 4173,
        host: process.env.HOST || '0.0.0.0',
        allowedHosts,
    },
})
