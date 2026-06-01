// ---------------------------------------------------------------------------
// Audit Log — Page
// ---------------------------------------------------------------------------

import PageHeader from '@/components/common/PageHeader';
import AuditLogTable from '@/components/audit/AuditLogTable';

export default function AuditLogPage() {
  return (
    <div>
      <PageHeader
        title="Audit Log"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Audit Log' }]}
      />

      <AuditLogTable />
    </div>
  );
}
