// ---------------------------------------------------------------------------
// Audit Log — API
// ---------------------------------------------------------------------------

import apiClient from './client';
import type { AuditLogResponse } from '@/types/audit';

export async function getAuditByOrg(orgUuid: string): Promise<AuditLogResponse[]> {
  const response = await apiClient.get<AuditLogResponse[]>(
    `/audit/organization/${orgUuid}`,
  );
  return response.data;
}

export async function getAuditByActor(userUuid: string): Promise<AuditLogResponse[]> {
  const response = await apiClient.get<AuditLogResponse[]>(
    `/audit/actor/${userUuid}`,
  );
  return response.data;
}
