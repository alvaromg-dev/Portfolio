// @ts-check
import { defineConfig } from "astro/config";
import node from "@astrojs/node";

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
