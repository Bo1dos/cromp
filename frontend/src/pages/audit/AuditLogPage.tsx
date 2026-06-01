// ---------------------------------------------------------------------------
// AuditLogPage — audit log viewer
// ---------------------------------------------------------------------------

import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import AuditLogTable from '@/components/audit/AuditLogTable';
import { useOrganization } from '@/hooks/useOrganization';

export default function AuditLogPage() {
  const { activeOrganization } = useOrganization();

  if (!activeOrganization) {
    return <LoadingSpinner tip="Loading organization…" />;
  }

  return (
    <div>
      <PageHeader
        title="Audit Log"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Audit' }]}
      />

      <AuditLogTable />
    </div>
  );
}
