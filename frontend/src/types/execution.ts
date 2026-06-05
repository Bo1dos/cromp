// ---------------------------------------------------------------------------
// Execution types — mirrors backend API contract
// ---------------------------------------------------------------------------

export type ExecutionStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELLED' | 'TIMEOUT';
export type ExecutionSource = 'SCHEDULED' | 'MANUAL';

/** Backend ExecutionResponse */
export interface ExecutionResponse {
  execUuid: string;
  jobId: number;
  jobVersionId: number;
  priority: number;
  source: string;
  finalStatus: string;
  totalAttempts: number;
  triggeredAt: string;
  scheduledAt: string;
  startedAt: string;
  finishedAt?: string;
  correlationId: string;
  createdAt: string;
  updatedAt: string;
}

/** Backend ExecutionDetailResponse = ExecutionResponse + attempts */
export interface ExecutionDetailResponse extends ExecutionResponse {
  attempts: AttemptResponse[];
}

/** Backend AttemptResponse */
export interface AttemptResponse {
  attemptUuid: string;
  attemptNumber: number;
  status: string;
  statusReason?: string;
  errorClass?: string;
  scheduledAt: string;
  claimedAt?: string;
  startedAt: string;
  finishedAt?: string;
  durationMs?: number;
  traceId: string;
  createdAt: string;
  updatedAt: string;
}

/** Backend ArtifactResponse */
export interface ArtifactResponse {
  artifactUuid: string;
  type: 'stdout' | 'stderr';
  content: string;
  sizeBytes: number;
  createdAt: string;
}

/** Backend PagedResponse */
export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
