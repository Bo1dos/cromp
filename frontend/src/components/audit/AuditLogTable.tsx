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
import { useLanguage } from '@/hooks/useLanguage';
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
  // Full action names from backend
  'JOB.CREATE': 'green',
  'JOB.UPDATE': 'blue',
  'JOB.DELETE': 'red',
  'JOB.STATUS_CHANGE': 'gold',
  'JOB.TRIGGER': 'geekblue',
  'JOB.REVERT': 'magenta',
  'JOB.ARCHIVE': 'default',
  'EXECUTION.CREATE': 'green',
  'EXECUTION.SUCCEED': 'green',
  'EXECUTION.FAIL': 'red',
  'EXECUTION.TIMEOUT': 'orange',
  'EXECUTION.CANCEL': 'volcano',
  'EXECUTION.CANCELLED': 'volcano',
  'SCHEDULE.CREATE': 'green',
  'SCHEDULE.UPDATE': 'blue',
  'SCHEDULE.DELETE': 'red',
  'SECRET.CREATE': 'green',
  'SECRET.DELETE': 'red',
  'SECRET.ROTATE': 'purple',
  'MEMBER.INVITE': 'purple',
  'MEMBER.REMOVE': 'volcano',
  'MEMBER.ROLE_CHANGE': 'orange',
  'ORG.CREATE': 'green',
  'ORG.UPDATE': 'blue',
};

function getActionColor(action: string): string {
  return ACTION_COLORS[action] ?? 'default';
}

