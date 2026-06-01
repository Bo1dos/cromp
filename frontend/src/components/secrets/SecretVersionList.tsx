// ---------------------------------------------------------------------------
// SecretVersionList — displays version history WITHOUT secret values
// ---------------------------------------------------------------------------

import { Table } from 'antd';
import dayjs from 'dayjs';
import type { SecretVersionResponse } from '@/types/secret';
import { useSecretVersions } from '@/hooks/useSecrets';

interface SecretVersionListProps {
  secretUuid: string;
}

export default function SecretVersionList({ secretUuid }: SecretVersionListProps) {
  const { data: versions, isLoading, isError } = useSecretVersions(secretUuid);

  const columns = [
    {
      title: 'Version',
      dataIndex: 'version',
      key: 'version',
      width: 100,
      render: (v: number) => <code>v{v}</code>,
    },
    {
      title: 'Created At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 220,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: 'Created By',
      dataIndex: 'createdBy',
      key: 'createdBy',
      render: (user: string) => user || <span style={{ color: '#999' }}>—</span>,
    },
  ];

  return (
    <Table
      dataSource={versions}
      columns={columns}
      rowKey="version"
      loading={isLoading}
      pagination={false}
      locale={{ emptyText: 'No version history available.' }}
      size="small"
    />
  );
}
