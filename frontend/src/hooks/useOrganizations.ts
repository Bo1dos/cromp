// ---------------------------------------------------------------------------
// useOrganizations — TanStack Query hook for user memberships
// ---------------------------------------------------------------------------

import { useQuery } from '@tanstack/react-query';
import { getMembershipsByOrganization } from '@/api/memberships.api';
import type { MembershipResponse } from '@/types/organization';

/**
 * Fetches all memberships (organisations) for the given user.
 *
 * NOTE: The backend endpoint GET /api/v1/memberships/organization/{orgUuid}
 *       returns memberships for a single organisation. If a user-scoped
 *       endpoint becomes available (e.g. GET /api/v1/memberships?userUuid=…),
 *       update the query function accordingly.
 */
export function useOrganizations(userId: string | undefined) {
  return useQuery<MembershipResponse[], Error>({
    queryKey: ['organizations', userId],
    queryFn: async () => {
      if (!userId) return [];
      // TODO: replace with a user-scoped endpoint when available.
      // For now we fetch memberships for a "personal" org derived from userId.
      return getMembershipsByOrganization(userId);
    },
    enabled: Boolean(userId),
    staleTime: 60_000,
  });
}
