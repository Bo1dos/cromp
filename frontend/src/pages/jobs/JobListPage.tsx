// ---------------------------------------------------------------------------
// JobListPage — list all jobs with filters, search, and create button
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import JobTable from '@/components/jobs/JobTable';

export default function JobListPage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);

  const handlePageChange = (p: number, ps: number) => {
    setPage(p);
    setPageSize(ps);
  };

  return (
    <div>
      <PageHeader
        title="Jobs"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Jobs' }]}
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/dashboard/jobs/new')}
          >
            Create Job
          </Button>
        }
      />

      <JobTable
        status={status}
        search={search}
        page={page}
        pageSize={pageSize}
        onStatusChange={(s) => {
          setStatus(s);
          setPage(1);
        }}
        onSearchChange={(s) => {
          setSearch(s);
          setPage(1);
        }}
        onPageChange={handlePageChange}
      />
    </div>
  );
}
