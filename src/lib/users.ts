import { getDb, generateId, bufferToHex, toDbId } from "./db";
import { hashPassword, invalidateUserSessions } from "./auth";
import type { ManagedUserRow } from "./types";

const MIN_PASSWORD_LENGTH = 5;
const USERNAME_PATTERN = /^[A-Za-z0-9._-]{3,64}$/;
const NAME_PATTERN = /^[\p{L}\s'-]{1,80}$/u;

export async function getUsers(): Promise<ManagedUserRow[]> {
  const db = getDb();
  const adminUsername = process.env.ADMIN_USERNAME || "admin";

  const result = await db.execute(
    "SELECT id, name FROM users ORDER BY name ASC"
  );

  const users: ManagedUserRow[] = [];
  for (const row of result.rows) {
    const rolesResult = await db.execute({
      sql: `SELECT r.code FROM roles r
            INNER JOIN users_roles ur ON ur.role_id = r.id
            WHERE ur.user_id = ?`,
      args: [row.id],
    });

    users.push({
      id: bufferToHex(row.id as ArrayBuffer),
      name: row.name as string,
      roleCodes: rolesResult.rows.map((r) => r.code as string),
      isAdmin: (row.name as string) === adminUsername,
    });
  }

  return users;
}

export async function createUser(
  username: string,
  password: string,
  _givenNames: string,
  _familyNames: string,
  roleCodes: string[]
): Promise<{ success: boolean; error?: string }> {
  const adminUsername = process.env.ADMIN_USERNAME || "admin";

  if (!username || !USERNAME_PATTERN.test(username)) {
    return { success: false, error: "Invalid username (3-64 chars, A-Za-z0-9._-)" };
  }

  if (username === adminUsername) {
    return { success: false, error: "Username is reserved" };
  }

  if (!password || password.length < MIN_PASSWORD_LENGTH) {
    return { success: false, error: `Password must be at least ${MIN_PASSWORD_LENGTH} characters` };
  }

  const db = getDb();

  const existing = await db.execute({
    sql: "SELECT id FROM users WHERE name = ?",
    args: [username],
  });
  if (existing.rows.length > 0) {
    return { success: false, error: "Username already exists" };
  }

  const userId = generateId();
  const hashedPassword = hashPassword(password);

  await db.execute({
    sql: `INSERT INTO users (id, name, password)
          VALUES (?, ?, ?)`,
    args: [userId, username, hashedPassword],
  });

  // Assign roles (default to user/editor role if none specified)
  const codes = roleCodes.length > 0 ? roleCodes : ["admin"];
  for (const code of codes) {
    const role = await db.execute({
      sql: "SELECT id FROM roles WHERE code = ?",
      args: [code],
    });
    if (role.rows.length > 0) {
      await db.execute({
        sql: "INSERT INTO users_roles (user_id, role_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
        args: [userId, role.rows[0].id],
      });
    }
  }

  return { success: true };
}

export async function updateUser(
  userId: string,
  username: string,
  _familyNames: string,
  roleCodes: string[],
  newPassword?: string
): Promise<{ success: boolean; error?: string }> {
  if (!username || !USERNAME_PATTERN.test(username)) {
    return { success: false, error: "Invalid username (3-64 chars, A-Za-z0-9._-)" };
  }

  if (newPassword && newPassword.length < MIN_PASSWORD_LENGTH) {
    return { success: false, error: `Password must be at least ${MIN_PASSWORD_LENGTH} characters` };
  }

  const db = getDb();
  const userDbId = toDbId(userId);

  const user = await db.execute({
    sql: "SELECT name FROM users WHERE id = ?",
    args: [userDbId],
  });
  if (user.rows.length === 0) {
    return { success: false, error: "User not found" };
  }

  const adminUsername = process.env.ADMIN_USERNAME || "admin";
  const isAdmin = (user.rows[0].name as string) === adminUsername;

  const updatedName = username.trim();

  if (newPassword) {
    const hashedPassword = hashPassword(newPassword);
    await db.execute({
      sql: "UPDATE users SET name = ?, password = ? WHERE id = ?",
      args: [updatedName, hashedPassword, userDbId],
    });
    invalidateUserSessions(userId);
  } else {
    await db.execute({
      sql: "UPDATE users SET name = ? WHERE id = ?",
      args: [updatedName, userDbId],
    });
  }

  // Update roles (skip for admin user)
  if (!isAdmin) {
    await db.execute({
      sql: "DELETE FROM users_roles WHERE user_id = ?",
      args: [userDbId],
    });

    for (const code of roleCodes) {
      const role = await db.execute({
        sql: "SELECT id FROM roles WHERE code = ?",
        args: [code],
      });
      if (role.rows.length > 0) {
        await db.execute({
          sql: "INSERT INTO users_roles (user_id, role_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
          args: [userDbId, role.rows[0].id],
        });
      }
    }
  }

  return { success: true };
}

export async function deleteUser(
  userId: string
): Promise<{ success: boolean; error?: string }> {
  const db = getDb();
  const userDbId = toDbId(userId);

  const user = await db.execute({
    sql: "SELECT name FROM users WHERE id = ?",
    args: [userDbId],
  });
  if (user.rows.length === 0) {
    return { success: false, error: "User not found" };
  }

  const adminUsername = process.env.ADMIN_USERNAME || "admin";
  if ((user.rows[0].name as string) === adminUsername) {
    return { success: false, error: "Cannot delete the admin user" };
  }

  await db.execute({
    sql: "DELETE FROM users_roles WHERE user_id = ?",
    args: [userDbId],
  });

  await db.execute({
    sql: "DELETE FROM users WHERE id = ?",
    args: [userDbId],
  });

  return { success: true };
}

export async function getAvailableRoles(): Promise<string[]> {
  const db = getDb();
  const result = await db.execute("SELECT code FROM roles ORDER BY code");
  return result.rows.map((r) => r.code as string);
}
