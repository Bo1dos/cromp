// ---------------------------------------------------------------------------
// VersionRevertButton — revert job to a specific version with confirmation
// ---------------------------------------------------------------------------

import { Button } from 'antd';
import { UndoOutlined } from '@ant-design/icons';
import { useRevertVersion } from '@/hooks/useJobVersions';
import ConfirmModal from '@/components/common/ConfirmModal';

interface VersionRevertButtonProps {
  jobUuid: string;
  version: number;
}

export default function VersionRevertButton({
  jobUuid,
  version,
}: VersionRevertButtonProps) {
  const revertMutation = useRevertVersion(jobUuid);

  const handleRevert = () => {
    ConfirmModal.show({
      title: `Revert to version #${version}?`,
      content:
        'This will replace the current job configuration with the selected version. The current state will be saved as a new version.',
      okText: 'Revert',
      onOk: () => revertMutation.mutate(version),
    });
  };

  return (
    <Button
      type="link"
      size="small"
      icon={<UndoOutlined />}
      loading={revertMutation.isPending}
      onClick={handleRevert}
    >
      Revert
    </Button>
  );
}
