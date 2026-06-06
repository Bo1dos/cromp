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
import { formatDateFull } from '@/utils/formatters';
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
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label="Execution ID" span={2}>
            <Text code>{execution.execUuid}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Job ID">
            <Text>{execution.jobId}</Text>
          </Descriptions.Item>
          <Descriptions.Item label="Status">
            <ExecutionStatusBadge status={execution.finalStatus as any} />
          </Descriptions.Item>
          <Descriptions.Item label="Source">
            <Tag>{execution.source}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Started At">
            {formatDateFull(execution.startedAt)}
          </Descriptions.Item>
          <Descriptions.Item label="Completed At">
            {execution.finishedAt ? formatDateFull(execution.finishedAt) : <Text type="secondary">—</Text>}
          </Descriptions.Item>
          <Descriptions.Item label="Attempts">
            {execution.totalAttempts}
          </Descriptions.Item>
        </Descriptions>
      ),
    },
    {
      key: 'attempts',
      label: 'Attempts',
      children: <AttemptsTimeline executionId={execution!.execUuid} />,
    },
    {
      key: 'artifacts',
      label: 'Artifacts',
      children: <ArtifactViewer executionId={execution!.execUuid} />,
    },
  ];

  return (
    <div>
      <PageHeader
        title={
          <Space>
            <span>Execution {execution.execUuid}</span>
            <ExecutionStatusBadge status={execution.finalStatus as any} />
          </Space>
        }
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Executions', href: '/dashboard/executions' },
          { title: `Job #${execution.jobId}` },
        ]}
        extra={
          <CancelExecutionButton
            executionId={execution.execUuid}
            status={execution.finalStatus as any}
          />
        }
      />

      <Tabs defaultActiveKey="overview" items={tabItems} />
    </div>
  );
}
