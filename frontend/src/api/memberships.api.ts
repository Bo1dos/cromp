// ---------------------------------------------------------------------------
// Memberships API — fetch organisation memberships for the current user
// ---------------------------------------------------------------------------

import apiClient from './client';
import type { MembershipResponse } from '@/types/organization';

/** GET /api/v1/memberships/organization/{orgUuid} */
export async function getMembersByOrg(orgUuid: string): Promise<MembershipResponse[]> {
  const { data } = await apiClient.get<MembershipResponse[]>(`/memberships/organization/${orgUuid}`);
  return data;
}

/** PUT /api/v1/memberships/{membershipUuid}/role */
export async function changeMemberRole(
  membershipUuid: string,
  role: string,
): Promise<MembershipResponse> {
  const { data } = await apiClient.put<MembershipResponse>(
    `/memberships/${membershipUuid}/role`,
    { role },
  );
  return data;
}

/** DELETE /api/v1/memberships/{membershipUuid} → 204 */
export async function removeMember(membershipUuid: string): Promise<void> {
  await apiClient.delete(`/memberships/${membershipUuid}`);
}
