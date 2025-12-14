// @ts-check
import { defineConfig } from "astro/config";

// https://astro.build/config
export default defineConfig({
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
        allowedHosts: ['alvaromg.com', 'www.alvaromg.com'],
    },
})
