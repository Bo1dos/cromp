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
  MEMBERS: 'members',
  INVITATIONS: 'invitations',
  AUDIT: 'auditLog',
  ANALYTICS_SUMMARY: 'analyticsSummary',
  ANALYTICS_PREDICTIONS: 'predictions',
  ANALYTICS_ANOMALIES: 'anomalies',
  ANALYTICS_TIMESERIES: 'analyticsTimeseries',
  ANALYTICS_DURATION_DIST: 'analyticsDurationDist',
} as const;
