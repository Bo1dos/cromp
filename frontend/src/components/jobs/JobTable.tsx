// ---------------------------------------------------------------------------
// JobTable — Ant Design Table with filters, pagination, and row actions
// ---------------------------------------------------------------------------

import { useNavigate } from 'react-router-dom';
import { Table, Select, Input, Button, Space, Modal } from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  DeleteOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import type { JobResponse, JobStatus } from '@/types/job';
import { useJobsList, useDeleteJob, useToggleJobStatus } from '@/hooks/useJobs';
import JobStatusBadge from './JobStatusBadge';

dayjs.extend(relativeTime);

/** Small component that renders action buttons with proper hooks context. */
function JobActions({ job }: { job: JobResponse }) {
  const deleteMutation = useDeleteJob();
  const toggleMutation = useToggleJobStatus(job.uuid);
  const isPaused = job.status === 'PAUSED';

  const handleDelete = () => {
    Modal.confirm({
      title: `Delete "${job.name}"?`,
      content: 'This action cannot be undone. The job will be permanently removed.',
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
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
      render: (date: string | undefined) =>
        date ? (
          <span title={dayjs(date).format('YYYY-MM-DD HH:mm:ss')}>
            {dayjs(date).fromNow()}
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
        <Input.Search
          placeholder="Search by name…"
          prefix={<SearchOutlined />}
          style={{ width: 260 }}
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          onSearch={(val) => onSearchChange(val)}
          allowClear
        />
      </Space>

      <Table
        dataSource={jobs}
        columns={columns}
        rowKey="uuid"
        loading={isLoading}
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
