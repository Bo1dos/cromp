// ---------------------------------------------------------------------------
// ExecutionDetailPage — view execution details, attempts, artifacts
// ---------------------------------------------------------------------------

import { useParams, useNavigate } from 'react-router-dom';
import { Descriptions, Space, Tag, Typography, Tabs, Button } from 'antd';
import { LinkOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import ExecutionStatusBadge from '@/components/executions/ExecutionStatusBadge';
import CancelExecutionButton from '@/components/executions/CancelExecutionButton';
import AttemptsTimeline from '@/components/executions/AttemptsTimeline';
import ArtifactViewer from '@/components/executions/ArtifactViewer';
import { formatDateFull } from '@/utils/formatters';
import { useExecutionDetail } from '@/hooks/useExecutions';
import { useLanguage } from '@/hooks/useLanguage';
import { getJobById } from '@/api/jobs.api';

const { Text } = Typography;

export default function ExecutionDetailPage() {
  const { executionId } = useParams<{ executionId: string }>();
  const navigate = useNavigate();
  const { data: execution, isLoading, isError } = useExecutionDetail(executionId!);
  const { t } = useLanguage();

  if (isLoading) {
    return <LoadingSpinner tip={t.common.loading} />;
  }

  if (isError || !execution) {
    return (
      <PageHeader
        title={t.common.noData}
        breadcrumbs={[
          { title: t.nav.dashboard, href: '/dashboard' },
          { title: t.nav.executions, href: '/dashboard/executions' },
          { title: t.common.noData },
        ]}
      />
    );
  }

  const tabItems = [
    {
      key: 'overview',
      label: t.jobs.overview,
      children: (
        <Descriptions bordered column={2} size="small">
          <Descriptions.Item label={t.executions.executionId} span={2}>
            <Text code>{execution.execUuid}</Text>
          </Descriptions.Item>
          <Descriptions.Item label={t.executions.jobId}>
            <Button
              type="link"
              icon={<LinkOutlined />}
              style={{ padding: 0 }}
              onClick={async () => {
                try {
                  const job = await getJobById(execution.jobId);
                  navigate(`/dashboard/jobs/${job.jobUuid}`);
                } catch {
                  // Silently fail — link just won't navigate
                }
              }}
            >
              #{execution.jobId}
            </Button>
          </Descriptions.Item>
          <Descriptions.Item label={t.common.status}>
            <ExecutionStatusBadge status={execution.finalStatus as any} />
          </Descriptions.Item>
          <Descriptions.Item label={t.executions.source}>
            <Tag>{execution.source}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label={t.executions.startedAt}>
            {formatDateFull(execution.startedAt)}
          </Descriptions.Item>
          <Descriptions.Item label={t.executions.completedAt}>
            {execution.finishedAt ? formatDateFull(execution.finishedAt) : <Text type="secondary">—</Text>}
          </Descriptions.Item>
          <Descriptions.Item label={t.executions.attempts}>
            {execution.totalAttempts}
          </Descriptions.Item>
        </Descriptions>
      ),
    },
    {
      key: 'attempts',
      label: t.executions.attempts,
      children: <AttemptsTimeline executionId={execution!.execUuid} />,
    },
    {
      key: 'artifacts',
      label: t.executions.artifacts,
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
          { title: t.nav.dashboard, href: '/dashboard' },
          { title: t.nav.executions, href: '/dashboard/executions' },
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
