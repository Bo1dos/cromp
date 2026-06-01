// ---------------------------------------------------------------------------
// CancelExecutionButton — cancel a running/pending execution
// ---------------------------------------------------------------------------

import { Button, notification } from 'antd';
import { CloseCircleOutlined } from '@ant-design/icons';
import { useCancelExecution } from '@/hooks/useExecutions';
import ConfirmModal from '@/components/common/ConfirmModal';
import type { ExecutionStatus } from '@/types/execution';

interface CancelExecutionButtonProps {
  executionId: string;
  status: ExecutionStatus;
}

export default function CancelExecutionButton({ executionId, status }: CancelExecutionButtonProps) {
  const cancelMutation = useCancelExecution(executionId);

  if (status !== 'RUNNING' && status !== 'PENDING') {
    return null;
  }

  const handleClick = () => {
    ConfirmModal.show({
      title: 'Cancel this execution?',
      content: 'The running job will be interrupted and the execution will be marked as cancelled.',
      danger: true,
      okText: 'Yes, cancel',
      onOk: async () => {
        try {
          await cancelMutation.mutateAsync();
          notification.success({ message: 'Execution cancelled' });
        } catch {
          // error handled in hook
        }
      },
    });
  };

  return (
    <Button
      danger
      icon={<CloseCircleOutlined />}
      onClick={handleClick}
      loading={cancelMutation.isPending}
    >
      Cancel
    </Button>
  );
}
