// ---------------------------------------------------------------------------
// JobHistoryTab — version history table with view / compare / revert actions
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Table, Button, Descriptions, Modal, Tag, Typography, Space } from 'antd';
import { EyeOutlined, SwapOutlined } from '@ant-design/icons';
import { useJobVersions } from '@/hooks/useJobVersions';
import VersionRevertButton from './VersionRevertButton';
import VersionDiffModal from './VersionDiffModal';
import EmptyState from '@/components/common/EmptyState';
import { formatDateFull } from '@/utils/formatters';
import type { JobVersionResponse } from '@/types/job';

const { Text } = Typography;

interface JobHistoryTabProps {
  jobUuid: string;
}

// ---------------------------------------------------------------------------
// Version detail modal
// ---------------------------------------------------------------------------
function JobVersionDetail({ version, onClose }: { version: JobVersionResponse; onClose: () => void }) {
  const c = version.config;
  return (
    <Modal
      title={`Version #${version.version}`}
      open
      onCancel={onClose}
      footer={<Button onClick={onClose}>Close</Button>}
      width={720}
    >
      <Descriptions bordered column={2} size="small">
        <Descriptions.Item label="Version" span={2}>
          <Tag>#{version.version}</Tag>
        </Descriptions.Item>
        <Descriptions.Item label="Job UUID" span={2}>
          <Text code>{version.jobUuid}</Text>
        </Descriptions.Item>
        <Descriptions.Item label="Created At" span={2}>
          {formatDateFull(version.createdAt)}
        </Descriptions.Item>

        <Descriptions.Item label="HTTP Config" span={2}>
          <pre style={{ margin: 0, fontSize: 12, maxHeight: 200, overflow: 'auto' }}>
            {JSON.stringify(c.target, null, 2)}
          </pre>
        </Descriptions.Item>
        <Descriptions.Item label="Retry Policy" span={2}>
          <pre style={{ margin: 0, fontSize: 12 }}>
            {JSON.stringify(c.retryPolicy, null, 2)}
          </pre>
        </Descriptions.Item>
        <Descriptions.Item label="Timeout">{c.timeoutMs} ms</Descriptions.Item>

        {c.secrets && c.secrets.length > 0 && (
          <Descriptions.Item label="Secrets" span={2}>
            {c.secrets.map((s) => `${s.envName} (${s.secretId})`).join(', ')}
          </Descriptions.Item>
        )}
      </Descriptions>
    </Modal>
  );
}

// ---------------------------------------------------------------------------
// Main tab
// ---------------------------------------------------------------------------
export default function JobHistoryTab({ jobUuid }: JobHistoryTabProps) {
  const { data: versions, isLoading } = useJobVersions(jobUuid);
  const [viewVersion, setViewVersion] = useState<JobVersionResponse | null>(null);
  const [diffModalOpen, setDiffModalOpen] = useState(false);
  const [selectedRows, setSelectedRows] = useState<JobVersionResponse[]>([]);

  const columns = [
    {
      title: '#',
      dataIndex: 'version',
      key: 'version',
      width: 80,
      render: (v: number) => <Tag>v{v}</Tag>,
    },
    {
      title: 'Config',
      key: 'config',
      ellipsis: true,
      render: (_: unknown, record: JobVersionResponse) => {
        const cfg = record.config;
        if (!cfg?.target) return <Text type="secondary">—</Text>;
        return (
          <Text code style={{ fontSize: 12 }}>
            {cfg.target.method ?? 'GET'} {cfg.target.url ?? '—'}
          </Text>
        );
      },
    },
    {
      title: 'Changed At',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (v: string) => formatDateFull(v),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 240,
      render: (_: unknown, record: JobVersionResponse) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => setViewVersion(record)}
          >
            View
          </Button>
          <VersionRevertButton jobUuid={jobUuid} version={record.version} />
        </Space>
      ),
    },
  ];

  const rowSelection = {
    type: 'checkbox' as const,
    selectedRowKeys: selectedRows.map((r) => r.version),
    onChange: (_: React.Key[], rows: JobVersionResponse[]) => {
      // Keep only last two selections for compare
      setSelectedRows(rows.slice(-2));
    },
  };

  const canCompare = selectedRows.length === 2;

  return (
    <div>
      <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
        <Text strong>Version History</Text>
        <Button
          icon={<SwapOutlined />}
          disabled={!canCompare}
          onClick={() => {
            if (canCompare) {
              setDiffModalOpen(true);
            }
          }}
        >
          Compare Selected{canCompare ? ` (v${selectedRows[0].version} ↔ v${selectedRows[1].version})` : ''}
        </Button>
      </Space>

      <Table
        rowKey="version"
        dataSource={versions ?? []}
        columns={columns}
        loading={isLoading}
        pagination={false}
        size="small"
        rowSelection={rowSelection}
        locale={{ emptyText: <EmptyState description="No version history" hint="Versions will be created when the job is updated." /> }}
      />

      {/* View detail modal */}
      {viewVersion && (
        <JobVersionDetail
          version={viewVersion}
          onClose={() => setViewVersion(null)}
        />
      )}

      {/* Diff modal */}
      <VersionDiffModal
        jobUuid={jobUuid}
        open={diffModalOpen}
        onClose={() => {
          setDiffModalOpen(false);
          setSelectedRows([]);
        }}
      />
    </div>
  );
}
