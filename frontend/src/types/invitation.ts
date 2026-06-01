// ---------------------------------------------------------------------------
// Invitation types — mirrors backend DTOs
// ---------------------------------------------------------------------------

export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED';

export interface InvitationCreateResponse {
  invitation: InvitationResponse;
  invitationToken: string;
}

export interface InvitationResponse {
  invitationUuid: string;
  email: string;
  roleName: string;
  expiresAt: string;
  createdAt: string;
  acceptedAt?: string;
  status: InvitationStatus;
}

export interface OrganizationBrief {
  uuid: string;
  name: string;
  slug: string;
}

export interface InviteUserRequest {
  email: string;
  organizationUuid: string;
  role: string;
}
