// ---------------------------------------------------------------------------
// Users API — fetch current user profile
// ---------------------------------------------------------------------------

import apiClient from './client';
import type { UserResponse } from '@/types/user';

/** GET /api/v1/users/{uuid} */
export async function getMe(userUuid: string): Promise<UserResponse> {
  const { data } = await apiClient.get<UserResponse>(`/users/${userUuid}`);
  return data;
}
