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
  CREATED:    { color: 'default', text: 'Created', icon: <ClockCircleFilled style={{ color: '#8c8c8c' }} /> },
  IN_PROGRESS: { color: 'processing', text: 'In Progress', icon: <SyncOutlined spin style={{ color: '#1677ff' }} /> },
  SUCCEEDED:  { color: 'success', text: 'Succeeded', icon: <CheckCircleFilled style={{ color: '#52c41a' }} /> },
  FAILED:     { color: 'error', text: 'Failed', icon: <CloseCircleFilled style={{ color: '#ff4d4f' }} /> },
  CANCELLED:  { color: 'warning', text: 'Cancelled', icon: <MinusCircleFilled style={{ color: '#faad14' }} /> },
  SKIPPED:    { color: '#ff7a45', text: 'Skipped', icon: <ClockCircleFilled style={{ color: '#ff7a45' }} /> },
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
