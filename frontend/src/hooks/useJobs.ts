// ---------------------------------------------------------------------------
// useJobs — TanStack Query hooks for the Jobs domain
// ---------------------------------------------------------------------------

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { notification } from 'antd';
import type {
  JobResponse,
  CreateJobRequest,
  UpdateJobRequest,
  ListJobsParams,
  TriggerResponse,
} from '@/types/job';
import * as jobsApi from '@/api/jobs.api';
import { QUERY_KEYS } from '@/utils/constants';

// ---------------------------------------------------------------------------
// List
// ---------------------------------------------------------------------------
interface UseJobsListFilters {
  status?: string;
  search?: string;
  page: number;
  pageSize: number;
}

export function useJobsList(filters: UseJobsListFilters) {
  const params: ListJobsParams = {
    status: filters.status as ListJobsParams['status'] | undefined,
    limit: filters.pageSize,
    offset: (filters.page - 1) * filters.pageSize,
  };

  return useQuery<JobResponse[], Error>({
    queryKey: [QUERY_KEYS.JOBS, filters],
    queryFn: () => jobsApi.listJobs(params),
    staleTime: 30_000,
    placeholderData: (previousData) => previousData,
  });
}

// ---------------------------------------------------------------------------
// Detail
// ---------------------------------------------------------------------------
export function useJobDetail(uuid: string) {
  return useQuery<JobResponse, Error>({
    queryKey: [QUERY_KEYS.JOB, uuid],
    queryFn: () => jobsApi.getJob(uuid),
    staleTime: 60_000,
    enabled: Boolean(uuid),
  });
}

// ---------------------------------------------------------------------------
// Create
// ---------------------------------------------------------------------------
export function useCreateJob() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation<JobResponse, Error, CreateJobRequest>({
    mutationFn: (data) => jobsApi.createJob(data),
    onSuccess: (job) => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOBS] });
      notification.success({ message: 'Job created', description: `Job "${job.name}" has been created.` });
      navigate(`/dashboard/jobs/${job.jobUuid}`);
    },
    onError: (error) => {
      notification.error({ message: 'Failed to create job', description: error.message });
    },
  });
}

// ---------------------------------------------------------------------------
// Update
// ---------------------------------------------------------------------------
export function useUpdateJob(uuid: string) {
  const queryClient = useQueryClient();

  return useMutation<JobResponse, Error, UpdateJobRequest>({
    mutationFn: (data) => jobsApi.updateJob(uuid, data),
    onSuccess: (job) => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOB, uuid] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOBS] });
      notification.success({ message: 'Job updated', description: `Job "${job.name}" has been updated.` });
    },
    onError: (error) => {
      notification.error({ message: 'Failed to update job', description: error.message });
    },
  });
}

// ---------------------------------------------------------------------------
// Delete
// ---------------------------------------------------------------------------
export function useDeleteJob() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation<void, Error, string>({
    mutationFn: (uuid) => jobsApi.deleteJob(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOBS] });
      notification.success({ message: 'Job deleted' });
      navigate('/dashboard/jobs');
    },
    onError: (error) => {
      notification.error({ message: 'Failed to delete job', description: error.message });
    },
  });
}

// ---------------------------------------------------------------------------
// Toggle status — with optimistic update
// ---------------------------------------------------------------------------
export function useToggleJobStatus(uuid: string) {
  const queryClient = useQueryClient();

  return useMutation<JobResponse, Error, 'ACTIVE' | 'DISABLED', { previous: JobResponse | undefined }>({
    mutationFn: (status) => jobsApi.toggleJobStatus(uuid, status),
    onMutate: async (newStatus) => {
      await queryClient.cancelQueries({ queryKey: [QUERY_KEYS.JOB, uuid] });
      const previous = queryClient.getQueryData<JobResponse>([QUERY_KEYS.JOB, uuid]);

      if (previous) {
        queryClient.setQueryData<JobResponse>([QUERY_KEYS.JOB, uuid], {
          ...previous,
          status: newStatus === 'ACTIVE' ? 'ACTIVE' : 'DISABLED',
        });
      }

      return { previous };
    },
    onError: (_err, _newStatus, context) => {
      if (context?.previous) {
        queryClient.setQueryData([QUERY_KEYS.JOB, uuid], context.previous);
      }
      notification.error({ message: 'Failed to update job status' });
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOB, uuid] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOBS] });
    },
  });
}

// ---------------------------------------------------------------------------
// Trigger
// ---------------------------------------------------------------------------
export function useTriggerJob(uuid: string) {
  const queryClient = useQueryClient();

  return useMutation<TriggerResponse, Error, void>({
    mutationFn: () => jobsApi.triggerJob(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.JOB, uuid] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.EXECUTIONS] });
      notification.success({ message: 'Job triggered', description: 'The job has been triggered manually.' });
    },
    onError: (error) => {
      notification.error({ message: 'Failed to trigger job', description: error.message });
    },
  });
}
