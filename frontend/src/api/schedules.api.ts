// ---------------------------------------------------------------------------
// Schedules API layer
// ---------------------------------------------------------------------------

import apiClient from './client';
import type { ScheduleResponse, CreateUpdateScheduleRequest } from '@/types/schedule';

export async function getSchedule(jobUuid: string): Promise<ScheduleResponse> {
  const response = await apiClient.get<ScheduleResponse>(`/jobs/${jobUuid}/schedule`);
  return response.data;
}

export async function createOrUpdateSchedule(
  jobUuid: string,
  data: CreateUpdateScheduleRequest,
): Promise<ScheduleResponse> {
  const response = await apiClient.put<ScheduleResponse>(`/jobs/${jobUuid}/schedule`, data);
  return response.data;
}

export async function deleteSchedule(jobUuid: string): Promise<void> {
  await apiClient.delete(`/jobs/${jobUuid}/schedule`);
}
