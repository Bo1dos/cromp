// ---------------------------------------------------------------------------
// VersionDiffModal — select two versions and view config diff side-by-side
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Modal, Select, Space, Spin, Typography, Empty } from 'antd';
import { SwapOutlined } from '@ant-design/icons';
import VersionDiff from './VersionDiff';
import { useJobVersions, useJobVersionDetail } from '@/hooks/useJobVersions';
import type { JobVersionResponse } from '@/types/job';

interface VersionDiffModalProps {
  jobUuid: string;
  open: boolean;
  onClose: () => void;
}

export default function VersionDiffModal({
  jobUuid,
  open,
  onClose,
}: VersionDiffModalProps) {
  const { data: versions, isLoading: versionsLoading } = useJobVersions(jobUuid);
  const [v1, setV1] = useState<number | null>(null);
  const [v2, setV2] = useState<number | null>(null);

  // Fetch full config of each selected version
  const { data: version1, isLoading: v1Loading } = useJobVersionDetail(jobUuid, v1);
  const { data: version2, isLoading: v2Loading } = useJobVersionDetail(jobUuid, v2);

  const isLoading = v1Loading || v2Loading;

  const versionOptions =
    versions?.map((v: JobVersionResponse) => ({
      value: v.version,
      label: `v${v.version} (${new Date(v.createdAt).toLocaleDateString()})`,
    })) ?? [];

  return (
    <Modal
      title="Compare Versions"
      open={open}
      onCancel={onClose}
      footer={null}
      width={900}
    >
      {versionsLoading ? (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <Spin />
        </div>
      ) : (
        <>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Space>
              <Select
                style={{ width: 260 }}
                placeholder="Select first version"
                options={versionOptions}
                value={v1}
                onChange={setV1}
                allowClear
              />
              <SwapOutlined style={{ fontSize: 18, color: '#999' }} />
              <Select
                style={{ width: 260 }}
                placeholder="Select second version"
                options={versionOptions}
                value={v2}
                onChange={setV2}
                allowClear
              />
            </Space>

            {v1 !== null && v2 !== null && (
              <Typography.Text type="secondary">
                Comparing v{v1} (left) with v{v2} (right)
              </Typography.Text>
            )}
          </Space>

          <div style={{ marginTop: 16 }}>
            {isLoading && (
              <div style={{ textAlign: 'center', padding: 40 }}>
                <Spin tip="Loading diff..." />
              </div>
            )}

            {version1 && version2 && !isLoading && (
              <VersionDiff
                oldConfig={version1.config as unknown as Record<string, unknown>}
                newConfig={version2.config as unknown as Record<string, unknown>}
                oldVersion={v1 ?? undefined}
                newVersion={v2 ?? undefined}
              />
            )}

            {!isLoading && v1 !== null && v2 !== null && (!version1 || !version2) && (
              <Empty description="No diff data available" />
            )}
          </div>
        </>
      )}
    </Modal>
  );
}
