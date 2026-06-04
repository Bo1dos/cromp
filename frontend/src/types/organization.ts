// ---------------------------------------------------------------------------
// Organization & Membership types — mirrors backend DTOs
// ---------------------------------------------------------------------------

export interface OrganizationResponse {
  id: number;
  orgUuid: string;
  name: string;
  settings?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
  deletedAt?: string | null;
}

export type OrganizationRole = 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';

export interface MembershipResponse {
  membershipUuid: string;
  roleName: string;
  organization: OrganizationResponse;
  userUuid: string;
  userName: string;
  userEmail: string;
  joinedAt?: string;
  updatedAt?: string;
}

export interface UserBrief {
  uuid: string;
  email: string;
  name: string;
  avatarUrl?: string | null;
}
