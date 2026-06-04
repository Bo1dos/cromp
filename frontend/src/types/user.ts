// ---------------------------------------------------------------------------
// User types — mirrors backend UserResponse DTO
// ---------------------------------------------------------------------------

export interface UserResponse {
  id: number;
  userUuid: string;
  email: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  displayName: string;
  profile?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
  deletedAt?: string | null;
}
