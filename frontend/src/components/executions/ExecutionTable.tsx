// ---------------------------------------------------------------------------
// ExecutionTable — filterable, paginated table of executions
// ---------------------------------------------------------------------------

import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Select, DatePicker, Space, Tag, Typography } from 'antd';
import { ClockCircleOutlined, ThunderboltOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { useExecutionsList } from '@/hooks/useExecutions';
import { useLanguage } from '@/hooks/useLanguage';
import ExecutionStatusBadge from './ExecutionStatusBadge';
import EmptyState from '@/components/common/EmptyState';
import { formatDate, formatDuration } from '@/utils/formatters';
import type { ExecutionResponse, ExecutionStatus, ExecutionSource } from '@/types/execution';

const { Text } = Typography;
const { RangePicker } = DatePicker;

// ---------------------------------------------------------------------------
// Status filter options
// ---------------------------------------------------------------------------
function getStatusOptions(t: any): { value: ExecutionStatus; label: string }[] {
  return [
    { value: 'CREATED', label: t.executions.statusCreated },
    { value: 'IN_PROGRESS', label: t.executions.statusInProgress },
    { value: 'SUCCEEDED', label: t.executions.statusSucceeded },
    { value: 'FAILED', label: t.executions.statusFailed },
    { value: 'CANCELLED', label: t.executions.statusCancelled },
    { value: 'SKIPPED', label: t.executions.statusSkipped },
  ];
}

function getSourceOptions(t: any): { value: ExecutionSource; label: string }[] {
  return [
    { value: 'SCHEDULED', label: t.executions.sourceScheduled },
    { value: 'MANUAL', label: t.executions.sourceManual },
    { value: 'API', label: t.executions.sourceApi },
  ];
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Props
// ---------------------------------------------------------------------------
interface ExecutionTableProps {
  jobId?: string; // pre-filter when used inside JobDetailPage
}

export default function ExecutionTable({ jobId }: ExecutionTableProps) {
  const navigate = useNavigate();
  const { t } = useLanguage();

  // Filters
  const [statusFilter, setStatusFilter] = useState<ExecutionStatus[]>([]);
  const [sourceFilter, setSourceFilter] = useState<ExecutionSource | undefined>(undefined);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null]>([null, null]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const statusOptions = useMemo(() => getStatusOptions(t), [t]);
  const sourceOptions = useMemo(() => getSourceOptions(t), [t]);

  // Build query params
  const filters = useMemo(() => {
    const params: Record<string, unknown> = { page, size: pageSize };
    if (jobId) params.jobId = jobId;
    if (statusFilter.length > 0) params.status = statusFilter.join(',');
    if (sourceFilter) params.source = sourceFilter;
    if (dateRange[0]) params.from = dateRange[0].toISOString();
    if (dateRange[1]) params.to = dateRange[1].toISOString();
    return params;
  }, [jobId, statusFilter, sourceFilter, dateRange, page, pageSize]);

  const { data, isLoading } = useExecutionsList(filters);

  // ---- Columns ----------------------------------------------------------
  const columns = [
    {
      title: t.executions.jobId,
      dataIndex: 'jobId',
      key: 'jobId',
      width: 80,
    },
    {
      title: t.common.status,
      dataIndex: 'finalStatus',
      key: 'finalStatus',
      width: 140,
      render: (status: string) => <ExecutionStatusBadge status={status as ExecutionStatus} />,
    },
    {
      title: t.executions.source,
      dataIndex: 'source',
      key: 'source',
      width: 120,
      render: (source: string) =>
        source === 'SCHEDULED' ? (
          <Tag icon={<ClockCircleOutlined />} color="blue">
            {t.executions.sourceScheduled}
          </Tag>
        ) : source === 'API' ? (
          <Tag icon={<ThunderboltOutlined />} color="purple">
            {t.executions.sourceApi}
          </Tag>
        ) : (
          <Tag icon={<ThunderboltOutlined />} color="orange">
            {t.executions.sourceManual}
          </Tag>
        ),
    },
    {
      title: t.executions.startedAt,
      dataIndex: 'startedAt',
      key: 'startedAt',
      width: 180,
      render: (val: string) => (
        <Text title={dayjs(val).format('YYYY-MM-DD HH:mm:ss')}>
          {formatDate(val)}
        </Text>
      ),
    },
    {
      title: t.executions.finishedAt,
      dataIndex: 'finishedAt',
      key: 'finishedAt',
      width: 180,
      render: (val: string | undefined) =>
        val ? (
          <Text title={dayjs(val).format('YYYY-MM-DD HH:mm:ss')}>
            {formatDate(val)}
          </Text>
        ) : (
          <Text type="secondary">—</Text>
        ),
    },
    {
      title: t.executions.attempts,
      dataIndex: 'totalAttempts',
      key: 'totalAttempts',
      width: 90,
      render: (val: number, record: ExecutionResponse) => (
        <a onClick={(e) => { e.stopPropagation(); navigate(`/dashboard/executions/${record.execUuid}`); }}>
          {val}
        </a>
      ),
    },
  ];

  // ---- Render ----------------------------------------------------------
  return (
    <div>
      {/* Filters */}
      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          mode="multiple"
          placeholder={t.executions.filterByStatus}
          value={statusFilter}
          onChange={setStatusFilter}
          options={statusOptions}
          allowClear
          style={{ minWidth: 200 }}
        />
        <Select
          placeholder={t.executions.filterBySource}
          value={sourceFilter}
          onChange={setSourceFilter}
          options={sourceOptions}
          allowClear
          style={{ minWidth: 150 }}
        />
        <RangePicker
          value={dateRange}
          onChange={(dates) => setDateRange(dates ? [dates[0], dates[1]] : [null, null])}
          showTime
        />
      </Space>

      <Table<ExecutionResponse>
        rowKey="execUuid"
        columns={columns}
        dataSource={data?.items ?? []}
        loading={isLoading}
        locale={{
          emptyText: <EmptyState description={t.executions.noExecutions} hint={t.executions.noExecutionsHint} />,
        }}
        pagination={{
          current: page + 1,
          pageSize,
          total: data?.total ?? 0,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50'],
          onChange: (p, ps) => {
            setPage(p - 1);
            setPageSize(ps);
          },
          showTotal: (total) => t.executions.totalExecutions.replace('{total}', String(total)),
        }}
        onRow={(record) => ({
          onClick: () => navigate(`/dashboard/executions/${record.execUuid}`),
          style: { cursor: 'pointer' },
        })}
        scroll={{ x: 900 }}
      />
    </div>
  );
}
