// ---------------------------------------------------------------------------
// ExecutionStatusBadge — color-coded badge for execution status
// ---------------------------------------------------------------------------

import { Badge } from 'antd';
import {
  SyncOutlined,
  CheckCircleFilled,
  CloseCircleFilled,
  MinusCircleFilled,
  ClockCircleFilled,
} from '@ant-design/icons';
import type { ExecutionStatus } from '@/types/execution';

interface StatusConfig {
  color: string;
  text: string;
  icon?: React.ReactNode;
}

const STATUS_CONFIG: Record<ExecutionStatus, StatusConfig> = {
  PENDING:   { color: 'default', text: 'Pending', icon: <ClockCircleFilled style={{ color: '#8c8c8c' }} /> },
  RUNNING:   { color: 'processing', text: 'Running', icon: <SyncOutlined spin style={{ color: '#1677ff' }} /> },
  SUCCESS:   { color: 'success', text: 'Success', icon: <CheckCircleFilled style={{ color: '#52c41a' }} /> },
  FAILED:    { color: 'error', text: 'Failed', icon: <CloseCircleFilled style={{ color: '#ff4d4f' }} /> },
  CANCELLED: { color: 'warning', text: 'Cancelled', icon: <MinusCircleFilled style={{ color: '#faad14' }} /> },
  TIMEOUT:   { color: '#ff7a45', text: 'Timeout', icon: <ClockCircleFilled style={{ color: '#ff7a45' }} /> },
};

interface ExecutionStatusBadgeProps {
  status: ExecutionStatus;
  showIcon?: boolean;
}

export default function ExecutionStatusBadge({ status, showIcon = true }: ExecutionStatusBadgeProps) {
  const config = STATUS_CONFIG[status] ?? { color: 'default', text: status };

  return (
    <Badge
      color={config.color}
      text={
        <span>
          {showIcon && config.icon && <> {config.icon}&nbsp;</>}
          {config.text}
        </span>
      }
    />
  );
}
