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
  uuid: string;
  actorUuid: string;
  actorName: string;
  action: AuditAction;
  resourceType: string;
  resourceUuid: string;
  details?: string;
  createdAt: string;
  organizationUuid: string;
}
