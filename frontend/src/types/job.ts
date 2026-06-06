// ---------------------------------------------------------------------------
// Job domain types — mirrors backend API contract
// ---------------------------------------------------------------------------

export type JobStatus = 'ACTIVE' | 'DISABLED' | 'ARCHIVED';

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'HEAD' | 'OPTIONS';

// =========================================================================
// Response types (backend → frontend)
// =========================================================================

/** Backend JobResponse */
export interface JobResponse {
  jobUuid: string;
  name: string;
  description: string;
  status: JobStatus;
  queueName: string;
  priority: number;
  createdBy: number;
  createdAt: string;
  updatedAt: string;
  currentConfig: JobConfigResponse;
  currentVersion: number;
  versionCount: number;
  hasSchedule: boolean;
}

/** Backend JobConfigResponse */
export interface JobConfigResponse {
  target: JobTargetResponse;
  retryPolicy: RetryPolicyResponse;
  timeoutMs: number;
  secrets: SecretRefResponse[];
}

/** Backend JobTargetResponse */
export interface JobTargetResponse {
  type: string;
  url: string;
  method: HttpMethod;
  headers?: Record<string, string>;
  body?: string;
}

/** Backend RetryPolicyResponse */
export interface RetryPolicyResponse {
  maxAttempts: number;
  backoffMs: number;
  backoffMultiplier: number;
  retryableErrors: string[];
}

/** Backend SecretRefResponse */
export interface SecretRefResponse {
  secretId: string;
  envName: string;
}

// =========================================================================
// Request types (frontend → backend)
// =========================================================================

export interface CreateJobRequest {
  name: string;
  description: string;
  config: JobConfigRequest;
  queueName?: string;
  priority?: number;
}

export interface JobConfigRequest {
  target: JobTargetRequest;
  retryPolicy: RetryPolicyRequest;
  timeoutMs: number;
  secrets?: SecretRefRequest[];
}

export interface JobTargetRequest {
  type: string;
  url: string;
  method: HttpMethod;
  headers?: Record<string, string>;
  body?: string;
}

export interface RetryPolicyRequest {
  maxAttempts: number;
  backoffMs: number;
  backoffMultiplier: number;
}

export interface SecretRefRequest {
  secretId: string;
  key: string;
}

export interface UpdateJobRequest {
  name?: string;
  description?: string;
  config?: JobConfigRequest;
  queueName?: string;
  priority?: number;
}

export interface ToggleStatusRequest {
  status: 'ACTIVE' | 'DISABLED';
}

export interface ListJobsParams {
  status?: JobStatus;
  limit?: number;
  offset?: number;
}

// =========================================================================
// Versioning
// =========================================================================

export interface JobVersionResponse {
  jobUuid: string;
  version: number;
  config: JobConfigResponse;
  createdAt: string;
}

export interface JobVersionCompareResponse {
  fromVersion: number;
  toVersion: number;
  diffs: JobVersionDiff[];
  summary: JobVersionCompareSummary;
}

export interface JobVersionDiff {
  field: string;
  oldValue: string;
  newValue: string;
  type: string;
}

export interface JobVersionCompareSummary {
  totalChanges: number;
  breakingChanges: number;
  addedSecrets: string[];
}

// =========================================================================
// Trigger
// =========================================================================

export interface TriggerResponse {
  executionId: string;
  message: string;
}
