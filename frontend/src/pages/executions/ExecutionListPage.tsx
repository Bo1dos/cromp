// ---------------------------------------------------------------------------
// ExecutionListPage � full-page list of executions with filters
// ---------------------------------------------------------------------------

import PageHeader from '@/components/common/PageHeader';
import ExecutionTable from '@/components/executions/ExecutionTable';

export default function ExecutionListPage() {
  return (
    <div>
      <PageHeader
        title="Executions"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Executions' }]}
      />

      <ExecutionTable />
    </div>
  );
}
