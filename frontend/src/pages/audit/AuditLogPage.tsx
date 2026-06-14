// ---------------------------------------------------------------------------
// AuditLogPage — audit log viewer
// ---------------------------------------------------------------------------

import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import AuditLogTable from '@/components/audit/AuditLogTable';
import { useOrganization } from '@/hooks/useOrganization';
import { useLanguage } from '@/hooks/useLanguage';

export default function AuditLogPage() {
  const { activeOrganization } = useOrganization();
  const { t } = useLanguage();

  if (!activeOrganization) {
    return <LoadingSpinner tip={t.common.loading} />;
  }

  return (
    <div>
      <PageHeader
        title={t.audit.title}
        breadcrumbs={[{ title: t.nav.dashboard }, { title: t.nav.audit }]}
      />

      <AuditLogTable />
    </div>
  );
}
