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
} as const;
