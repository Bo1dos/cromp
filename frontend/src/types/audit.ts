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
  | 'OTHER';

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
