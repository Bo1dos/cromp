// ---------------------------------------------------------------------------
// Audit Log — TanStack Query hooks
// ---------------------------------------------------------------------------

import { useQuery } from '@tanstack/react-query';
import * as auditApi from '@/api/audit.api';
import type { AuditLogResponse } from '@/types/audit';
import { QUERY_KEYS } from '@/utils/constants';

export function useAuditByOrg(orgUuid: string | undefined) {
  return useQuery<AuditLogResponse[], Error>({
    queryKey: [QUERY_KEYS.AUDIT, orgUuid],
    queryFn: () => auditApi.getAuditByOrg(orgUuid!),
    staleTime: 2 * 60 * 1000, // 2 min
    enabled: Boolean(orgUuid),
  });
}
