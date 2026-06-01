// ---------------------------------------------------------------------------
// SecretDetailPage — view secret details, versions, rotate, delete
// IMPORTANT: Secret values are NEVER displayed.
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Button, Descriptions, Tag, Space, Typography } from 'antd';
import { DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import SecretVersionList from '@/components/secrets/SecretVersionList';
import RotateSecretModal from '@/components/secrets/RotateSecretModal';
import ConfirmModal from '@/components/common/ConfirmModal';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { formatDateFull } from '@/utils/formatters';
import { useSecretDetail, useDeleteSecret } from '@/hooks/useSecrets';
import type { SecretScope } from '@/types/secret';

const { Text } = Typography;

const SCOPE_CONFIG: Record<SecretScope, { color: string; label: string }> = {
  JOB: { color: 'blue', label: 'Job' },
  ORGANIZATION: { color: 'purple', label: 'Organization' },
};

export default function SecretDetailPage() {
  const { secretUuid } = useParams<{ secretUuid: string }>();
  const [rotateModalOpen, setRotateModalOpen] = useState(false);

  const { data: secret, isLoading, isError } = useSecretDetail(secretUuid!);
  const deleteMutation = useDeleteSecret();

  // ---- Loading / Error states ------------------------------------------
  if (isLoading) {
    return <LoadingSpinner tip="Loading secret details…" />;
  }

  if (isError || !secret) {
    return (
      <PageHeader
        title="Secret not found"
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Secrets', href: '/dashboard/secrets' },
          { title: 'Not Found' },
        ]}
      />
    );
  }

  // ---- Actions ----------------------------------------------------------
  const handleDelete = () => {
    ConfirmModal.show({
      title: `Delete "${secret.name}"?`,
      content:
        'This action cannot be undone. The secret will be permanently removed and any jobs relying on it will fail.',
      danger: true,
      okText: 'Delete',
      onOk: () => deleteMutation.mutate(secret.uuid),
    });
  };

  const scopeCfg = SCOPE_CONFIG[secret.scope] ?? { color: 'default', label: secret.scope };

  // ---- Render ----------------------------------------------------------
  return (
    <div>
      <PageHeader
        title={secret.name}
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Secrets', href: '/dashboard/secrets' },
          { title: secret.name },
        ]}
        extra={
          <Space wrap>
            <Button
              icon={<ReloadOutlined />}
              onClick={() => setRotateModalOpen(true)}
            >
              Rotate
            </Button>
            <Button
              danger
              icon={<DeleteOutlined />}
              onClick={handleDelete}
              loading={deleteMutation.isPending}
            >
              Delete
            </Button>
          </Space>
        }
      />

      <Descriptions bordered column={2} size="small" style={{ background: '#fff', marginBottom: 24 }}>
        <Descriptions.Item label="UUID" span={2}>
          <Text code>{secret.uuid}</Text>
        </Descriptions.Item>
        <Descriptions.Item label="Name">{secret.name}</Descriptions.Item>
        <Descriptions.Item label="Scope">
          <Tag color={scopeCfg.color}>{scopeCfg.label}</Tag>
        </Descriptions.Item>
        <Descriptions.Item label="Description" span={2}>
          {secret.description || <Text type="secondary">—</Text>}
        </Descriptions.Item>
        <Descriptions.Item label="Versions">{secret.versionCount}</Descriptions.Item>
        <Descriptions.Item label="Created">
          {formatDateFull(secret.createdAt)}
        </Descriptions.Item>
        <Descriptions.Item label="Updated" span={2}>
          {formatDateFull(secret.updatedAt)}
        </Descriptions.Item>
      </Descriptions>

      <div style={{ background: '#fff', padding: '16px 24px', borderRadius: 8 }}>
        <Typography.Title level={5} style={{ marginBottom: 16 }}>
          Version History
        </Typography.Title>
        <SecretVersionList secretUuid={secret.uuid} />
      </div>

      <RotateSecretModal
        secretUuid={secret.uuid}
        secretName={secret.name}
        open={rotateModalOpen}
        onClose={() => setRotateModalOpen(false)}
      />
    </div>
  );
}
