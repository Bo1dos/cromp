// ---------------------------------------------------------------------------
// useSecrets — TanStack Query hooks for the Secrets domain
// ---------------------------------------------------------------------------

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notification } from 'antd';
import type {
  SecretResponse,
  CreateSecretRequest,
  RotateSecretRequest,
  ListSecretsParams,
  SecretVersionResponse,
} from '@/types/secret';
import * as secretsApi from '@/api/secrets.api';
import { QUERY_KEYS } from '@/utils/constants';

// ---------------------------------------------------------------------------
// List
// ---------------------------------------------------------------------------
interface UseSecretsListFilters {
  scope?: string;
  search?: string;
  page: number;
  pageSize: number;
}

export function useSecretsList(filters: UseSecretsListFilters) {
  const params: ListSecretsParams = {
    scope: filters.scope as ListSecretsParams['scope'] | undefined,
    search: filters.search || undefined,
    limit: filters.pageSize,
    offset: (filters.page - 1) * filters.pageSize,
  };

  return useQuery<SecretResponse[], Error>({
    queryKey: [QUERY_KEYS.SECRETS, filters],
    queryFn: () => secretsApi.listSecrets(params),
    staleTime: 120_000, // 2 min
    placeholderData: (previousData) => previousData,
  });
}

// ---------------------------------------------------------------------------
// Detail
// ---------------------------------------------------------------------------
export function useSecretDetail(uuid: string) {
  return useQuery<SecretResponse, Error>({
    queryKey: [QUERY_KEYS.SECRET, uuid],
    queryFn: () => secretsApi.getSecret(uuid),
    staleTime: 120_000, // 2 min
    enabled: Boolean(uuid),
  });
}

// ---------------------------------------------------------------------------
// Create
// ---------------------------------------------------------------------------
export function useCreateSecret() {
  const queryClient = useQueryClient();

  return useMutation<SecretResponse, Error, CreateSecretRequest>({
    mutationFn: (data) => secretsApi.createSecret(data),
    onSuccess: (secret) => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.SECRETS] });
      notification.success({
        message: 'Secret created',
        description: `Secret "${secret.name}" has been created.`,
      });
    },
    onError: (error) => {
      notification.error({
        message: 'Failed to create secret',
        description: error.message,
      });
    },
  });
}

// ---------------------------------------------------------------------------
// Delete
// ---------------------------------------------------------------------------
export function useDeleteSecret() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (uuid) => secretsApi.deleteSecret(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.SECRETS] });
      notification.success({ message: 'Secret deleted' });
    },
    onError: (error) => {
      notification.error({
        message: 'Failed to delete secret',
        description: error.message,
      });
    },
  });
}

// ---------------------------------------------------------------------------
// Rotate
// ---------------------------------------------------------------------------
export function useRotateSecret(uuid: string) {
  const queryClient = useQueryClient();

  return useMutation<SecretResponse, Error, RotateSecretRequest>({
    mutationFn: (data) => secretsApi.rotateSecret(uuid, data),
    onSuccess: (secret) => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.SECRET, uuid] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.SECRET_VERSIONS, uuid] });
      notification.success({
        message: 'Secret rotated',
        description: `Secret "${secret.name}" has been rotated.`,
      });
    },
    onError: (error) => {
      notification.error({
        message: 'Failed to rotate secret',
        description: error.message,
      });
    },
  });
}

// ---------------------------------------------------------------------------
// Versions list
// ---------------------------------------------------------------------------
export function useSecretVersions(uuid: string) {
  return useQuery<SecretVersionResponse[], Error>({
    queryKey: [QUERY_KEYS.SECRET_VERSIONS, uuid],
    queryFn: () => secretsApi.getSecretVersions(uuid),
    staleTime: 120_000,
    enabled: Boolean(uuid),
  });
}
