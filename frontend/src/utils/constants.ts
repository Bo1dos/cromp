// ---------------------------------------------------------------------------
// API base URL — resolved from Vite env var
// ---------------------------------------------------------------------------
export const API_BASE = import.meta.env.VITE_API_BASE_URL;

// ---------------------------------------------------------------------------
// TanStack Query keys — structured per domain. Fill as you add queries.
// ---------------------------------------------------------------------------
export const QUERY_KEYS = {
  JOBS:  'jobs',
  JOB:   'job',
  EXECUTIONS: 'executions',
  JOB_VERSIONS: 'jobVersions',
  SECRETS: 'secrets',
  SECRET:  'secret',
  SECRET_VERSIONS: 'secretVersions',
  SCHEDULE: 'schedule',
} as const;
