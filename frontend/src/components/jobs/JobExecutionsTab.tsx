// ---------------------------------------------------------------------------
// JobExecutionsTab — filtered execution list for a specific job
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Tag, Typography, Select, Space, Empty } from 'antd';
import type { ColumnsType } from 'antd/es/table';

import { useExecutionsList } from '@/hooks/useExecutions';
import { useLanguage } from '@/hooks/useLanguage';
import { formatDateFull, formatDuration } from '@/utils/formatters';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import type { ExecutionResponse } from '@/types/execution';

const { Text } = Typography;

interface Props {
  jobId: number;
}

const STATUS_COLORS: Record<string, string> = {
  SUCCEEDED: 'success',
  FAILED: 'error',
  IN_PROGRESS: 'processing',
  CREATED: 'default',
  CANCELLED: 'warning',
  SKIPPED: 'default',
};

const PAGE_SIZE = 20;

export default function JobExecutionsTab({ jobId }: Props) {
  const navigate = useNavigate();
  const { t } = useLanguage();
  const [page, setPage] = useState(1);
  const [statusFilter, setStatusFilter] = useState<string>('');

  const { data, isLoading, isError } = useExecutionsList({
    jobId: String(jobId),
    status: statusFilter || undefined,
    page: page - 1,  // backend is 0-based
    size: PAGE_SIZE,
  });

  const columns: ColumnsType<ExecutionResponse> = [
    {
      title: 'ID',
      dataIndex: 'execUuid',
      key: 'id',
      width: 110,
      render: (uuid: string) => (
        <Text code ellipsis style={{ maxWidth: 100 }}>
          {uuid.slice(0, 8)}…
        </Text>
      ),
    },
    {
      title: t.common.status,
      dataIndex: 'finalStatus',
      key: 'status',
      width: 120,
      render: (s: string) => (
        <Tag color={STATUS_COLORS[s] || 'default'}>{s}</Tag>
      ),
    },
    {
      title: 'Source',
      dataIndex: 'source',
      key: 'source',
      width: 100,
      render: (s: string) => (
        <Text type="secondary" style={{ fontSize: 12 }}>{s}</Text>
      ),
    },
    {
      title: 'Attempts',
      dataIndex: 'totalAttempts',
      key: 'attempts',
      width: 90,
      align: 'center',
    },
    {
      title: 'Triggered',
      dataIndex: 'triggeredAt',
      key: 'triggeredAt',
      width: 170,
      render: (d: string) => (
        <Text style={{ fontSize: 12 }}>{formatDateFull(d)}</Text>
      ),
    },
    {
      title: 'Duration',
      key: 'duration',
      width: 100,
      render: (_: unknown, record: ExecutionResponse) => {
        if (!record.startedAt || !record.finishedAt) return '—';
        const ms = new Date(record.finishedAt).getTime() - new Date(record.startedAt).getTime();
        return <Text style={{ fontSize: 12 }}>{formatDuration(ms)}</Text>;
      },
    },
  ];

  if (isLoading) {
    return <LoadingSpinner tip={t.common.loading} minHeight={160} />;
  }

  if (isError) {
    return <Empty description={t.common.noData} />;
  }

  const executions = data?.items ?? [];
  const total = data?.total ?? 0;

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Select
          allowClear
          placeholder="All statuses"
          style={{ width: 160 }}
          value={statusFilter || undefined}
          onChange={(val) => {
            setStatusFilter(val ?? '');
            setPage(1);
          }}
          options={[
            { value: 'SUCCEEDED', label: 'Succeeded' },
            { value: 'FAILED', label: 'Failed' },
            { value: 'IN_PROGRESS', label: 'In Progress' },
            { value: 'CANCELLED', label: 'Cancelled' },
            { value: 'CREATED', label: 'Created' },
            { value: 'SKIPPED', label: 'Skipped' },
          ]}
        />
        <Text type="secondary">
          {total} execution{total !== 1 ? 's' : ''}
        </Text>
      </Space>

      <Table<ExecutionResponse>
        columns={columns}
        dataSource={executions}
        rowKey="execUuid"
        size="small"
        loading={isLoading}
        locale={{ emptyText: t.common.noData }}
        onRow={(record) => ({
          onClick: () => navigate(`/dashboard/executions/${record.execUuid}`),
          style: { cursor: 'pointer' },
        })}
        pagination={{
          current: page,
          pageSize: PAGE_SIZE,
          total,
          showSizeChanger: false,
          onChange: (p) => setPage(p),
        }}
      />
    </div>
  );
}
