// ---------------------------------------------------------------------------
// Schedule domain types — mirrors backend API contract
// ---------------------------------------------------------------------------

export type ScheduleStatus = 'ACTIVE' | 'PAUSED';

/** Backend ScheduleResponse */
export interface ScheduleResponse {
  cronExpression: string;
  timezone: string;
  rules?: string;
  nextRunAt?: string;
  status: ScheduleStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUpdateScheduleRequest {
  cronExpression: string;
  timezone: string;
}
