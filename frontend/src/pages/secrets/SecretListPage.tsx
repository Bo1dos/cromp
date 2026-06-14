// ---------------------------------------------------------------------------
// SecretListPage — list all secrets with table, filters & create button
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import SecretTable from '@/components/secrets/SecretTable';
import SecretForm from '@/components/secrets/SecretForm';
import { useLanguage } from '@/hooks/useLanguage';

export default function SecretListPage() {
  const { t } = useLanguage();
  const [scope, setScope] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [createModalOpen, setCreateModalOpen] = useState(false);

  const handlePageChange = (p: number, ps: number) => {
    setPage(p);
    setPageSize(ps);
  };

  return (
    <div>
      <PageHeader
        title={t.nav.secrets}
        breadcrumbs={[{ title: t.nav.dashboard }, { title: t.nav.secrets }]}
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setCreateModalOpen(true)}
          >
            {t.secrets.createSecret}
          </Button>
        }
      />

      <SecretTable
        scope={scope}
        search={search}
        page={page}
        pageSize={pageSize}
        onScopeChange={(s) => {
          setScope(s);
          setPage(1);
        }}
        onSearchChange={(s) => {
          setSearch(s);
          setPage(1);
        }}
        onPageChange={handlePageChange}
      />

      <SecretForm
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
      />
    </div>
  );
}
