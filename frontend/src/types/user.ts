// ---------------------------------------------------------------------------
// User types — mirrors backend UserResponse DTO
// ---------------------------------------------------------------------------

export interface UserResponse {
  uuid: string;
  email: string;
  name: string;
  avatarUrl?: string | null;
}
