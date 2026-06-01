// ---------------------------------------------------------------------------
// useSchedules — TanStack Query hooks for the Schedule domain
// ---------------------------------------------------------------------------

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notification } from 'antd';
import type { ScheduleResponse, CreateUpdateScheduleRequest } from '@/types/schedule';
import * as schedulesApi from '@/api/schedules.api';
import { QUERY_KEYS } from '@/utils/constants';

// ---------------------------------------------------------------------------
// Get schedule
// ---------------------------------------------------------------------------
export function useSchedule(jobUuid: string) {
  return useQuery<ScheduleResponse, Error>({
    queryKey: [QUERY_KEYS.SCHEDULE, jobUuid],
    queryFn: () => schedulesApi.getSchedule(jobUuid),
    staleTime: 60_000,
    enabled: Boolean(jobUuid),
    retry: false,
  });
}

// ---------------------------------------------------------------------------
// Create or update schedule
// ---------------------------------------------------------------------------
export function useUpdateSchedule(jobUuid: string) {
  const queryClient = useQueryClient();

  return useMutation<ScheduleResponse, Error, CreateUpdateScheduleRequest>({
    mutationFn: (data) => schedulesApi.createOrUpdateSchedule(jobUuid, data),
    onSuccess: (schedule) => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.SCHEDULE, jobUuid] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOB, jobUuid] });
      notification.success({
        message: 'Schedule saved',
        description: `Cron expression ${schedule.cronExpression} has been applied.`,
      });
    },
    onError: (error) => {
      notification.error({ message: 'Failed to save schedule', description: error.message });
    },
  });
}

// ---------------------------------------------------------------------------
// Delete schedule
// ---------------------------------------------------------------------------
export function useDeleteSchedule(jobUuid: string) {
  const queryClient = useQueryClient();

  return useMutation<void, Error, void>({
    mutationFn: () => schedulesApi.deleteSchedule(jobUuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.SCHEDULE, jobUuid] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOB, jobUuid] });
      notification.success({ message: 'Schedule removed' });
    },
    onError: (error) => {
      notification.error({ message: 'Failed to remove schedule', description: error.message });
    },
  });
}
