// ---------------------------------------------------------------------------
// Execution types for CaaS
// ---------------------------------------------------------------------------

export type ExecutionStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELLED' | 'TIMEOUT';
export type ExecutionSource = 'SCHEDULED' | 'MANUAL';

export interface ExecutionResponse {
  uuid: string;
  jobUuid: string;
  jobName: string;
  status: ExecutionStatus;
  source: ExecutionSource;
  startedAt: string;
  completedAt?: string;
  durationMs?: number;
  attemptCount: number;
}

export interface ExecutionDetailResponse extends ExecutionResponse {
  httpConfig: {
    url: string;
    method: string;
    headers?: Record<string, string>;
    body?: string;
    timeoutMs: number;
  };
  retryPolicy: {
    maxAttempts: number;
    backoffMs: number;
    backoffMultiplier: number;
  };
  payload?: object;
}

export interface AttemptResponse {
  uuid: string;
  attemptNumber: number;
  status: ExecutionStatus;
  startedAt: string;
  completedAt?: string;
  durationMs?: number;
  errorMessage?: string;
  statusCode?: number;
}

export interface ArtifactResponse {
  uuid: string;
  type: 'stdout' | 'stderr';
  content: string;
  sizeBytes: number;
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
