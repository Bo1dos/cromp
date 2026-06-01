// ---------------------------------------------------------------------------
// Permission helpers — role constants & guards
// ---------------------------------------------------------------------------

import type { OrganizationRole } from '@/types/organization';

export const ROLES = {
  OWNER: 'OWNER' as const,
  ADMIN: 'ADMIN' as const,
  MEMBER: 'MEMBER' as const,
} as const;

/**
 * Check whether `userRole` is included in `allowedRoles`.
 */
export function hasRole(
  userRole: OrganizationRole | null | undefined,
  allowedRoles: OrganizationRole[],
): boolean {
  if (!userRole) return false;
  return allowedRoles.includes(userRole);
}

/** Shortcut: is the user the OWNER? */
export function isOwner(role: OrganizationRole | null | undefined): boolean {
  return role === ROLES.OWNER;
}

/** Shortcut: is the user an ADMIN? */
export function isAdmin(role: OrganizationRole | null | undefined): boolean {
  return role === ROLES.ADMIN;
}

/** Shortcut: is the user an OWNER or ADMIN? */
export function isOwnerOrAdmin(
  role: OrganizationRole | null | undefined,
): boolean {
  return role === ROLES.OWNER || role === ROLES.ADMIN;
}
