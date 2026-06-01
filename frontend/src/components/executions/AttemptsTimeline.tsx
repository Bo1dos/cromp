// ---------------------------------------------------------------------------
// AttemptsTimeline — Ant Design Timeline for execution attempts
// ---------------------------------------------------------------------------

import { Timeline, Typography, Spin, Empty } from 'antd';
import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons';
import dayjs from 'dayjs';
import { useAttempts } from '@/hooks/useExecutions';

const { Text } = Typography;

interface AttemptsTimelineProps {
  executionId: string;
}

function formatDuration(ms?: number): string {
  if (ms == null) return '—';
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const remainSec = seconds % 60;
  if (minutes > 0) return `${minutes}m ${remainSec}s`;
  if (remainSec > 0) return `${remainSec}s`;
  return `${ms}ms`;
}

export default function AttemptsTimeline({ executionId }: AttemptsTimelineProps) {
  const { data: attempts, isLoading } = useAttempts(executionId);

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 40 }}>
        <Spin />
      </div>
    );
  }

  if (!attempts || attempts.length === 0) {
    return <Empty description="No attempts" />;
  }

  const items = attempts.map((attempt) => ({
    key: attempt.uuid,
    color: attempt.status === 'SUCCESS' ? 'green' : 'red',
    dot:
      attempt.status === 'SUCCESS' ? (
        <CheckCircleFilled style={{ color: '#52c41a', fontSize: 16 }} />
      ) : (
        <CloseCircleFilled style={{ color: '#ff4d4f', fontSize: 16 }} />
      ),
    children: (
      <div>
        <Text strong>
          Attempt #{attempt.attemptNumber} — {attempt.status}
        </Text>
        <div style={{ marginTop: 4 }}>
          <Text type="secondary">
            Started: {dayjs(attempt.startedAt).format('YYYY-MM-DD HH:mm:ss')}
          </Text>
          <br />
          {attempt.completedAt && (
            <Text type="secondary">
              Completed: {dayjs(attempt.completedAt).format('YYYY-MM-DD HH:mm:ss')}
            </Text>
          )}
          <br />
          <Text type="secondary">Duration: {formatDuration(attempt.durationMs)}</Text>
          {attempt.errorMessage && (
            <>
              <br />
              <Text type="danger">Error: {attempt.errorMessage}</Text>
            </>
          )}
          {attempt.statusCode && (
            <>
              <br />
              <Text>Status Code: {attempt.statusCode}</Text>
            </>
          )}
        </div>
      </div>
    ),
  }));

  return <Timeline items={items} />;
}
