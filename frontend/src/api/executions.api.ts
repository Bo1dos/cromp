// ---------------------------------------------------------------------------
// Executions API layer
// ---------------------------------------------------------------------------

import apiClient from './client';
import type {
  ExecutionResponse,
  ExecutionDetailResponse,
  AttemptResponse,
  ArtifactResponse,
  PagedResponse,
} from '@/types/execution';

export interface ListExecutionsParams {
  jobId?: string;
  status?: string;
  source?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}

export async function listExecutions(
  orgId: string,
  params?: ListExecutionsParams,
): Promise<PagedResponse<ExecutionResponse>> {
  const response = await apiClient.get<PagedResponse<ExecutionResponse>>(
    `/organizations/${orgId}/executions`,
    { params },
  );
  return response.data;
}

export async function getExecution(
  orgId: string,
  executionId: string,
): Promise<ExecutionDetailResponse> {
  const response = await apiClient.get<ExecutionDetailResponse>(
    `/organizations/${orgId}/executions/${executionId}`,
  );
  return response.data;
}

export async function cancelExecution(
  orgId: string,
  executionId: string,
): Promise<void> {
  await apiClient.post(`/organizations/${orgId}/executions/${executionId}/cancel`);
}

export async function getAttempts(
  orgId: string,
  executionId: string,
): Promise<AttemptResponse[]> {
  const response = await apiClient.get<AttemptResponse[]>(
    `/organizations/${orgId}/executions/${executionId}/attempts`,
  );
  return response.data;
}

export async function getArtifacts(
  orgId: string,
  executionId: string,
): Promise<ArtifactResponse[]> {
  const response = await apiClient.get<ArtifactResponse[]>(
    `/organizations/${orgId}/executions/${executionId}/artifacts`,
  );
  return response.data;
}

export function getArtifactDownloadUrl(
  orgId: string,
  executionId: string,
  artifactId: number,
): string {
  // apiClient.defaults.baseURL === '/api/v1', gives relative URL
  return `/api/v1/organizations/${orgId}/executions/${executionId}/artifacts/${artifactId}/download`;
}
