// ---------------------------------------------------------------------------
// JobStatusBadge — color-coded badge for job status
// ---------------------------------------------------------------------------

import { Badge } from 'antd';
import type { JobStatus } from '@/types/job';

const STATUS_CONFIG: Record<JobStatus, { color: string; text: string }> = {
  ACTIVE:   { color: 'green',   text: 'Active' },
  DISABLED: { color: 'red',     text: 'Disabled' },
  ARCHIVED: { color: 'default', text: 'Archived' },
};

interface JobStatusBadgeProps {
  status: JobStatus;
}

export default function JobStatusBadge({ status }: JobStatusBadgeProps) {
  const config = STATUS_CONFIG[status] ?? { color: 'default', text: status };
  return <Badge color={config.color} text={config.text} />;
}
