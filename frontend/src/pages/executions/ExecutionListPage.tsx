// ---------------------------------------------------------------------------
// ExecutionListPage � full-page list of executions with filters
// ---------------------------------------------------------------------------

import PageHeader from '@/components/common/PageHeader';
import ExecutionTable from '@/components/executions/ExecutionTable';
import { useLanguage } from '@/hooks/useLanguage';

export default function ExecutionListPage() {
  const { t } = useLanguage();

  return (
    <div>
      <PageHeader
        title={t.nav.executions}
        breadcrumbs={[{ title: t.nav.dashboard }, { title: t.nav.executions }]}
      />

      <ExecutionTable />
    </div>
  );
}
