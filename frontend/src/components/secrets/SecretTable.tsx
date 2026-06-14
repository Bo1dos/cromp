// ---------------------------------------------------------------------------
// SecretTable — Ant Design Table with filters, pagination, and row actions
// ---------------------------------------------------------------------------

import { useNavigate } from 'react-router-dom';
import { Table, Select, Space, Button, Tag } from 'antd';
import { DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import type { SecretResponse, SecretScope } from '@/types/secret';
import { useSecretsList, useDeleteSecret } from '@/hooks/useSecrets';
import { useLanguage } from '@/hooks/useLanguage';
import ConfirmModal from '@/components/common/ConfirmModal';
import EmptyState from '@/components/common/EmptyState';
import SearchInput from '@/components/common/SearchInput';
import { formatRelative } from '@/utils/formatters';

const SCOPE_CONFIG: Record<SecretScope, { color: string; label: string }> = {
  JOB: { color: 'blue', label: 'Job' },
  ORGANIZATION: { color: 'purple', label: 'Organization' },
};

/** Small component for row actions — needs its own hook context */
function SecretActions({ secret }: { secret: SecretResponse }) {
  const navigate = useNavigate();
  const { t } = useLanguage();
  const deleteMutation = useDeleteSecret();

  const handleDelete = () => {
    ConfirmModal.show({
      title: `${t.secrets.deleteSecret} "${secret.name}"?`,
      content: t.secrets.deleteConfirm,
      danger: true,
      okText: t.common.delete,
      onOk: () => deleteMutation.mutate(secret.secretUuid),
    });
  };

  return (
    <Space>
      <Button
        type="text"
        icon={<EyeOutlined />}
        size="small"
        title={t.common.edit}
        onClick={(e) => {
          e.stopPropagation();
          navigate(`/dashboard/secrets/${secret.secretUuid}`);
        }}
      />
      <Button
        type="text"
        danger
        icon={<DeleteOutlined />}
        size="small"
        title={t.common.delete}
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
  const { t } = useLanguage();
  const { data: secrets, isLoading } = useSecretsList({ scope, search, page, pageSize });

  const columns = [
    {
      title: t.common.name,
      dataIndex: 'name',
      key: 'name',
      sorter: (a: SecretResponse, b: SecretResponse) => a.name.localeCompare(b.name),
      render: (_: string, record: SecretResponse) => (
        <Button
          type="link"
          style={{ padding: 0 }}
          onClick={() => navigate(`/dashboard/secrets/${record.secretUuid}`)}
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
      filters: [
        { text: 'Job', value: 'JOB' },
        { text: 'Organization', value: 'ORGANIZATION' },
      ],
      onFilter: (value: any, record: SecretResponse) => record.scope === value,
      render: (s: SecretScope) => {
        const cfg = SCOPE_CONFIG[s] ?? { color: 'default', label: s };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    {
      title: 'Versions',
      dataIndex: 'currentVersion',
      key: 'currentVersion',
      width: 100,
      align: 'center' as const,
      sorter: (a: SecretResponse, b: SecretResponse) => a.currentVersion - b.currentVersion,
    },
    {
      title: t.secrets.createdAt,
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      sorter: (a: SecretResponse, b: SecretResponse) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      render: (date: string) => (
        <span title={new Date(date).toLocaleString()}>
          {formatRelative(date)}
        </span>
      ),
    },
    {
      title: t.common.actions,
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
        <SearchInput
          value={search ?? ''}
          onChange={onSearchChange}
          placeholder={t.common.search + '…'}
        />
      </Space>

      <Table
        dataSource={secrets}
        columns={columns}
        rowKey="uuid"
        loading={isLoading}
        locale={{
          emptyText: <EmptyState description={t.secrets.noSecrets} hint={t.secrets.noSecretsHint} />,
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
