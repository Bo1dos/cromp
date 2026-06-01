// ---------------------------------------------------------------------------
// Schedule domain types — mirrors backend API contract
// ---------------------------------------------------------------------------

export type ScheduleStatus = 'ACTIVE' | 'PAUSED';

export interface ScheduleResponse {
  jobUuid: string;
  cronExpression: string;
  timezone: string;
  nextRunAt?: string;
  status: ScheduleStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUpdateScheduleRequest {
  cronExpression: string;
  timezone: string;
}
