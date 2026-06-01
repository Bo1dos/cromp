// ---------------------------------------------------------------------------
// Organization & Membership types — mirrors backend DTOs
// ---------------------------------------------------------------------------

export interface OrganizationResponse {
  uuid: string;
  name: string;
  slug: string;
}

export type OrganizationRole = 'OWNER' | 'ADMIN' | 'MEMBER';

export interface MembershipResponse {
  uuid: string;
  userUuid: string;
  organization: OrganizationResponse;
  role: OrganizationRole;
}
