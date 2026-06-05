// ---------------------------------------------------------------------------
// JobEditPage — edit an existing job via the multi-step JobForm
// ---------------------------------------------------------------------------

import { useParams } from 'react-router-dom';
import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import JobForm from '@/components/jobs/JobForm';
import { useJobDetail, useUpdateJob } from '@/hooks/useJobs';
import type { CreateJobRequest } from '@/types/job';

export default function JobEditPage() {
  const { jobUuid } = useParams<{ jobUuid: string }>();
  const { data: job, isLoading, isError } = useJobDetail(jobUuid!);
  const updateMutation = useUpdateJob(jobUuid!);

  if (isLoading) {
    return <LoadingSpinner tip="Loading job…" />;
  }

  if (isError || !job) {
    return (
      <PageHeader
        title="Job not found"
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Jobs', href: '/dashboard/jobs' },
          { title: 'Not Found' },
        ]}
      />
    );
  }

  const handleSubmit = (values: CreateJobRequest) => {
    updateMutation.mutate(values);
  };

  return (
    <div>
      <PageHeader
        title={`Edit: ${job.name}`}
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Jobs', href: '/dashboard/jobs' },
          { title: job.name, href: `/dashboard/jobs/${job.jobUuid}` },
          { title: 'Edit' },
        ]}
      />

      <JobForm
        mode="edit"
        initialValues={job}
        onSubmit={handleSubmit}
        loading={updateMutation.isPending}
      />
    </div>
  );
}
