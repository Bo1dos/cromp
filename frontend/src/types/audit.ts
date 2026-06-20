// ---------------------------------------------------------------------------
// Audit Log — types
// ---------------------------------------------------------------------------

export type AuditAction =
  | 'CREATE'
  | 'UPDATE'
  | 'DELETE'
  | 'LOGIN'
  | 'LOGOUT'
  | 'INVITE'
  | 'REMOVE'
  | 'ROLE_CHANGE'
  | 'TOGGLE_STATUS'
  | 'TRIGGER'
  | 'REVERT'
  | 'EXPORT'
  | 'OTHER'
  | 'JOB.CREATE'
  | 'JOB.UPDATE'
  | 'JOB.DELETE'
  | 'JOB.STATUS_CHANGE'
  | 'JOB.TRIGGER'
  | 'JOB.REVERT'
  | 'JOB.ARCHIVE'
  | 'EXECUTION.CREATE'
  | 'EXECUTION.SUCCEED'
  | 'EXECUTION.FAIL'
  | 'EXECUTION.TIMEOUT'
  | 'EXECUTION.CANCEL'
  | 'EXECUTION.CANCELLED'
  | 'SCHEDULE.CREATE'
  | 'SCHEDULE.UPDATE'
  | 'SCHEDULE.DELETE'
  | 'SECRET.CREATE'
  | 'SECRET.DELETE'
  | 'SECRET.ROTATE'
  | 'MEMBER.INVITE'
  | 'MEMBER.REMOVE'
  | 'MEMBER.ROLE_CHANGE'
  | 'ORG.CREATE'
  | 'ORG.UPDATE';

export interface AuditLogResponse {
  id: number;
  organizationId: number;
  recordedAt: string;
  actorId: number | null;
  actorSnapshot: Record<string, unknown>;
  action: AuditAction;
  resourceType: string;
  resourceId: number;
  changesDiff: Record<string, unknown>;
}
