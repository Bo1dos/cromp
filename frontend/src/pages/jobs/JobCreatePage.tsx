// ---------------------------------------------------------------------------
// JobCreatePage — create a new job via the multi-step JobForm
// ---------------------------------------------------------------------------

import PageHeader from '@/components/common/PageHeader';
import JobForm from '@/components/jobs/JobForm';
import { useCreateJob } from '@/hooks/useJobs';
import type { CreateJobRequest } from '@/types/job';

export default function JobCreatePage() {
  const createMutation = useCreateJob();

  const handleSubmit = (values: CreateJobRequest) => {
    createMutation.mutate(values);
  };

  return (
    <div>
      <PageHeader
        title="Create Job"
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Jobs', href: '/dashboard/jobs' },
          { title: 'Create' },
        ]}
      />

      <JobForm mode="create" onSubmit={handleSubmit} loading={createMutation.isPending} />
    </div>
  );
}
