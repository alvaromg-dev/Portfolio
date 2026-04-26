import { randomUUID } from "node:crypto";

const CHALLENGE_TTL_MS = 2 * 60 * 1000;
const MIN_CHALLENGE_AGE_MS = 900;
const MAX_CHALLENGES = 2000;

type Challenge = {
  createdAt: number;
};

type ClientSignals = {
  language?: unknown;
  timezone?: unknown;
  screen?: unknown;
  visibilityState?: unknown;
  webdriver?: unknown;
};

const globalRef = globalThis as unknown as {
  __telemetryChallenges?: Map<string, Challenge>;
};

function getChallengeStore(): Map<string, Challenge> {
  if (!globalRef.__telemetryChallenges) {
    globalRef.__telemetryChallenges = new Map();
  }
  return globalRef.__telemetryChallenges;
}

function pruneChallenges(store: Map<string, Challenge>): void {
  const now = Date.now();
  for (const [token, challenge] of store) {
    if (now - challenge.createdAt > CHALLENGE_TTL_MS) {
      store.delete(token);
    }
  }

  while (store.size > MAX_CHALLENGES) {
    const oldestToken = store.keys().next().value as string | undefined;
    if (!oldestToken) return;
    store.delete(oldestToken);
  }
}

export function createTelemetryChallenge(): string {
  const store = getChallengeStore();
  pruneChallenges(store);

  const token = randomUUID();
  store.set(token, { createdAt: Date.now() });
  return token;
}

function hasText(value: unknown): boolean {
  return typeof value === "string" && value.trim().length > 0;
}

export function consumeTelemetryChallenge(token: unknown, signals: ClientSignals): boolean {
  if (!hasText(token)) return false;

  const store = getChallengeStore();
  pruneChallenges(store);

  const challenge = store.get(token);
  if (!challenge) return false;
  store.delete(token);

  const age = Date.now() - challenge.createdAt;
  if (age < MIN_CHALLENGE_AGE_MS || age > CHALLENGE_TTL_MS) return false;
  if (signals.webdriver === true) return false;
  if (!hasText(signals.language)) return false;
  if (!hasText(signals.timezone)) return false;
  if (!hasText(signals.screen)) return false;
  if (!hasText(signals.visibilityState)) return false;

  return true;
}
