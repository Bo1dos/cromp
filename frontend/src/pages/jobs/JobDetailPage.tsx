// ---------------------------------------------------------------------------
// JobDetailPage — view job details, edit, toggle, trigger, delete
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Tabs, Descriptions, Space, Typography } from 'antd';
import {
  EditOutlined,
  PlayCircleOutlined,
  DeleteOutlined,
  PauseCircleOutlined,
} from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import JobStatusBadge from '@/components/jobs/JobStatusBadge';
import JobScheduleTab from '@/components/jobs/JobScheduleTab';
import ConfirmModal from '@/components/common/ConfirmModal';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { formatDateFull } from '@/utils/formatters';
import {
  useJobDetail,
  useDeleteJob,
  useToggleJobStatus,
  useTriggerJob,
} from '@/hooks/useJobs';

const { Text } = Typography;

export default function JobDetailPage() {
  const { jobUuid } = useParams<{ jobUuid: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');

  const { data: job, isLoading, isError } = useJobDetail(jobUuid!);
  const deleteMutation = useDeleteJob();
  const toggleMutation = useToggleJobStatus(jobUuid!);
  const triggerMutation = useTriggerJob(jobUuid!);

  // ---- Loading / Error states ------------------------------------------
  if (isLoading) {
    return <LoadingSpinner tip="Loading job details…" />;
  }

  if (isError || !job) {
    return (
      <PageHeader
        title="Job not found"
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Jobs', href: '/dashboard/jobs' },
          { title: 'Not Found' },
        ]}
      />
    );
  }

  // ---- Actions ----------------------------------------------------------
  const handleDelete = () => {
    ConfirmModal.show({
      title: `Delete "${job.name}"?`,
      content: 'This action cannot be undone. The job will be permanently removed.',
      danger: true,
      okText: 'Delete',
      onOk: () => deleteMutation.mutate(job.jobUuid),
    });
  };

  const handleToggle = () => {
    const newStatus = job.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
    toggleMutation.mutate(newStatus);
  };

  const handleTrigger = () => {
    triggerMutation.mutate();
  };

  const isActive = job.status === 'ACTIVE';

  // ---- Render ----------------------------------------------------------
  return (
    <div>
      <PageHeader
        title={
          <Space>
            <span>{job.name}</span>
            <JobStatusBadge status={job.status} />
          </Space>
        }
        breadcrumbs={[
          { title: 'Dashboard', href: '/dashboard' },
          { title: 'Jobs', href: '/dashboard/jobs' },
          { title: job.name },
        ]}
        extra={
          <Space wrap>
            <Button icon={<EditOutlined />} onClick={() => navigate(`/dashboard/jobs/${job.jobUuid}/edit`)}>
              Edit
            </Button>
            <Button
              icon={isActive ? <PauseCircleOutlined /> : <PlayCircleOutlined />}
              onClick={handleToggle}
            >
              {isActive ? 'Disable' : 'Enable'}
            </Button>
            <Button icon={<PlayCircleOutlined />} onClick={handleTrigger} loading={triggerMutation.isPending}>
              Run Now
            </Button>
            <Button danger icon={<DeleteOutlined />} onClick={handleDelete} loading={deleteMutation.isPending}>
              Delete
            </Button>
          </Space>
        }
      />

      <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
        {
          key: 'overview',
          label: 'Overview',
          children: (
            <Descriptions bordered column={2} size="small">
              <Descriptions.Item label="UUID" span={2}>
                <Text code>{job.jobUuid}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="Name">{job.name}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <JobStatusBadge status={job.status} />
              </Descriptions.Item>
              <Descriptions.Item label="Description" span={2}>
                {job.description || <Text type="secondary">—</Text>}
              </Descriptions.Item>

              <Descriptions.Item label="Queue">{job.queueName}</Descriptions.Item>
              <Descriptions.Item label="Priority">{job.priority}</Descriptions.Item>

              <Descriptions.Item label="Version">{job.currentVersion} / {job.versionCount}</Descriptions.Item>
              <Descriptions.Item label="Created By">User #{job.createdBy}</Descriptions.Item>

              <Descriptions.Item label="Created">
                {formatDateFull(job.createdAt)}
              </Descriptions.Item>
              <Descriptions.Item label="Updated">
                {formatDateFull(job.updatedAt)}
              </Descriptions.Item>

              <Descriptions.Item label="HTTP URL" span={2}>
                <Text code>{job.currentConfig.target.method}</Text>&nbsp;
                <Text>{job.currentConfig.target.url}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="Timeout">{job.currentConfig.timeoutMs} ms</Descriptions.Item>
              <Descriptions.Item label="Request Body">
                {job.currentConfig.target.body ? <pre style={{ margin: 0, fontSize: 12 }}>{job.currentConfig.target.body}</pre> : <Text type="secondary">—</Text>}
              </Descriptions.Item>

              {job.currentConfig.target.headers && Object.keys(job.currentConfig.target.headers).length > 0 && (
                <Descriptions.Item label="Headers" span={2}>
                  <pre style={{ margin: 0, fontSize: 12 }}>
                    {JSON.stringify(job.currentConfig.target.headers, null, 2)}
                  </pre>
                </Descriptions.Item>
              )}

              <Descriptions.Item label="Max Attempts">{job.currentConfig.retryPolicy.maxAttempts}</Descriptions.Item>
              <Descriptions.Item label="Backoff">{job.currentConfig.retryPolicy.backoffMs} ms × {job.currentConfig.retryPolicy.backoffMultiplier}</Descriptions.Item>
            </Descriptions>
          ),
        },
        {
          key: 'history',
          label: 'History',
          children: (
            <div style={{ padding: 24, textAlign: 'center' }}>
              <Text type="secondary">Execution history coming soon.</Text>
            </div>
          ),
        },
        {
          key: 'executions',
          label: 'Executions',
          children: (
            <div style={{ padding: 24, textAlign: 'center' }}>
              <Text type="secondary">Executions panel coming soon.</Text>
            </div>
          ),
        },
        {
          key: 'schedule',
          label: 'Schedule',
          children: <JobScheduleTab jobUuid={job.jobUuid} />,
        },
      ]} />
    </div>
  );
}
