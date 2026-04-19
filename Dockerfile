FROM oven/bun:1 AS base
WORKDIR /app

FROM base AS deps
COPY package.json bun.lock* ./
RUN bun install --frozen-lockfile 2>/dev/null || bun install

FROM base AS build
ARG PROFILE=pro
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN bun run build:${PROFILE}

FROM base AS runtime
RUN groupadd -r appgroup && useradd -r -g appgroup -s /bin/false appuser
COPY --from=deps --chown=appuser:appgroup /app/node_modules ./node_modules
COPY --from=build --chown=appuser:appgroup /app/dist ./dist
COPY --from=build --chown=appuser:appgroup /app/scripts ./scripts
COPY --from=build --chown=appuser:appgroup /app/package.json ./
USER appuser
EXPOSE 4173
