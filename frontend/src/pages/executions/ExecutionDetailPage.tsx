// ---------------------------------------------------------------------------
// ExecutionDetailPage — full detail view of a single execution
// ---------------------------------------------------------------------------

import { useParams, useNavigate } from 'react-router-dom';
import { Descriptions, Typography, Space, Spin, Button } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import PageHeader from '@/components/common/PageHeader';
import ExecutionStatusBadge from '@/components/executions/ExecutionStatusBadge';
import CancelExecutionButton from '@/components/executions/CancelExecutionButton';
import AttemptsTimeline from '@/components/executions/AttemptsTimeline';
import ArtifactViewer from '@/components/executions/ArtifactViewer';
import {
  useExecutionDetail,
  useAttempts,
  useArtifacts,
} from '@/hooks/useExecutions';

const { Text } = Typography;

function formatDuration(ms?: number): string {
  if (ms == null) return '—';
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const remainSec = seconds % 60;
  if (minutes > 0) return `${minutes}m ${remainSec}s`;
  if (remainSec > 0) return `${remainSec}s`;
  return `${ms}ms`;
}

export default function ExecutionDetailPage() {
  const { executionId } = useParams<{ executionId: string }>();
  const navigate = useNavigate();

  const {
    data: execution,
    isLoading: isExecLoading,
    isError: isExecError,
  } = useExecutionDetail(executionId!);

  const { data: attempts } = useAttempts(executionId!);
  const { data: artifacts } = useArtifacts(executionId!);

  // ---- Loading / Error --------------------------------------------------
  if (isExecLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (isExecError || !execution) {
    return (
      <PageHeader
        title="Execution not found"
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Executions', href: '/dashboard/executions' },
          { title: 'Not Found' },
        ]}
      />
    );
  }

  // ---- Render ----------------------------------------------------------
  return (
    <div>
      <PageHeader
        title={
          <Space>
            <span>Execution {execution.uuid.slice(0, 8)}...</span>
            <ExecutionStatusBadge status={execution.status} />
          </Space>
        }
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Executions', href: '/dashboard/executions' },
          { title: execution.uuid.slice(0, 8) },
        ]}
        extra={
          <Space>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/dashboard/executions')}>
              Back
            </Button>
            <CancelExecutionButton executionId={execution.uuid} status={execution.status} />
          </Space>
        }
      />

      {/* ---- Details --------------------------------------------------- */}
      <div style={{ background: '#fff', padding: 24, borderRadius: 6, marginBottom: 24 }}>
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label="UUID" span={2}>
            <Text code>{execution.uuid}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <ExecutionStatusBadge status={execution.status} />
          </Descriptions.Item>
          <Descriptions.Item label="Source">
            <Text>{execution.source === 'SCHEDULED' ? 'Scheduled' : 'Manual'}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Job">
            <a onClick={() => navigate(`/dashboard/jobs/${execution.jobUuid}`)}>
              {execution.jobName}
            </a>
          </Descriptions.Item>
          <Descriptions.Item label="Started At">
            {dayjs(execution.startedAt).format('YYYY-MM-DD HH:mm:ss')}
          </Descriptions.Item>
          <Descriptions.Item label="Completed At">
            {execution.completedAt
              ? dayjs(execution.completedAt).format('YYYY-MM-DD HH:mm:ss')
              : <Text type="secondary">—</Text>}
          </Descriptions.Item>
          <Descriptions.Item label="Duration">
            {formatDuration(execution.durationMs)}
          </Descriptions.Item>
          <Descriptions.Item label="Attempt Count">
            {execution.attemptCount}
          </Descriptions.Item>

          <Descriptions.Item label="HTTP Config" span={2}>
            <pre style={{ margin: 0, fontSize: 12 }}>
              {JSON.stringify(execution.httpConfig, null, 2)}
            </pre>
          </Descriptions.Item>
          <Descriptions.Item label="Retry Policy" span={2}>
            <pre style={{ margin: 0, fontSize: 12 }}>
              {JSON.stringify(execution.retryPolicy, null, 2)}
            </pre>
          </Descriptions.Item>

          {execution.payload && (
            <Descriptions.Item label="Payload" span={2}>
              <pre style={{ margin: 0, fontSize: 12, maxHeight: 200, overflow: 'auto' }}>
                {JSON.stringify(execution.payload, null, 2)}
              </pre>
            </Descriptions.Item>
          )}
        </Descriptions>
      </div>

      {/* ---- Attempts -------------------------------------------------- */}
      <div style={{ background: '#fff', padding: 24, borderRadius: 6, marginBottom: 24 }}>
        <Text strong style={{ fontSize: 16, display: 'block', marginBottom: 16 }}>
          Attempts
        </Text>
        <AttemptsTimeline executionId={execution.uuid} />
      </div>

      {/* ---- Artifacts ------------------------------------------------- */}
      {artifacts && artifacts.length > 0 && (
        <div style={{ background: '#fff', padding: 24, borderRadius: 6 }}>
          <Text strong style={{ fontSize: 16, display: 'block', marginBottom: 16 }}>
            Artifacts
          </Text>
          <ArtifactViewer executionId={execution.uuid} />
        </div>
      )}
    </div>
  );
}



