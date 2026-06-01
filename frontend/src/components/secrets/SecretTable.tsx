// ---------------------------------------------------------------------------
// SecretTable — Ant Design Table with filters, pagination, and row actions
// ---------------------------------------------------------------------------

import { useNavigate } from 'react-router-dom';
import { Table, Select, Input, Space, Button, Modal, Tag } from 'antd';
import { DeleteOutlined, SearchOutlined, EyeOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import type { SecretResponse, SecretScope } from '@/types/secret';
import { useSecretsList, useDeleteSecret } from '@/hooks/useSecrets';

dayjs.extend(relativeTime);

const SCOPE_CONFIG: Record<SecretScope, { color: string; label: string }> = {
  JOB: { color: 'blue', label: 'Job' },
  ORGANIZATION: { color: 'purple', label: 'Organization' },
};

/** Small component for row actions — needs its own hook context */
function SecretActions({ secret }: { secret: SecretResponse }) {
  const navigate = useNavigate();
  const deleteMutation = useDeleteSecret();

  const handleDelete = () => {
    Modal.confirm({
      title: `Delete "${secret.name}"?`,
      content:
        'This action cannot be undone. The secret will be permanently removed and any jobs relying on it will fail.',
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: () => deleteMutation.mutate(secret.uuid),
    });
  };

  return (
    <Space>
      <Button
        type="text"
        icon={<EyeOutlined />}
        size="small"
        title="View details"
        onClick={(e) => {
          e.stopPropagation();
          navigate(`/dashboard/secrets/${secret.uuid}`);
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
        loading={deleteMutation.isPending}
      />
    </Space>
  );
}

interface SecretTableProps {
  scope?: string;
  search?: string;
  page: number;
  pageSize: number;
  onScopeChange: (scope: string) => void;
  onSearchChange: (search: string) => void;
  onPageChange: (page: number, pageSize: number) => void;
}

export default function SecretTable({
  scope,
  search,
  page,
  pageSize,
  onScopeChange,
  onSearchChange,
  onPageChange,
}: SecretTableProps) {
  const navigate = useNavigate();
  const { data: secrets, isLoading } = useSecretsList({ scope, search, page, pageSize });

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (_: string, record: SecretResponse) => (
        <Button
          type="link"
          style={{ padding: 0 }}
          onClick={() => navigate(`/dashboard/secrets/${record.uuid}`)}
        >
          {record.name}
        </Button>
      ),
    },
    {
      title: 'Scope',
      dataIndex: 'scope',
      key: 'scope',
      width: 140,
      render: (s: SecretScope) => {
        const cfg = SCOPE_CONFIG[s] ?? { color: 'default', label: s };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    {
      title: 'Versions',
      dataIndex: 'versionCount',
      key: 'versionCount',
      width: 100,
      align: 'center' as const,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => (
        <span title={dayjs(date).format('YYYY-MM-DD HH:mm:ss')}>
          {dayjs(date).fromNow()}
        </span>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (_: unknown, record: SecretResponse) => (
        <SecretActions secret={record} />
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }} wrap>
        <Select
          allowClear
          placeholder="All scopes"
          style={{ width: 160 }}
          value={scope || undefined}
          onChange={(val) => onScopeChange(val ?? '')}
          options={[
            { label: 'Job', value: 'JOB' },
            { label: 'Organization', value: 'ORGANIZATION' },
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
        dataSource={secrets}
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
