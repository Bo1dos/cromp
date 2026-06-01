// ---------------------------------------------------------------------------
// VersionDiffModal — modal for selecting two versions and viewing their diff
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Modal, Select, Space, Spin, Typography, Empty } from 'antd';
import { SwapOutlined } from '@ant-design/icons';
import VersionDiff from './VersionDiff';
import {
  useJobVersions,
  useVersionCompare,
} from '@/hooks/useJobVersions';
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

  const {
    data: compareResult,
    isLoading: compareLoading,
    isFetching: compareFetching,
  } = useVersionCompare(jobUuid, v1, v2);

  const versionOptions =
    versions?.map((v: JobVersionResponse) => ({
      value: v.version,
      label: `v${v.version} — ${v.name} (${new Date(v.createdAt).toLocaleDateString()})`,
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
                style={{ width: 280 }}
                placeholder="Select first version"
                options={versionOptions}
                value={v1}
                onChange={setV1}
                allowClear
              />
              <SwapOutlined style={{ fontSize: 18, color: '#999' }} />
              <Select
                style={{ width: 280 }}
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
            {(compareLoading || compareFetching) && (
              <div style={{ textAlign: 'center', padding: 40 }}>
                <Spin tip="Loading diff..." />
              </div>
            )}

            {compareResult && !compareLoading && !compareFetching && (
              <VersionDiff
                oldConfig={compareResult.left as unknown as Record<string, unknown>}
                newConfig={compareResult.right as unknown as Record<string, unknown>}
              />
            )}

            {!compareResult && !compareLoading && !compareFetching && v1 !== null && v2 !== null && (
              <Empty description="No diff data available" />
            )}
          </div>
        </>
      )}
    </Modal>
  );
}
