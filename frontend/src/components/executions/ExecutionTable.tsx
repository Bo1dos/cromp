// ---------------------------------------------------------------------------
// ExecutionTable — filterable, paginated table of executions
// ---------------------------------------------------------------------------

import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Select, DatePicker, Space, Tag, Typography } from 'antd';
import { ClockCircleOutlined, ThunderboltOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { useExecutionsList } from '@/hooks/useExecutions';
import ExecutionStatusBadge from './ExecutionStatusBadge';
import EmptyState from '@/components/common/EmptyState';
import { formatDate, formatDuration } from '@/utils/formatters';
import type { ExecutionResponse, ExecutionStatus, ExecutionSource } from '@/types/execution';

const { Text } = Typography;
const { RangePicker } = DatePicker;

// ---------------------------------------------------------------------------
// Status filter options
// ---------------------------------------------------------------------------
const STATUS_OPTIONS: { value: ExecutionStatus; label: string }[] = [
  { value: 'CREATED', label: 'Created' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'SUCCEEDED', label: 'Succeeded' },
  { value: 'FAILED', label: 'Failed' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'SKIPPED', label: 'Skipped' },
];

const SOURCE_OPTIONS: { value: ExecutionSource; label: string }[] = [
  { value: 'SCHEDULED', label: 'Scheduled' },
  { value: 'MANUAL', label: 'Manual' },
  { value: 'API', label: 'API' },
];

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

  // Filters
  const [statusFilter, setStatusFilter] = useState<ExecutionStatus[]>([]);
  const [sourceFilter, setSourceFilter] = useState<ExecutionSource | undefined>(undefined);
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null]>([null, null]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

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
      title: 'Job ID',
      dataIndex: 'jobId',
      key: 'jobId',
      width: 80,
    },
    {
      title: 'Status',
      dataIndex: 'finalStatus',
      key: 'finalStatus',
      width: 140,
      render: (status: string) => <ExecutionStatusBadge status={status as ExecutionStatus} />,
    },
    {
      title: 'Source',
      dataIndex: 'source',
      key: 'source',
      width: 120,
      render: (source: string) =>
        source === 'SCHEDULED' ? (
          <Tag icon={<ClockCircleOutlined />} color="blue">
            Scheduled
          </Tag>
        ) : source === 'API' ? (
          <Tag icon={<ThunderboltOutlined />} color="purple">
            API
          </Tag>
        ) : (
          <Tag icon={<ThunderboltOutlined />} color="orange">
            Manual
          </Tag>
        ),
    },
    {
      title: 'Started At',
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
      title: 'Finished At',
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
      title: 'Attempts',
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
          placeholder="Filter by status"
          value={statusFilter}
          onChange={setStatusFilter}
          options={STATUS_OPTIONS}
          allowClear
          style={{ minWidth: 200 }}
        />
        <Select
          placeholder="Filter by source"
          value={sourceFilter}
          onChange={setSourceFilter}
          options={SOURCE_OPTIONS}
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
          emptyText: <EmptyState description="No executions found" hint="Executions will appear here when jobs run." />,
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
          showTotal: (total) => `Total ${total} executions`,
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