// -----------------------------------------------------------------------
// Action filter options (all known actions)
// -----------------------------------------------------------------------
const ACTION_OPTIONS: { value: AuditAction; label: string }[] = [
  { value: 'JOB.CREATE',          label: 'Job Create' },
  { value: 'JOB.UPDATE',          label: 'Job Update' },
  { value: 'JOB.DELETE',          label: 'Job Delete' },
  { value: 'JOB.STATUS_CHANGE',   label: 'Job Status' },
  { value: 'JOB.TRIGGER',         label: 'Job Trigger' },
  { value: 'JOB.REVERT',          label: 'Job Revert' },
  { value: 'JOB.ARCHIVE',         label: 'Job Archive' },
  { value: 'EXECUTION.CREATE',    label: 'Exec Create' },
  { value: 'EXECUTION.SUCCEED',   label: 'Exec Succeed' },
  { value: 'EXECUTION.FAIL',      label: 'Exec Fail' },
  { value: 'EXECUTION.TIMEOUT',   label: 'Exec Timeout' },
  { value: 'EXECUTION.CANCEL',    label: 'Exec Cancel' },
  { value: 'SCHEDULE.CREATE',     label: 'Schedule Create' },
  { value: 'SCHEDULE.UPDATE',     label: 'Schedule Update' },
  { value: 'SCHEDULE.DELETE',     label: 'Schedule Delete' },
  { value: 'SECRET.CREATE',       label: 'Secret Create' },
  { value: 'SECRET.DELETE',       label: 'Secret Delete' },
  { value: 'SECRET.ROTATE',       label: 'Secret Rotate' },
  { value: 'MEMBER.INVITE',       label: 'Member Invite' },
  { value: 'MEMBER.REMOVE',       label: 'Member Remove' },
  { value: 'MEMBER.ROLE_CHANGE',  label: 'Member Role' },
  { value: 'ORG.CREATE',          label: 'Org Create' },
  { value: 'ORG.UPDATE',          label: 'Org Update' },
  { value: 'OTHER',               label: 'Other' },
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
  const orgUuid = activeOrganization?.orgUuid;
  const { t } = useLanguage();

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

  const actionOptions = useMemo(() => ACTION_OPTIONS.map((a) => ({ ...a })), []);

  /** Format changesDiff map into a readable string */
  function formatDetails(diff: Record<string, unknown> | undefined): string {
    if (!diff || Object.keys(diff).length === 0) return '';
    return Object.entries(diff)
      .map(([k, v]) => `${k}: ${typeof v === 'object' ? JSON.stringify(v) : v}`)
      .join('; ');
  }

  /** Get actor display name */
  function getActorName(r: AuditLogResponse): string {
    if (r.actorId == null) return 'system';
    const snap = r.actorSnapshot as Record<string, unknown> | undefined;
    if (snap && typeof snap.name === 'string') return snap.name;
    if (snap && typeof snap.email === 'string') return snap.email;
    return `User #${r.actorId}`;
  }

  // --- Filtered data ---
  const filtered = useMemo(() => {
    let list = rawData;

    // Search by actor
    if (search.trim()) {
      const q = search.trim().toLowerCase();
      list = list.filter((r) => getActorName(r).toLowerCase().includes(q));
    }

    // Date range
    if (dateRange[0] && dateRange[1]) {
      const from = dateRange[0].valueOf();
      const to = dateRange[1].valueOf();
      list = list.filter((r) => {
        const tr = dayjs(r.recordedAt).valueOf();
        return tr >= from && tr <= to;
      });
    }

    // Action filter
    if (selectedActions.length > 0) {
      list = list.filter((r) => selectedActions.includes(r.action));
    }

    return list;
  }, [rawData, search, dateRange, selectedActions]);

  // --- Columns ---
  const columns = [
    {
      title: t.audit.timestamp,
      dataIndex: 'recordedAt',
      key: 'recordedAt',
      width: 180,
      sorter: (a: AuditLogResponse, b: AuditLogResponse) =>
        dayjs(a.recordedAt).valueOf() - dayjs(b.recordedAt).valueOf(),
      defaultSortOrder: 'descend' as const,
      render: (val: string) => (
        <Tooltip title={formatDateFull(val)}>
          <Text>{formatDateFull(val)}</Text>
        </Tooltip>
      ),
    },
    {
      title: t.audit.actor,
      key: 'actor',
      width: 160,
      render: (_: unknown, record: AuditLogResponse) => (
        <Text>{getActorName(record)}</Text>
      ),
    },
    {
      title: t.audit.action,
      dataIndex: 'action',
      key: 'action',
      width: 150,
      render: (action: AuditAction) => (
        <Tag color={getActionColor(action)}>{action}</Tag>
      ),
    },
    {
      title: t.audit.resource,
      key: 'resource',
      width: 220,
      render: (_: unknown, record: AuditLogResponse) => (
        <Space size={4}>
          <Text type="secondary" style={{ fontSize: 12 }}>{record.resourceType}</Text>
          <Text code style={{ fontSize: 11 }}>#{record.resourceId}</Text>
        </Space>
      ),
    },
    {
      title: t.audit.details,
      key: 'details',
      render: (_: unknown, record: AuditLogResponse) => {
        const details = formatDetails(record.changesDiff);
        return details ? (
          <Tooltip title={details}>
            <Text ellipsis style={{ maxWidth: 300, display: 'inline-block' }}>
              {details}
            </Text>
          </Tooltip>
        ) : (
          <Text type="secondary">—</Text>
        );
      },
    },
  ];

  return (
    <div>
      {/* Filters */}
      <Space wrap style={{ marginBottom: 16 }}>
        <Input
          placeholder={t.common.search + '…'}
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
          placeholder={t.audit.action}
          value={selectedActions}
          onChange={(vals) => {
            setSelectedActions(vals);
            setPage(1);
          }}
          options={actionOptions}
          allowClear
          style={{ minWidth: 200 }}
        />
      </Space>

      {/* Table */}
      <Table<AuditLogResponse>
        rowKey="id"
        columns={columns}
        dataSource={filtered}
        loading={isLoading}
        locale={{
          emptyText: <EmptyState description={t.audit.noEntries} hint={t.audit.noEntriesHint} />,
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
          showTotal: (total) => `${t.audit.title}: ${total}`,
        }}
        scroll={{ x: 900 }}
      />
    </div>
  );
}
