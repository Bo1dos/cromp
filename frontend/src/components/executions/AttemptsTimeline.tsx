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
    key: attempt.attemptUuid,
    color: attempt.status === 'SUCCEEDED' ? 'green' : 'red',
    dot:
      attempt.status === 'SUCCEEDED' ? (
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
          {attempt.startedAt && (
            <Text type="secondary">
              Started: {dayjs(attempt.startedAt).format('YYYY-MM-DD HH:mm:ss')}
            </Text>
          )}
          {attempt.startedAt && <br />}
          {attempt.finishedAt && (
            <Text type="secondary">
              Completed: {dayjs(attempt.finishedAt).format('YYYY-MM-DD HH:mm:ss')}
            </Text>
          )}
          <br />
          <Text type="secondary">Duration: {formatDuration(attempt.durationMs)}</Text>
          {(attempt.statusReason || attempt.errorClass) && (
            <>
              <br />
              <Text type="danger">Error: {attempt.statusReason || attempt.errorClass}</Text>
            </>
          )}
          {attempt.outputSummary && (() => {
            try {
              const s = JSON.parse(attempt.outputSummary);
              return (
                <>
                  <br />
                  <Text>Status: {s.statusCode}</Text>
                  {s.body && (
                    <>
                      <br />
                      <Text type="secondary" style={{ fontSize: 12 }}>Response:</Text>
                      <pre style={{ margin: '4px 0 0', fontSize: 12, maxHeight: 120, overflow: 'auto', background: 'var(--color-bg-layout, #f5f5f5)', padding: 8, borderRadius: 4 }}>
                        {typeof s.body === 'string' ? s.body : JSON.stringify(s.body, null, 2)}
                      </pre>
                    </>
                  )}
                </>
              );
            } catch {
              return null;
            }
          })()}
        </div>
      </div>
    ),
  }));

  return <Timeline items={items} />;
}
