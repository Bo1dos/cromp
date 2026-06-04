// ---------------------------------------------------------------------------
// useOrganizations — TanStack Query hook for user memberships
// ---------------------------------------------------------------------------

import { useQuery } from '@tanstack/react-query';
import { getMembersByUser } from '@/api/memberships.api';
import type { MembershipResponse } from '@/types/organization';

export function useOrganizations(userId: string | undefined) {
  return useQuery<MembershipResponse[], Error>({
    queryKey: ['organizations', userId],
    queryFn: () => {
      if (!userId) return Promise.resolve([]);
      return getMembersByUser(userId);
    },
    enabled: Boolean(userId),
    staleTime: 60_000,
  });
}
