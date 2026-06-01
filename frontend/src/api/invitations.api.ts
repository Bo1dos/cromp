// ---------------------------------------------------------------------------
// Invitations API layer
// ---------------------------------------------------------------------------

import apiClient from './client';
import type {
  InvitationCreateResponse,
  InvitationResponse,
  InviteUserRequest,
} from '@/types/invitation';

/** POST /api/v1/invitations → 201 */
export async function inviteUser(data: InviteUserRequest): Promise<InvitationCreateResponse> {
  const response = await apiClient.post<InvitationCreateResponse>('/invitations', data);
  return response.data;
}

/** POST /api/v1/invitations/accept */
export async function acceptInvitation(token: string): Promise<InvitationResponse> {
  const response = await apiClient.post<InvitationResponse>('/invitations/accept', { token });
  return response.data;
}

/** POST /api/v1/invitations/reject → 204 */
export async function rejectInvitation(token: string): Promise<void> {
  await apiClient.post('/invitations/reject', { token });
}

/** POST /api/v1/invitations/revoke → 200 */
export async function revokeInvitation(invitationUuid: string): Promise<void> {
  await apiClient.post('/invitations/revoke', { invitationUuid });
}
