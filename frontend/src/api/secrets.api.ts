// ---------------------------------------------------------------------------
// Secrets API layer
// ---------------------------------------------------------------------------

import apiClient from './client';
import type {
  SecretResponse,
  CreateSecretRequest,
  RotateSecretRequest,
  ListSecretsParams,
  SecretVersionResponse,
} from '@/types/secret';

/** POST /api/v1/secrets */
export async function createSecret(data: CreateSecretRequest): Promise<SecretResponse> {
  const response = await apiClient.post<SecretResponse>('/secrets', data);
  return response.data;
}

/** GET /api/v1/secrets */
export async function listSecrets(params?: ListSecretsParams): Promise<SecretResponse[]> {
  const response = await apiClient.get<SecretResponse[]>('/secrets', { params });
  return response.data;
}

/** GET /api/v1/secrets/{secretUuid} */
export async function getSecret(uuid: string): Promise<SecretResponse> {
  const response = await apiClient.get<SecretResponse>(`/secrets/${uuid}`);
  return response.data;
}

/** DELETE /api/v1/secrets/{secretUuid} */
export async function deleteSecret(uuid: string): Promise<void> {
  await apiClient.delete(`/secrets/${uuid}`);
}

/** POST /api/v1/secrets/{secretUuid}/rotate */
export async function rotateSecret(
  uuid: string,
  data: RotateSecretRequest,
): Promise<SecretResponse> {
  const response = await apiClient.post<SecretResponse>(`/secrets/${uuid}/rotate`, data);
  return response.data;
}

/** GET /api/v1/secrets/{secretUuid}/versions */
export async function getSecretVersions(uuid: string): Promise<SecretVersionResponse[]> {
  const response = await apiClient.get<SecretVersionResponse[]>(`/secrets/${uuid}/versions`);
  return response.data;
}
