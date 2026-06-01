// ---------------------------------------------------------------------------
// useJobVersions — TanStack Query hooks for job versioning
// ---------------------------------------------------------------------------

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notification } from 'antd';
import type {
  JobVersionResponse,
  JobVersionCompareResponse,
} from '@/types/job';
import * as jobsApi from '@/api/jobs.api';
import { QUERY_KEYS } from '@/utils/constants';

// ---------------------------------------------------------------------------
// Versions list
// ---------------------------------------------------------------------------
export function useJobVersions(uuid: string) {
  return useQuery<JobVersionResponse[], Error>({
    queryKey: [QUERY_KEYS.JOB_VERSIONS, uuid],
    queryFn: () => jobsApi.getJobVersions(uuid),
    staleTime: 30_000,
    enabled: Boolean(uuid),
  });
}

// ---------------------------------------------------------------------------
// Single version detail
// ---------------------------------------------------------------------------
export function useJobVersionDetail(uuid: string, version: number | null) {
  return useQuery<JobVersionResponse, Error>({
    queryKey: [QUERY_KEYS.JOB_VERSIONS, uuid, version],
    queryFn: () => jobsApi.getJobVersion(uuid, version!),
    staleTime: 60_000,
    enabled: Boolean(uuid) && version !== null && version !== undefined,
  });
}

// ---------------------------------------------------------------------------
// Version compare
// ---------------------------------------------------------------------------
export function useVersionCompare(
  uuid: string,
  v1: number | null,
  v2: number | null,
) {
  return useQuery<JobVersionCompareResponse, Error>({
    queryKey: [QUERY_KEYS.JOB_VERSIONS, uuid, 'compare', v1, v2],
    queryFn: () => jobsApi.compareVersions(uuid, v1!, v2!),
    staleTime: 60_000,
    enabled: Boolean(uuid) && v1 !== null && v2 !== null,
  });
}

// ---------------------------------------------------------------------------
// Revert
// ---------------------------------------------------------------------------
export function useRevertVersion(uuid: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (targetVersion: number) => jobsApi.revertJob(uuid, targetVersion),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOB, uuid] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOB_VERSIONS, uuid] });
      notification.success({
        message: 'Job reverted',
        description: 'The job has been reverted to the selected version.',
      });
    },
    onError: (error: Error) => {
      notification.error({
        message: 'Failed to revert job',
        description: error.message,
      });
    },
  });
}
