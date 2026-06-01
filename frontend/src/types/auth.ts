// ---------------------------------------------------------------------------
// Auth response types — mirrors backend AuthResponse DTO
// ---------------------------------------------------------------------------

import type { UserResponse } from './user';

export interface AuthResponse {
  /** The authenticated user profile */
  user: UserResponse;
  /** When true the user must pick an organisation before proceeding */
  requiresOrganizationSelection?: boolean;
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
