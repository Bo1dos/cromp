// ---------------------------------------------------------------------------
// SecretListPage — list all secrets with table, filters & create button
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import SecretTable from '@/components/secrets/SecretTable';
import SecretForm from '@/components/secrets/SecretForm';

export default function SecretListPage() {
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
        title="Secrets"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Secrets' }]}
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setCreateModalOpen(true)}
          >
            Create Secret
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
