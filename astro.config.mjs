// @ts-check
import { defineConfig } from "astro/config";

// https://astro.build/config
export default defineConfig({
    devToolbar: {
        enabled: false
    },
    build: {
        inlineStylesheets: 'auto',
    },
    compressHTML: true,
    vite: {
        build: {
            cssMinify: true,
            minify: 'esbuild',
        }
    }
})

