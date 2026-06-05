// ---------------------------------------------------------------------------
// Jobs API layer
// ---------------------------------------------------------------------------

import apiClient from './client';
import type {
  JobResponse,
  CreateJobRequest,
  UpdateJobRequest,
  ToggleStatusRequest,
  ListJobsParams,
  JobVersionResponse,
  JobVersionCompareResponse,
  TriggerResponse,
} from '@/types/job';

export async function createJob(data: CreateJobRequest): Promise<JobResponse> {
  const response = await apiClient.post<JobResponse>('/jobs', data);
  return response.data;
}

export async function listJobs(params?: ListJobsParams): Promise<JobResponse[]> {
  const response = await apiClient.get<JobResponse[]>('/jobs', { params });
  return response.data;
}

export async function getJob(uuid: string): Promise<JobResponse> {
  const response = await apiClient.get<JobResponse>(`/jobs/${uuid}`);
  return response.data;
}

export async function updateJob(uuid: string, data: UpdateJobRequest): Promise<JobResponse> {
  const response = await apiClient.put<JobResponse>(`/jobs/${uuid}`, data);
  return response.data;
}

export async function deleteJob(uuid: string): Promise<void> {
  await apiClient.delete(`/jobs/${uuid}`);
}

export async function toggleJobStatus(
  uuid: string,
  status: ToggleStatusRequest['status'],
): Promise<JobResponse> {
  const response = await apiClient.patch<JobResponse>(`/jobs/${uuid}/status`, { status });
  return response.data;
}

export async function triggerJob(uuid: string): Promise<TriggerResponse> {
  const response = await apiClient.post<TriggerResponse>(`/jobs/${uuid}/trigger`, {});
  return response.data;
}

// ---------------------------------------------------------------------------
// Job versioning
// ---------------------------------------------------------------------------

export async function getJobVersions(uuid: string): Promise<JobVersionResponse[]> {
  const response = await apiClient.get<JobVersionResponse[]>(`/jobs/${uuid}/versions`);
  return response.data;
}

export async function getJobVersion(uuid: string, version: number): Promise<JobVersionResponse> {
  const response = await apiClient.get<JobVersionResponse>(`/jobs/${uuid}/versions/${version}`);
  return response.data;
}

export async function compareVersions(
  uuid: string,
  v1: number,
  v2: number,
): Promise<JobVersionCompareResponse> {
  const response = await apiClient.get<JobVersionCompareResponse>(
    `/jobs/${uuid}/versions/compare`,
    { params: { v1, v2 } },
  );
  return response.data;
}

export async function revertJob(uuid: string, targetVersion: number): Promise<JobResponse> {
  const response = await apiClient.post<JobResponse>(`/jobs/${uuid}/revert`, {
    targetVersion,
  });
  return response.data;
}

export async function getCurrentVersion(uuid: string): Promise<JobVersionResponse> {
  const response = await apiClient.get<JobVersionResponse>(`/jobs/${uuid}/current-version`);
  return response.data;
}
