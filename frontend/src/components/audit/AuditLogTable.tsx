// ---------------------------------------------------------------------------
// Audit Log — Table component with client-side filtering & pagination
// ---------------------------------------------------------------------------

import { useMemo, useState } from 'react';
import { Table, Tag, Input, Select, DatePicker, Space, Tooltip, Typography } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { AuditLogResponse, AuditAction } from '@/types/audit';
import { useOrganization } from '@/hooks/useOrganization';
import { useAuditByOrg } from '@/hooks/useAuditLog';
import EmptyState from '@/components/common/EmptyState';
import { formatDateFull } from '@/utils/formatters';

const { Text } = Typography;
const { RangePicker } = DatePicker;

// -----------------------------------------------------------------------
// Colour mapping for action tags
// -----------------------------------------------------------------------
const ACTION_COLORS: Record<string, string> = {
  CREATE:       'green',
  UPDATE:       'blue',
  DELETE:       'red',
  LOGIN:        'cyan',
  LOGOUT:       'default',
  INVITE:       'purple',
  REMOVE:       'volcano',
  ROLE_CHANGE:  'orange',
  TOGGLE_STATUS: 'gold',
  TRIGGER:      'geekblue',
  REVERT:       'magenta',
  EXPORT:       'lime',
};

function getActionColor(action: string): string {
  return ACTION_COLORS[action] ?? 'default';
}

// -----------------------------------------------------------------------
// Action filter options (all known actions)
// -----------------------------------------------------------------------
const ACTION_OPTIONS: { value: AuditAction; label: string }[] = [
  { value: 'CREATE',        label: 'Create' },
  { value: 'UPDATE',        label: 'Update' },
  { value: 'DELETE',        label: 'Delete' },
  { value: 'LOGIN',         label: 'Login' },
  { value: 'LOGOUT',        label: 'Logout' },
  { value: 'INVITE',        label: 'Invite' },
  { value: 'REMOVE',        label: 'Remove' },
  { value: 'ROLE_CHANGE',   label: 'Role Change' },
  { value: 'TOGGLE_STATUS', label: 'Toggle Status' },
  { value: 'TRIGGER',       label: 'Trigger' },
  { value: 'REVERT',        label: 'Revert' },
  { value: 'EXPORT',        label: 'Export' },
  { value: 'OTHER',         label: 'Other' },
];

// -----------------------------------------------------------------------
// Props
// -----------------------------------------------------------------------
interface AuditLogTableProps {
  /** Optional external data override (e.g. when used outside org context) */
  dataSource?: AuditLogResponse[];
  loading?: boolean;
}

export default function AuditLogTable({ dataSource: externalData, loading: externalLoading }: AuditLogTableProps) {
  const { activeOrganization } = useOrganization();
  const orgUuid = activeOrganization?.uuid;

  // --- Server data (when no external data provided) ---
  const { data: serverData, isLoading: serverLoading } = useAuditByOrg(orgUuid);

  const rawData = externalData ?? serverData ?? [];
  const isLoading = externalLoading ?? serverLoading;

  // --- Client-side filters ---
  const [search, setSearch] = useState('');
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null]>([null, null]);
  const [selectedActions, setSelectedActions] = useState<AuditAction[]>([]);

  // --- Pagination ---
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);

  // --- Filtered data ---
  const filtered = useMemo(() => {
    let list = rawData;

    // Search by actor name
    if (search.trim()) {
      const q = search.trim().toLowerCase();
      list = list.filter((r) => r.actorName.toLowerCase().includes(q));
    }

    // Date range
    if (dateRange[0] && dateRange[1]) {
      const from = dateRange[0].valueOf();
      const to = dateRange[1].valueOf();
      list = list.filter((r) => {
        const t = dayjs(r.createdAt).valueOf();
        return t >= from && t <= to;
      });
    }

    // Action filter (multi-select)
    if (selectedActions.length > 0) {
      list = list.filter((r) => selectedActions.includes(r.action));
    }

    return list;
  }, [rawData, search, dateRange, selectedActions]);

  // --- Columns ---
  const columns = [
    {
      title: 'Timestamp',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      sorter: (a: AuditLogResponse, b: AuditLogResponse) =>
        dayjs(a.createdAt).valueOf() - dayjs(b.createdAt).valueOf(),
      defaultSortOrder: 'descend' as const,
      render: (val: string) => (
        <Tooltip title={formatDateFull(val)}>
          <Text>{formatDateFull(val)}</Text>
        </Tooltip>
      ),
    },
    {
      title: 'Actor',
      dataIndex: 'actorName',
      key: 'actorName',
      width: 180,
    },
    {
      title: 'Action',
      dataIndex: 'action',
      key: 'action',
      width: 150,
      render: (action: AuditAction) => (
        <Tag color={getActionColor(action)}>{action}</Tag>
      ),
    },
    {
      title: 'Resource',
      key: 'resource',
      width: 280,
      render: (_: unknown, record: AuditLogResponse) => (
        <Space size={4}>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.resourceType}</Text>
          <Text code style={{ fontSize: 11, maxWidth: 180 }} ellipsis={{ tooltip: record.resourceUuid }}>
            {record.resourceUuid}
          </Text>
        </Space>
      ),
    },
    {
      title: 'Details',
      dataIndex: 'details',
      key: 'details',
      render: (details: string | undefined) =>
        details ? (
          <Tooltip title={details}>
            <Text
              ellipsis
              style={{ maxWidth: 300, display: 'inline-block' }}
            >
              {details}
            </Text>
          </Tooltip>
        ) : (
          <Text type="secondary">—</Text>
        ),
    },
  ];

  return (
    <div>
      {/* Filters */}
      <Space wrap style={{ marginBottom: 16 }}>
        <Input
          placeholder="Search by actor…"
          prefix={<SearchOutlined />}
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(1);
          }}
          allowClear
          style={{ width: 220 }}
        />

        <RangePicker
          value={dateRange}
          onChange={(dates) => {
            setDateRange(dates ? [dates[0], dates[1]] : [null, null]);
            setPage(1);
          }}
          showTime
        />

        <Select
          mode="multiple"
          placeholder="Filter by action"
          value={selectedActions}
          onChange={(vals) => {
            setSelectedActions(vals);
            setPage(1);
          }}
          options={ACTION_OPTIONS}
          allowClear
          style={{ minWidth: 200 }}
        />
      </Space>

      {/* Table */}
      <Table<AuditLogResponse>
        rowKey="uuid"
        columns={columns}
        dataSource={filtered}
        loading={isLoading}
        locale={{
          emptyText: <EmptyState description="No audit log entries found" hint="Audit events will appear here as actions are performed." />,
        }}
        pagination={{
          current: page,
          pageSize,
          total: filtered.length,
          showSizeChanger: true,
          pageSizeOptions: ['10', '20', '50', '100'],
          onChange: (p, ps) => {
            setPage(p);
            setPageSize(ps);
          },
          showTotal: (total) => `Total ${total} entries`,
        }}
        scroll={{ x: 900 }}
      />
    </div>
  );
}
