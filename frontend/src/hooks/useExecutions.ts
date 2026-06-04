// ---------------------------------------------------------------------------
// useExecutions — TanStack Query hooks for the Executions domain
// ---------------------------------------------------------------------------

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notification } from 'antd';
import * as executionsApi from '@/api/executions.api';
import type { ListExecutionsParams } from '@/api/executions.api';
import type {
  ExecutionResponse,
  ExecutionDetailResponse,
  AttemptResponse,
  ArtifactResponse,
  PagedResponse,
} from '@/types/execution';
import { QUERY_KEYS } from '@/utils/constants';
import { useOrganization } from './useOrganization';

// ---------------------------------------------------------------------------
// List — with server-side pagination & filtering
// ---------------------------------------------------------------------------
export function useExecutionsList(filters: ListExecutionsParams) {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid;

  return useQuery<PagedResponse<ExecutionResponse>, Error>({
    queryKey: [QUERY_KEYS.EXECUTIONS, orgId, filters],
    queryFn: () => executionsApi.listExecutions(orgId!, filters),
    staleTime: 15_000,
    enabled: Boolean(orgId),
    placeholderData: (previousData) => previousData,
  });
}

// ---------------------------------------------------------------------------
// Detail — auto-refresh if execution is still running
// ---------------------------------------------------------------------------
export function useExecutionDetail(executionId: string) {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid;

  return useQuery<ExecutionDetailResponse, Error>({
    queryKey: ['execution', executionId],
    queryFn: () => executionsApi.getExecution(orgId!, executionId),
    staleTime: 60_000,
    enabled: Boolean(orgId) && Boolean(executionId),
    refetchInterval: (query) => {
      const data = query.state.data;
      if (data && (data.status === 'RUNNING' || data.status === 'PENDING')) {
        return 10_000;
      }
      return false;
    },
  });
}

// ---------------------------------------------------------------------------
// Cancel — optimistic update
// ---------------------------------------------------------------------------
export function useCancelExecution(executionId: string) {
  const queryClient = useQueryClient();
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid;

  return useMutation<void, Error, void, { previous?: ExecutionDetailResponse }>({
    mutationFn: () => executionsApi.cancelExecution(orgId!, executionId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ['execution', executionId] });
      const previous = queryClient.getQueryData<ExecutionDetailResponse>(['execution', executionId]);

      if (previous) {
        queryClient.setQueryData<ExecutionDetailResponse>(['execution', executionId], {
          ...previous,
          status: 'CANCELLED',
        });
      }

      return { previous };
    },
    onError: (_err, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(['execution', executionId], context.previous);
      }
      notification.error({ message: 'Failed to cancel execution' });
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.EXECUTIONS] });
      queryClient.invalidateQueries({ queryKey: ['execution', executionId] });
    },
  });
}

// ---------------------------------------------------------------------------
// Attempts
// ---------------------------------------------------------------------------
export function useAttempts(executionId: string) {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid;

  return useQuery<AttemptResponse[], Error>({
    queryKey: ['attempts', executionId],
    queryFn: () => executionsApi.getAttempts(orgId!, executionId),
    enabled: Boolean(orgId) && Boolean(executionId),
  });
}

// ---------------------------------------------------------------------------
// Artifacts
// ---------------------------------------------------------------------------
export function useArtifacts(executionId: string) {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid;

  return useQuery<ArtifactResponse[], Error>({
    queryKey: ['artifacts', executionId],
    queryFn: () => executionsApi.getArtifacts(orgId!, executionId),
    enabled: Boolean(orgId) && Boolean(executionId),
  });
}
