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
  AUDIT: 'audit',
  ORGANIZATIONS: 'organizations',
  ANALYTICS_SUMMARY: 'analyticsSummary',
  ANALYTICS_PREDICTIONS: 'analyticsPredictions',
  ANALYTICS_ANOMALIES: 'analyticsAnomalies',
  ANALYTICS_TIMESERIES: 'analyticsTimeseries',
  ANALYTICS_DURATION_DIST: 'analyticsDurationDist',
  NOTIFICATIONS: 'notifications',
  UNREAD_COUNT: 'unreadCount',
  NOTIFICATION_PREFS: 'notificationPrefs',
} as const;
