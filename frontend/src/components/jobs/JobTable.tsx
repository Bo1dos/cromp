// ---------------------------------------------------------------------------
// JobTable — Ant Design Table with filters, pagination, and row actions
// ---------------------------------------------------------------------------

import { useNavigate } from 'react-router-dom';
import { Table, Select, Button, Space, Tag, Typography } from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import type { JobResponse, JobStatus } from '@/types/job';
import { useJobsList, useDeleteJob, useToggleJobStatus } from '@/hooks/useJobs';
import { useLanguage } from '@/hooks/useLanguage';
import JobStatusBadge from './JobStatusBadge';
import ConfirmModal from '@/components/common/ConfirmModal';
import EmptyState from '@/components/common/EmptyState';
import SearchInput from '@/components/common/SearchInput';

const { Text } = Typography;

/** Small component that renders action buttons with proper hooks context. */
function JobActions({ job }: { job: JobResponse }) {
  const deleteMutation = useDeleteJob();
  const toggleMutation = useToggleJobStatus(job.jobUuid);
  const isActive = job.status === 'ACTIVE';

  const handleDelete = () => {
    ConfirmModal.show({
      title: `Delete "${job.name}"?`,
      content: 'This action cannot be undone. The job will be permanently removed.',
      danger: true,
      okText: 'Delete',
      onOk: () => deleteMutation.mutate(job.jobUuid),
    });
  };

  const handleToggle = () => {
    toggleMutation.mutate(isActive ? 'DISABLED' : 'ACTIVE');
  };

  const handleTrigger = async () => {
    const { triggerJob } = await import('@/api/jobs.api');
    await triggerJob(job.jobUuid);
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
        title={isActive ? 'Disable' : 'Enable'}
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
  const { t } = useLanguage();
  const { data: jobs, isLoading } = useJobsList({ status, search, page, pageSize });

  const columns = [
    {
      title: t.common.name,
      dataIndex: 'name',
      key: 'name',
      sorter: (a: JobResponse, b: JobResponse) => a.name.localeCompare(b.name),
      render: (_: string, record: JobResponse) => (
        <Button type="link" style={{ padding: 0 }} onClick={() => navigate(`/dashboard/jobs/${record.jobUuid}`)}>
          {record.name}
        </Button>
      ),
    },
    {
      title: t.common.status,
      dataIndex: 'status',
      key: 'status',
      width: 120,
      filters: [
        { text: t.common.enabled, value: 'ACTIVE' },
        { text: t.common.disabled, value: 'DISABLED' },
        { text: 'Archived', value: 'ARCHIVED' },
      ],
      onFilter: (value: any, record: JobResponse) => record.status === value,
      render: (status: JobStatus) => <JobStatusBadge status={status} />,
    },
    {
      title: t.jobs.schedule,
      dataIndex: 'hasSchedule',
      key: 'schedule',
      width: 100,
      render: (has: boolean) => (
        has
          ? <Tag color="blue" style={{ margin: 0 }}>{t.jobs.scheduled}</Tag>
          : <Text type="secondary" style={{ fontSize: 12 }}>{t.jobs.manual}</Text>
      ),
    },
    {
      title: t.common.actions,
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
          placeholder={t.jobs.allStatuses}
          style={{ width: 160 }}
          value={status || undefined}
          onChange={(val) => onStatusChange(val ?? '')}
          options={[
            { label: t.common.enabled, value: 'ACTIVE' },
            { label: t.common.disabled, value: 'DISABLED' },
            { label: 'Archived', value: 'ARCHIVED' },
          ]}
        />
        <SearchInput
          value={search ?? ''}
          onChange={onSearchChange}
          placeholder={t.jobs.searchByName}
        />
      </Space>

      <Table
        dataSource={jobs}
        columns={columns}
        rowKey="uuid"
        loading={isLoading}
        locale={{
          emptyText: <EmptyState description={t.jobs.noJobsFound} hint={t.jobs.createFirstHint} />,
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
