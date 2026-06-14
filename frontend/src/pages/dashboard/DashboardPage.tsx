// ---------------------------------------------------------------------------
// DashboardPage — KPI cards, status breakdown, and recent activity
// ---------------------------------------------------------------------------

import { useNavigate } from 'react-router-dom';
import {
  Row,
  Col,
  Card,
  Statistic,
  Table,
  Tag,
  Typography,
  Space,
  Button,
  Progress,
  Skeleton,
} from 'antd';
import {
  ScheduleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  PauseCircleOutlined,
  ThunderboltOutlined,
  ArrowRightOutlined,
  WarningOutlined,
  StopOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';

import PageHeader from '@/components/common/PageHeader';
import { useLanguage } from '@/hooks/useLanguage';
import { useOrganization } from '@/hooks/useOrganization';
import { useJobsList } from '@/hooks/useJobs';
import { useExecutionsList } from '@/hooks/useExecutions';
import { useSummary } from '@/hooks/useAnalytics';
import { formatRelative } from '@/utils/formatters';
import type { ExecutionResponse } from '@/types/execution';
import type { ColumnsType } from 'antd/es/table';

const { Text } = Typography;

// ---------------------------------------------------------------------------
// Status color helpers
// ---------------------------------------------------------------------------
const statusColors: Record<string, string> = {
  SUCCEEDED: 'success',
  FAILED: 'error',
  IN_PROGRESS: 'processing',
  CREATED: 'default',
  CANCELLED: 'warning',
  SKIPPED: 'default',
};

// ---------------------------------------------------------------------------
// DashboardPage
// ---------------------------------------------------------------------------
export default function DashboardPage() {
  const navigate = useNavigate();
  const { t } = useLanguage();
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid ?? '';

  // ---- Data hooks (all real backend endpoints) --------------------------
  const { data: jobs = [] } = useJobsList({ page: 1, pageSize: 200 });
  const {
    data: summary,
    isLoading: summaryLoading,
    isError: summaryError,
  } = useSummary(orgId, '7d');
  const { data: executionsPage } = useExecutionsList({ page: 0, size: 6 });
  const { data: failedPage } = useExecutionsList({ status: 'FAILED', page: 0, size: 5 });

  // ---- Derived counts ---------------------------------------------------
  const totalJobs = jobs.length;
  const activeJobs = jobs.filter((j) => j.status === 'ACTIVE').length;
  const disabledJobs = jobs.filter((j) => j.status === 'DISABLED').length;
  const archivedJobs = jobs.filter((j) => j.status === 'ARCHIVED').length;

  // ---- Unscheduled active jobs ------------------------------------------
  const unscheduledActive = jobs.filter(
    (j) => j.status === 'ACTIVE' && !j.hasSchedule,
  );

  // ---- Failed executions for quick list ---------------------------------
  const lastFailures = failedPage?.items ?? [];

  const successRate = summary && summary.total > 0
    ? Math.round((summary.succeeded / summary.total) * 100)
    : 0;

  // ---- Recent executions columns ----------------------------------------
  const executionColumns: ColumnsType<ExecutionResponse> = [
    {
      title: 'ID',
      dataIndex: 'execUuid',
      key: 'id',
      width: 100,
      render: (uuid: string) => (
        <Text code ellipsis style={{ maxWidth: 90 }}>
          {uuid.slice(0, 8)}…
        </Text>
      ),
    },
    {
      title: t.common.status,
      dataIndex: 'finalStatus',
      key: 'status',
      width: 120,
      render: (s: string) => <Tag color={statusColors[s] || 'default'}>{s}</Tag>,
    },
    {
      title: t.nav.jobs,
      dataIndex: 'jobId',
      key: 'jobId',
      width: 80,
      render: (id: number) => <Text>#{id}</Text>,
    },
    {
      title: t.executions.triggered,
      dataIndex: 'triggeredAt',
      key: 'triggeredAt',
      width: 150,
      render: (d: string) => (
        <Text type="secondary" style={{ fontSize: 12 }}>
          {formatRelative(d)}
        </Text>
      ),
    },
  ];

  // ---- Guard: no org selected -------------------------------------------
  if (!orgId) {
    return (
      <PageHeader
        title={t.nav.dashboard}
        breadcrumbs={[{ title: t.nav.dashboard }]}
      />
    );
  }

  return (
    <div>
      <PageHeader
        title={t.nav.dashboard}
        breadcrumbs={[{ title: t.nav.dashboard }]}
        extra={
          <Space>
            <Button
              type="primary"
              icon={<ScheduleOutlined />}
              onClick={() => navigate('/dashboard/jobs/new')}
            >
              {t.dashboard.createJob}
            </Button>
            <Button onClick={() => navigate('/dashboard/jobs')}>
              {t.dashboard.viewAll} <ArrowRightOutlined />
            </Button>
          </Space>
        }
      />

      {/* ---- KPI Cards ---- */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title={t.dashboard.totalJobs}
              value={totalJobs}
              prefix={<ScheduleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title={t.dashboard.activeJobs}
              value={activeJobs}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            <Statistic
              title={t.dashboard.disabledJobs}
              value={disabledJobs}
              prefix={<PauseCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card>
            {summaryLoading ? (
              <Skeleton active paragraph={{ rows: 1 }} />
            ) : summaryError ? (
              <Statistic
                title={t.dashboard.failedToday}
                value="—"
                prefix={<CloseCircleOutlined />}
              />
            ) : (
              <Statistic
                title={t.dashboard.failedToday}
                value={summary?.failed ?? 0}
                valueStyle={{ color: summary?.failed ? '#ff4d4f' : undefined }}
                prefix={<CloseCircleOutlined />}
                suffix={
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    / {summary?.total ?? 0}
                  </Text>
                }
              />
            )}
          </Card>
        </Col>
      </Row>

      {/* ---- Unscheduled Active Jobs Alert ---- */}
      {unscheduledActive.length > 0 && (
        <Card
          size="small"
          style={{ marginBottom: 24, borderLeft: '3px solid #faad14' }}
        >
          <Space direction="vertical" style={{ width: '100%' }} size="small">
            <Space>
              <WarningOutlined style={{ color: '#faad14', fontSize: 16 }} />
              <Text strong>
                {t.dashboard.activeJobsNoSchedule.replace('{count}', String(unscheduledActive.length))}
              </Text>
            </Space>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              {unscheduledActive.slice(0, 5).map((job) => (
                <Button
                  key={job.jobUuid}
                  size="small"
                  type="dashed"
                  onClick={() => navigate(`/dashboard/jobs/${job.jobUuid}`)}
                >
                  {job.name}
                </Button>
              ))}
              {unscheduledActive.length > 5 && (
                <Text type="secondary">+{unscheduledActive.length - 5} more</Text>
              )}
            </div>
          </Space>
        </Card>
      )}

      {/* ---- Row 2: Status Breakdown + Job Donut ---- */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        {/* Status breakdown bars */}
        <Col xs={24} lg={16}>
          <Card title={t.dashboard.executionsByDay}>
            {summaryLoading ? (
              <Skeleton active paragraph={{ rows: 6 }} />
            ) : summaryError ? (
              <Text type="secondary">{t.common.noData}</Text>
            ) : !summary || summary.total === 0 ? (
              <Text type="secondary">{t.common.noData}</Text>
            ) : (
              <Space direction="vertical" style={{ width: '100%' }} size="middle">
                {/* Succeeded bar */}
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                    <Space size={4}>
                      <span style={{ width: 10, height: 10, borderRadius: 2, background: '#52c41a', display: 'inline-block' }} />
                      <Text>{t.analytics.successful}</Text>
                    </Space>
                    <Text strong>{summary.succeeded}</Text>
                  </div>
                  <div style={{ background: 'var(--color-bg-layout, #f0f0f0)', borderRadius: 4, height: 20, overflow: 'hidden' }}>
                    <div style={{
                      width: `${(summary.succeeded / summary.total) * 100}%`,
                      height: '100%',
                      background: '#52c41a',
                      borderRadius: 4,
                      transition: 'width 0.5s',
                    }} />
                  </div>
                </div>

                {/* Failed bar */}
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                    <Space size={4}>
                      <span style={{ width: 10, height: 10, borderRadius: 2, background: '#ff4d4f', display: 'inline-block' }} />
                      <Text>{t.analytics.failed}</Text>
                    </Space>
                    <Text strong>{summary.failed}</Text>
                  </div>
                  <div style={{ background: 'var(--color-bg-layout, #f0f0f0)', borderRadius: 4, height: 20, overflow: 'hidden' }}>
                    <div style={{
                      width: `${summary.total > 0 ? (summary.failed / summary.total) * 100 : 0}%`,
                      height: '100%',
                      background: '#ff4d4f',
                      borderRadius: 4,
                      transition: 'width 0.5s',
                    }} />
                  </div>
                </div>

                {/* Error Rate */}
                <div style={{ marginTop: 8, textAlign: 'center' }}>
                  <Text type="secondary">
                    {t.dashboard.errorRate}: {summary.errorRate != null ? `${(summary.errorRate * 100).toFixed(1)}%` : '—'}
                    {summary.avgDurationMs != null ? `  ·  ${t.dashboard.avg}: ${(summary.avgDurationMs / 1000).toFixed(1)}s` : ''}
                    {summary.p95DurationMs != null ? `  ·  ${t.dashboard.p95}: ${(summary.p95DurationMs / 1000).toFixed(1)}s` : ''}
                  </Text>
                </div>
              </Space>
            )}
          </Card>
        </Col>

        {/* Right column: Job Status Donut + Success Rate */}
        <Col xs={24} lg={8}>
          {/* ---- Job Status Donut ---- */}
          <Card title={`${t.nav.jobs} ${t.common.status}`} style={{ marginBottom: 16 }}>
            <div style={{ textAlign: 'center' }}>
              <Progress
                type="circle"
                percent={Math.round((activeJobs / Math.max(totalJobs, 1)) * 100)}
                strokeColor="#52c41a"
                trailColor="var(--color-bg-layout, #f0f0f0)"
                size={100}
                format={() => `${activeJobs}/${totalJobs}`}
              />
              <div style={{ marginTop: 8 }}>
                <Space direction="vertical" size={2}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 24 }}>
                    <Space size={4}>
                      <span style={{ width: 10, height: 10, borderRadius: 2, background: '#52c41a', display: 'inline-block' }} />
                      <Text type="secondary">{t.dashboard.activeJobs}</Text>
                    </Space>
                    <Text strong>{activeJobs}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 24 }}>
                    <Space size={4}>
                      <span style={{ width: 10, height: 10, borderRadius: 2, background: '#d9d9d9', display: 'inline-block' }} />
                      <Text type="secondary">{t.dashboard.disabledJobs}</Text>
                    </Space>
                    <Text strong>{disabledJobs}</Text>
                  </div>
                  {archivedJobs > 0 && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', gap: 24 }}>
                      <Space size={4}>
                        <StopOutlined style={{ fontSize: 12 }} />
                        <Text type="secondary">Archived</Text>
                      </Space>
                      <Text strong>{archivedJobs}</Text>
                    </div>
                  )}
                </Space>
              </div>
            </div>
          </Card>

          {/* ---- Success Rate ---- */}
          <Card title={t.dashboard.statusDistribution}>
            {summaryError ? (
              <Text type="secondary">{t.common.noData}</Text>
            ) : (
              <>
                <div style={{ textAlign: 'center', marginBottom: 16 }}>
                  <Progress
                    type="circle"
                    percent={successRate}
                    strokeColor={successRate > 80 ? '#52c41a' : successRate > 50 ? '#faad14' : '#ff4d4f'}
                    size={140}
                  />
                  <div style={{ marginTop: 12 }}>
                    <Text strong style={{ fontSize: 18 }}>
                      {successRate}%
                    </Text>
                    <br />
                    <Text type="secondary">{t.dashboard.successRate}</Text>
                  </div>
                </div>

                <Space direction="vertical" style={{ width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Text type="secondary">{t.dashboard.totalJobs}</Text>
                    <Text strong>{summary?.total ?? '—'}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Space size={4}>
                      <span style={{ width: 12, height: 12, borderRadius: 3, background: '#52c41a', display: 'inline-block' }} />
                      <Text type="secondary">{t.analytics.successful}</Text>
                    </Space>
                    <Text strong>{summary?.succeeded ?? '—'}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Space size={4}>
                      <span style={{ width: 12, height: 12, borderRadius: 3, background: '#ff4d4f', display: 'inline-block' }} />
                      <Text type="secondary">{t.analytics.failed}</Text>
                    </Space>
                    <Text strong>{summary?.failed ?? '—'}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Space size={4}>
                      <ThunderboltOutlined />
                      <Text type="secondary">{t.dashboard.avgDuration}</Text>
                    </Space>
                    <Text strong>
                      {summary?.avgDurationMs != null ? `${(summary.avgDurationMs / 1000).toFixed(1)}s` : '—'}
                    </Text>
                  </div>
                </Space>
              </>
            )}
          </Card>
        </Col>
      </Row>

      {/* ---- Last Failures ---- */}
      {lastFailures.length > 0 && (
        <Card
          size="small"
          style={{ marginBottom: 24 }}
          title={
            <Space>
              <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />
              <span>{t.dashboard.lastFailures}</span>
            </Space>
          }
          extra={
            <Button type="link" size="small" onClick={() => navigate('/dashboard/executions')}>
              {t.dashboard.viewAll} <ArrowRightOutlined />
            </Button>
          }
        >
          <Table<ExecutionResponse>
            columns={[
              {
                title: '#',
                dataIndex: 'jobId',
                key: 'jobId',
                width: 60,
                render: (id: number) => <Text>#{id}</Text>,
              },
              {
                title: t.common.status,
                dataIndex: 'finalStatus',
                key: 'status',
                width: 100,
                render: (s: string) => <Tag color={statusColors[s] || 'default'}>{s}</Tag>,
              },
              {
                title: 'When',
                dataIndex: 'triggeredAt',
                key: 'triggeredAt',
                render: (d: string) => (
                  <Text type="secondary" style={{ fontSize: 12 }}>{formatRelative(d)}</Text>
                ),
              },
            ]}
            dataSource={lastFailures}
            rowKey="execUuid"
            size="small"
            pagination={false}
            showHeader={false}
            onRow={(r) => ({
              onClick: () => navigate(`/dashboard/executions/${r.execUuid}`),
              style: { cursor: 'pointer' },
            })}
          />
        </Card>
      )}

      {/* ---- Recent Executions ---- */}
      <Card
        title={t.dashboard.recentExecutions}
        extra={
          <Button type="link" onClick={() => navigate('/dashboard/executions')}>
            {t.dashboard.viewAll} <ArrowRightOutlined />
          </Button>
        }
      >
        <Table<ExecutionResponse>
          columns={executionColumns}
          dataSource={executionsPage?.items ?? []}
          rowKey="execUuid"
          size="small"
          pagination={false}
          loading={!executionsPage}
          onRow={(record) => ({
            onClick: () => navigate(`/dashboard/executions/${record.execUuid}`),
            style: { cursor: 'pointer' },
          })}
          locale={{ emptyText: t.common.noData }}
        />
      </Card>
    </div>
  );
}
