// ---------------------------------------------------------------------------
// Secret domain types — mirrors backend API contract
// ---------------------------------------------------------------------------

export type SecretScope = 'JOB' | 'ORGANIZATION';

export interface SecretResponse {
  uuid: string;
  name: string;
  description?: string;
  scope: SecretScope;
  versionCount: number;
  createdAt: string;
  updatedAt: string;
}

/** Version metadata — NEVER contains the secret value */
export interface SecretVersionResponse {
  version: number;
  createdAt: string;
  createdBy: string;
}

export interface CreateSecretRequest {
  name: string;
  /** The secret value — transmitted once, NEVER stored in frontend state */
  value: string;
  description?: string;
  scope?: SecretScope;
}

export interface RotateSecretRequest {
  /** The new secret value — transmitted once, NEVER stored in frontend state */
  value: string;
}

export interface ListSecretsParams {
  scope?: SecretScope;
  search?: string;
  limit?: number;
  offset?: number;
}
