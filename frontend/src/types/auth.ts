// ---------------------------------------------------------------------------
// Auth response types — mirrors backend AuthResponse DTO
// ---------------------------------------------------------------------------

import type { UserResponse } from './user';

export interface AuthResponse {
  /** Token type (Bearer) */
  tokenType: string;
  /** JWT access token */
  accessToken: string;
  /** The authenticated user profile */
  user: UserResponse;
  /** Available organisations (may be empty on login) */
  organizations?: Array<{ uuid: string; name: string; slug: string }>;
  /** Active organisation (null on login, filled after selection) */
  activeOrganization?: { uuid: string; name: string; slug: string } | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
}

export interface SelectOrganizationRequest {
  organizationId: string;
}
