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

export async function triggerJob(uuid: string): Promise<JobResponse> {
  const response = await apiClient.post<JobResponse>(`/jobs/${uuid}/trigger`);
  return response.data;
}
