// ---------------------------------------------------------------------------
// JobDetailPage — view job details, edit, toggle, trigger, delete
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Tabs, Descriptions, Tag, Spin, Space, Modal, Typography } from 'antd';
import {
  EditOutlined,
  PlayCircleOutlined,
  DeleteOutlined,
  PauseCircleOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import PageHeader from '@/components/common/PageHeader';
import JobStatusBadge from '@/components/jobs/JobStatusBadge';
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
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    );
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
    Modal.confirm({
      title: `Delete "${job.name}"?`,
      content: 'This action cannot be undone. The job will be permanently removed.',
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: () => deleteMutation.mutate(job.uuid),
    });
  };

  const handleToggle = () => {
    const newStatus = job.status === 'ACTIVE' ? 'DISABLED' : 'ENABLED';
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
            <Button icon={<EditOutlined />} onClick={() => navigate(`/dashboard/jobs/${job.uuid}/edit`)}>
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
            <Descriptions bordered column={2} size="small" style={{ background: '#fff' }}>
              <Descriptions.Item label="UUID" span={2}>
                <Text code>{job.uuid}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="Name">{job.name}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <JobStatusBadge status={job.status} />
              </Descriptions.Item>
              <Descriptions.Item label="Description" span={2}>
                {job.description || <Text type="secondary">—</Text>}
              </Descriptions.Item>

              <Descriptions.Item label="Schedule">
                {job.cronExpression ? <Tag>{job.cronExpression}</Tag> : <Text type="secondary">Manual</Text>}
              </Descriptions.Item>
              <Descriptions.Item label="Timezone">
                {job.timezone ?? 'UTC'}
              </Descriptions.Item>

              <Descriptions.Item label="Created">
                {dayjs(job.createdAt).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>
              <Descriptions.Item label="Updated">
                {dayjs(job.updatedAt).format('YYYY-MM-DD HH:mm:ss')}
              </Descriptions.Item>

              <Descriptions.Item label="Next Run">
                {job.nextRunAt ? dayjs(job.nextRunAt).format('YYYY-MM-DD HH:mm:ss') : <Text type="secondary">—</Text>}
              </Descriptions.Item>
              <Descriptions.Item label="Last Execution">
                {job.lastExecutionAt
                  ? `${dayjs(job.lastExecutionAt).format('YYYY-MM-DD HH:mm:ss')}${job.lastExecutionStatus ? ` (${job.lastExecutionStatus})` : ''}`
                  : <Text type="secondary">—</Text>}
              </Descriptions.Item>

              <Descriptions.Item label="HTTP URL" span={2}>
                <Text code>{job.httpConfig.method}</Text>&nbsp;
                <Text>{job.httpConfig.url}</Text>
              </Descriptions.Item>
              <Descriptions.Item label="Timeout">{job.httpConfig.timeoutMs} ms</Descriptions.Item>
              <Descriptions.Item label="Request Body">
                {job.httpConfig.body ? <pre style={{ margin: 0, fontSize: 12 }}>{job.httpConfig.body}</pre> : <Text type="secondary">—</Text>}
              </Descriptions.Item>

              {job.httpConfig.headers && Object.keys(job.httpConfig.headers).length > 0 && (
                <Descriptions.Item label="Headers" span={2}>
                  <pre style={{ margin: 0, fontSize: 12 }}>
                    {JSON.stringify(job.httpConfig.headers, null, 2)}
                  </pre>
                </Descriptions.Item>
              )}

              <Descriptions.Item label="Max Attempts">{job.retryPolicy.maxAttempts}</Descriptions.Item>
              <Descriptions.Item label="Backoff">{job.retryPolicy.backoffMs} ms × {job.retryPolicy.backoffMultiplier}</Descriptions.Item>
            </Descriptions>
          ),
        },
      ]} />
    </div>
  );
}
