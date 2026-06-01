// ---------------------------------------------------------------------------
// Auth API — login, register, select-organization
// ---------------------------------------------------------------------------

import apiClient from './client';
import type { AuthResponse, LoginRequest, RegisterRequest, SelectOrganizationRequest } from '@/types/auth';

/** POST /api/v1/auth/login */
export async function login(request: LoginRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', request);
  return data;
}

/** POST /api/v1/auth/register */
export async function register(request: RegisterRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/register', request);
  return data;
}

/** POST /api/v1/auth/select-organization */
export async function selectOrganization(request: SelectOrganizationRequest): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/select-organization', request);
  return data;
}
