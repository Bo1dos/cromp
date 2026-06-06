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

  // ---- Derived counts ---------------------------------------------------
  const totalJobs = jobs.length;
  const activeJobs = jobs.filter((j) => j.status === 'ACTIVE').length;
  const disabledJobs = jobs.filter((j) => j.status === 'DISABLED').length;

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
      title: 'Job',
      dataIndex: 'jobId',
      key: 'jobId',
      width: 80,
      render: (id: number) => <Text>#{id}</Text>,
    },
    {
      title: 'Triggered',
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

      {/* ---- Row 2: Status Breakdown + Success Rate ---- */}
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
                      <Text>Succeeded</Text>
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
                      <Text>Failed</Text>
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
                    Error Rate: {summary.errorRate != null ? `${(summary.errorRate * 100).toFixed(1)}%` : '—'}
                    {summary.avgDurationMs != null ? `  ·  Avg: ${(summary.avgDurationMs / 1000).toFixed(1)}s` : ''}
                    {summary.p95DurationMs != null ? `  ·  P95: ${(summary.p95DurationMs / 1000).toFixed(1)}s` : ''}
                  </Text>
                </div>
              </Space>
            )}
          </Card>
        </Col>

        {/* Success rate circle + legend */}
        <Col xs={24} lg={8}>
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
                    <Text type="secondary">Success Rate</Text>
                  </div>
                </div>

                <Space direction="vertical" style={{ width: '100%' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Text type="secondary">Total</Text>
                    <Text strong>{summary?.total ?? '—'}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Space size={4}>
                      <span style={{ width: 12, height: 12, borderRadius: 3, background: '#52c41a', display: 'inline-block' }} />
                      <Text type="secondary">Succeeded</Text>
                    </Space>
                    <Text strong>{summary?.succeeded ?? '—'}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Space size={4}>
                      <span style={{ width: 12, height: 12, borderRadius: 3, background: '#ff4d4f', display: 'inline-block' }} />
                      <Text type="secondary">Failed</Text>
                    </Space>
                    <Text strong>{summary?.failed ?? '—'}</Text>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Space size={4}>
                      <ThunderboltOutlined />
                      <Text type="secondary">Avg duration</Text>
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
