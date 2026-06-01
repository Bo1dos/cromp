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
  { value: 'PENDING', label: 'Pending' },
  { value: 'RUNNING', label: 'Running' },
  { value: 'SUCCESS', label: 'Success' },
  { value: 'FAILED', label: 'Failed' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'TIMEOUT', label: 'Timeout' },
];

const SOURCE_OPTIONS: { value: ExecutionSource; label: string }[] = [
  { value: 'SCHEDULED', label: 'Scheduled' },
  { value: 'MANUAL', label: 'Manual' },
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
      title: 'Job Name',
      dataIndex: 'jobName',
      key: 'jobName',
      render: (name: string, record: ExecutionResponse) => (
        <a onClick={() => navigate(`/dashboard/jobs/${record.jobUuid}`)}>{name}</a>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 140,
      render: (status: ExecutionStatus) => <ExecutionStatusBadge status={status} />,
    },
    {
      title: 'Source',
      dataIndex: 'source',
      key: 'source',
      width: 120,
      render: (source: ExecutionSource) =>
        source === 'SCHEDULED' ? (
          <Tag icon={<ClockCircleOutlined />} color="blue">
            Scheduled
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
      title: 'Duration',
      dataIndex: 'durationMs',
      key: 'durationMs',
      width: 110,
      render: (val: number | undefined) => formatDuration(val),
    },
    {
      title: 'Attempts',
      dataIndex: 'attemptCount',
      key: 'attemptCount',
      width: 90,
      render: (val: number, record: ExecutionResponse) => (
        <a onClick={(e) => { e.stopPropagation(); navigate(`/dashboard/executions/${record.uuid}`); }}>
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
        rowKey="uuid"
        columns={columns}
        dataSource={data?.content ?? []}
        loading={isLoading}
        locale={{
          emptyText: <EmptyState description="No executions found" hint="Executions will appear here when jobs run." />,
        }}
        pagination={{
          current: page + 1,
          pageSize,
          total: data?.totalElements ?? 0,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50'],
          onChange: (p, ps) => {
            setPage(p - 1);
            setPageSize(ps);
          },
          showTotal: (total) => `Total ${total} executions`,
        }}
        onRow={(record) => ({
          onClick: () => navigate(`/dashboard/executions/${record.uuid}`),
          style: { cursor: 'pointer' },
        })}
        scroll={{ x: 900 }}
      />
    </div>
  );
}
