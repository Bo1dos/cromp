// ---------------------------------------------------------------------------
// JobTable — Ant Design Table with filters, pagination, and row actions
// ---------------------------------------------------------------------------

import { useNavigate } from 'react-router-dom';
import { Table, Select, Button, Space } from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import type { JobResponse, JobStatus } from '@/types/job';
import { useJobsList, useDeleteJob, useToggleJobStatus } from '@/hooks/useJobs';
import JobStatusBadge from './JobStatusBadge';
import ConfirmModal from '@/components/common/ConfirmModal';
import EmptyState from '@/components/common/EmptyState';
import SearchInput from '@/components/common/SearchInput';
import { formatRelative } from '@/utils/formatters';

/** Small component that renders action buttons with proper hooks context. */
function JobActions({ job }: { job: JobResponse }) {
  const deleteMutation = useDeleteJob();
  const toggleMutation = useToggleJobStatus(job.uuid);
  const isPaused = job.status === 'PAUSED';

  const handleDelete = () => {
    ConfirmModal.show({
      title: `Delete "${job.name}"?`,
      content: 'This action cannot be undone. The job will be permanently removed.',
      danger: true,
      okText: 'Delete',
      onOk: () => deleteMutation.mutate(job.uuid),
    });
  };

  const handleToggle = () => {
    toggleMutation.mutate(isPaused ? 'ENABLED' : 'DISABLED');
  };

  const handleTrigger = async () => {
    const { triggerJob } = await import('@/api/jobs.api');
    await triggerJob(job.uuid);
  };

  return (
    <Space>
      <Button
        type="text"
        icon={<PlayCircleOutlined />}
        size="small"
        title="Run now"
        onClick={(e) => {
          e.stopPropagation();
          handleTrigger();
        }}
      />
      <Button
        type="text"
        icon={<PauseCircleOutlined />}
        size="small"
        title={isPaused ? 'Resume' : 'Pause'}
        onClick={(e) => {
          e.stopPropagation();
          handleToggle();
        }}
      />
      <Button
        type="text"
        danger
        icon={<DeleteOutlined />}
        size="small"
        title="Delete"
        onClick={(e) => {
          e.stopPropagation();
          handleDelete();
        }}
      />
    </Space>
  );
}

interface JobTableProps {
  status?: string;
  search?: string;
  page: number;
  pageSize: number;
  onStatusChange: (status: string) => void;
  onSearchChange: (search: string) => void;
  onPageChange: (page: number, pageSize: number) => void;
}

export default function JobTable({
  status,
  search,
  page,
  pageSize,
  onStatusChange,
  onSearchChange,
  onPageChange,
}: JobTableProps) {
  const navigate = useNavigate();
  const { data: jobs, isLoading } = useJobsList({ status, search, page, pageSize });

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: (a: JobResponse, b: JobResponse) => a.name.localeCompare(b.name),
      render: (_: string, record: JobResponse) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/dashboard/jobs/${record.uuid}`)}>
          {record.name}
        </Button>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      filters: [
        { text: 'Active', value: 'ACTIVE' },
        { text: 'Paused', value: 'PAUSED' },
        { text: 'Disabled', value: 'DISABLED' },
      ],
      onFilter: (value: any, record: JobResponse) => record.status === value,
      render: (status: JobStatus) => <JobStatusBadge status={status} />,
    },
    {
      title: 'Schedule',
      dataIndex: 'cronExpression',
      key: 'schedule',
      width: 160,
      render: (cron: string | undefined) => (
        <span>{cron ? <code>{cron}</code> : 'Manual'}</span>
      ),
    },
    {
      title: 'Last Execution',
      dataIndex: 'lastExecutionAt',
      key: 'lastExecutionAt',
      width: 160,
      sorter: (a: JobResponse, b: JobResponse) =>
        (a.lastExecutionAt ? dayjs(a.lastExecutionAt).valueOf() : 0) -
        (b.lastExecutionAt ? dayjs(b.lastExecutionAt).valueOf() : 0),
      render: (date: string | undefined) =>
        date ? (
          <span title={dayjs(date).format('YYYY-MM-DD HH:mm:ss')}>
            {formatRelative(date)}
          </span>
        ) : (
          <span style={{ color: '#999' }}>—</span>
        ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 180,
      render: (_: unknown, record: JobResponse) => <JobActions job={record} />,
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }} wrap>
        <Select
          allowClear
          placeholder="All statuses"
          style={{ width: 160 }}
          value={status || undefined}
          onChange={(val) => onStatusChange(val ?? '')}
          options={[
            { label: 'Active', value: 'ACTIVE' },
            { label: 'Paused', value: 'PAUSED' },
            { label: 'Disabled', value: 'DISABLED' },
          ]}
        />
        <SearchInput
          value={search ?? ''}
          onChange={onSearchChange}
          placeholder="Search by name…"
        />
      </Space>

      <Table
        dataSource={jobs}
        columns={columns}
        rowKey="uuid"
        loading={isLoading}
        locale={{
          emptyText: <EmptyState description="No jobs found" hint="Create your first job to get started!" />,
        }}
        pagination={{
          current: page,
          pageSize,
          onChange: onPageChange,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50'],
        }}
      />
    </div>
  );
}
