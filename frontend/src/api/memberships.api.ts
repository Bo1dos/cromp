// ---------------------------------------------------------------------------
// Memberships API — fetch organisation memberships for the current user
// ---------------------------------------------------------------------------

import apiClient from './client';
import type { MembershipResponse } from '@/types/organization';

/** GET /api/v1/memberships/organization/{orgUuid} */
export async function getMembershipsByOrganization(orgUuid: string): Promise<MembershipResponse[]> {
  const { data } = await apiClient.get<MembershipResponse[]>(`/memberships/organization/${orgUuid}`);
  return data;
}
