export function normalizeRole(role: string): string {
  return String(role || "").trim().toUpperCase();
}

export function hasRole(roles: string[] | undefined, acceptedRoles: string[]): boolean {
  if (!roles || roles.length === 0) return false;
  const normalizedRoles = roles.map(normalizeRole);
  return acceptedRoles.map(normalizeRole).some((role) => normalizedRoles.includes(role));
}

export function canEditPortfolio(roles: string[] | undefined): boolean {
  return hasRole(roles, ["ADMIN"]);
}

export function canAdminister(roles: string[] | undefined): boolean {
  return hasRole(roles, ["ADMIN"]);
}
