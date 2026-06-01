// ---------------------------------------------------------------------------
// Job domain types — mirrors backend API contract
// ---------------------------------------------------------------------------

export type JobStatus = 'ACTIVE' | 'PAUSED' | 'DISABLED';

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

export interface HttpConfig {
  url: string;
  method: HttpMethod;
  headers?: Record<string, string>;
  body?: string;
  timeoutMs: number;
}

export interface RetryPolicy {
  maxAttempts: number;
  backoffMs: number;
  backoffMultiplier: number;
}

export interface JobResponse {
  uuid: string;
  name: string;
  description: string;
  status: JobStatus;
  httpConfig: HttpConfig;
  retryPolicy: RetryPolicy;
  cronExpression?: string;
  timezone?: string;
  nextRunAt?: string;
  createdAt: string;
  updatedAt: string;
  lastExecutionAt?: string;
  lastExecutionStatus?: string;
}

export interface CreateJobRequest {
  name: string;
  description: string;
  httpConfig: HttpConfig;
  retryPolicy: RetryPolicy;
  cronExpression?: string;
  timezone?: string;
  secretIds?: string[];
}

export interface UpdateJobRequest {
  name?: string;
  description?: string;
  httpConfig?: HttpConfig;
  retryPolicy?: RetryPolicy;
  cronExpression?: string;
  timezone?: string;
  secretIds?: string[];
}

export interface ToggleStatusRequest {
  status: 'ENABLED' | 'DISABLED';
}

export interface ListJobsParams {
  status?: JobStatus;
  limit?: number;
  offset?: number;
}

// ---------------------------------------------------------------------------
// Job versioning
// ---------------------------------------------------------------------------

export interface JobVersionResponse {
  version: number;
  name: string;
  description: string;
  httpConfig: HttpConfig;
  retryPolicy: RetryPolicy;
  cronExpression?: string;
  timezone?: string;
  secretIds?: string[];
  createdAt: string;
  createdBy: string;
}

export interface JobVersionCompareResponse {
  left: JobVersionResponse;
  right: JobVersionResponse;
  diff: string;
}
