import bcrypt from "bcryptjs";
const { compareSync, hashSync } = bcrypt;
import { randomUUID } from "node:crypto";
import { getDb, bufferToHex } from "./db";

interface Session {
  id: string;
  name: string;
  roles: string[];
  createdAt: number;
}

const sessions = new Map<string, Session>();
const SESSION_MAX_AGE = 24 * 60 * 60; // 24 hours in seconds

// Login throttle
const loginAttempts = new Map<string, { count: number; blockedUntil: number }>();
const MAX_ATTEMPTS = 5;
const BLOCK_DURATION = 5 * 60 * 1000; // 5 minutes

export function isLoginBlocked(key: string): boolean {
  const attempt = loginAttempts.get(key);
  if (!attempt) return false;
  if (Date.now() > attempt.blockedUntil) {
    loginAttempts.delete(key);
    return false;
  }
  return attempt.count >= MAX_ATTEMPTS;
}

export function registerLoginFailure(key: string): void {
  const attempt = loginAttempts.get(key) || { count: 0, blockedUntil: 0 };
  attempt.count++;
  if (attempt.count >= MAX_ATTEMPTS) {
    attempt.blockedUntil = Date.now() + BLOCK_DURATION;
  }
  loginAttempts.set(key, attempt);
}

export function registerLoginSuccess(key: string): void {
  loginAttempts.delete(key);
}

export async function authenticateUser(
  name: string,
  password: string
): Promise<Session | null> {
  const db = getDb();

  const result = await db.execute({
    sql: `SELECT u.id, u.name, u.password
          FROM users u WHERE u.name = ?`,
    args: [name],
  });

  if (result.rows.length === 0) return null;

  const user = result.rows[0];

  const storedPassword = user.password as string;
  if (!compareSync(password, storedPassword)) return null;

  // Get roles
  const rolesResult = await db.execute({
    sql: `SELECT r.code FROM roles r
          INNER JOIN users_roles ur ON ur.role_id = r.id
          WHERE ur.user_id = ?`,
    args: [user.id],
  });

  const roles = rolesResult.rows
    .map((r) => String(r.code || "").trim())
    .filter(Boolean)
    .map((code) => code.toUpperCase());

  const sessionId = randomUUID();
  const session: Session = {
    id: bufferToHex(user.id as ArrayBuffer),
    name: user.name as string,
    roles,
    createdAt: Date.now(),
  };

  sessions.set(sessionId, session);
  return { ...session, id: sessionId };
}

export function getSession(sessionId: string): Session | null {
  const session = sessions.get(sessionId);
  if (!session) return null;

  // Check expiration
  const elapsed = (Date.now() - session.createdAt) / 1000;
  if (elapsed > SESSION_MAX_AGE) {
    sessions.delete(sessionId);
    return null;
  }

  return session;
}

export function destroySession(sessionId: string): void {
  sessions.delete(sessionId);
}

export function hashPassword(password: string): string {
  return hashSync(password, 12);
}

export function invalidateUserSessions(userId: string): void {
  for (const [sessionId, session] of sessions.entries()) {
    if (session.id === userId) {
      sessions.delete(sessionId);
    }
  }
}

export function getSessionMaxAge(): number {
  return SESSION_MAX_AGE;
}

export function isSecureCookie(): boolean {
  return process.env.SESSION_COOKIE_SECURE === "true";
}
