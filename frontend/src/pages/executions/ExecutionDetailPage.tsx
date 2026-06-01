// ---------------------------------------------------------------------------
// ExecutionDetailPage — view execution details, attempts, artifacts
// ---------------------------------------------------------------------------

import { useParams } from 'react-router-dom';
import { Descriptions, Space, Tag, Typography, Tabs } from 'antd';
import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import ExecutionStatusBadge from '@/components/executions/ExecutionStatusBadge';
import CancelExecutionButton from '@/components/executions/CancelExecutionButton';
import AttemptsTimeline from '@/components/executions/AttemptsTimeline';
import ArtifactViewer from '@/components/executions/ArtifactViewer';
import { formatDateFull, formatDuration } from '@/utils/formatters';
import { useExecutionDetail } from '@/hooks/useExecutions';

const { Text } = Typography;

export default function ExecutionDetailPage() {
  const { executionId } = useParams<{ executionId: string }>();
  const { data: execution, isLoading, isError } = useExecutionDetail(executionId!);

  if (isLoading) {
    return <LoadingSpinner tip="Loading execution details…" />;
  }

  if (isError || !execution) {
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

  const tabItems = [
    {
      key: 'overview',
      label: 'Overview',
      children: (
        <Descriptions bordered column={2} size="small" style={{ background: '#fff' }}>
          <Descriptions.Item label="Execution ID" span={2}>
            <Text code>{execution.uuid}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Job">
            <a href={`/dashboard/jobs/${execution.jobUuid}`}>{execution.jobName}</a>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <ExecutionStatusBadge status={execution.status} />
          </Descriptions.Item>
          <Descriptions.Item label="Source">
            <Tag>{execution.source}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Started At">
            {formatDateFull(execution.startedAt)}
          </Descriptions.Item>
          <Descriptions.Item label="Completed At">
            {execution.completedAt ? formatDateFull(execution.completedAt) : <Text type="secondary">—</Text>}
          </Descriptions.Item>
          <Descriptions.Item label="Duration">
            {formatDuration(execution.durationMs)}
          </Descriptions.Item>
          <Descriptions.Item label="Attempts">
            {execution.attemptCount}
          </Descriptions.Item>
          <Descriptions.Item label="Error Message" span={2}>
            {execution.errorMessage ?? <Text type="secondary">—</Text>}
          </Descriptions.Item>
        </Descriptions>
      ),
    },
    {
      key: 'attempts',
      label: 'Attempts',
      children: <AttemptsTimeline executionId={execution!.uuid} />,
    },
    {
      key: 'artifacts',
      label: 'Artifacts',
      children: <ArtifactViewer executionId={execution!.uuid} />,
    },
  ];

  return (
    <div>
      <PageHeader
        title={
          <Space>
            <span>{execution.jobName}</span>
            <ExecutionStatusBadge status={execution.status} />
          </Space>
        }
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Executions', href: '/dashboard/executions' },
          { title: execution.jobName },
        ]}
        extra={
          <CancelExecutionButton
            executionId={execution.uuid}
            status={execution.status}
          />
        }
      />

      <Tabs defaultActiveKey="overview" items={tabItems} />
    </div>
  );
}
