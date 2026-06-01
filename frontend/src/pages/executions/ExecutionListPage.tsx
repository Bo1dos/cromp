// ---------------------------------------------------------------------------
// ExecutionListPage — full-page list of executions with filters
// ---------------------------------------------------------------------------

import PageHeader from '@/components/common/PageHeader';
import ExecutionTable from '@/components/executions/ExecutionTable';
import { useOrganization } from '@/hooks/useOrganization';

export default function ExecutionListPage() {
  const { activeOrganization } = useOrganization();

  return (
    <div>
      <PageHeader
        title="Executions"
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Executions' },
        ]}
      />
      <div style={{ background: '#fff', padding: 24, borderRadius: 6 }}>
        <ExecutionTable />
      </div>
    </div>
  );
}
