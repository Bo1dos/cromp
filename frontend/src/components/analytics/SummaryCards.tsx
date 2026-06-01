// ---------------------------------------------------------------------------
// SummaryCards — 4 stat cards: Total, Successful, Failed, Avg Duration
// ---------------------------------------------------------------------------

import { Row, Col, Card, Statistic } from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import type { SummaryResponse } from '@/types/analytics';

interface Props {
  summary: SummaryResponse;
  loading?: boolean;
}

export default function SummaryCards({ summary, loading }: Props) {
  const {
    totalExecutions,
    successfulExecutions,
    failedExecutions,
    avgDurationMs,
  } = summary;

  const successRate =
    totalExecutions > 0
      ? ((successfulExecutions / totalExecutions) * 100).toFixed(1)
      : '0';

  const failureRate =
    totalExecutions > 0
      ? ((failedExecutions / totalExecutions) * 100).toFixed(1)
      : '0';

  const formatDuration = (ms: number): string => {
    if (ms < 1000) return `${ms}ms`;
    return `${(ms / 1000).toFixed(1)}s`;
  };

  return (
    <Row gutter={[16, 16]}>
      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false}>
          <Statistic
            title="Total Executions"
            value={totalExecutions}
            prefix={<BarChartOutlined style={{ color: '#1677ff' }} />}
            loading={loading}
            valueStyle={{ color: '#1677ff' }}
          />
        </Card>
      </Col>

      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false}>
          <Statistic
            title="Successful"
            value={successfulExecutions}
            suffix={`/ ${successRate}%`}
            prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
            loading={loading}
            valueStyle={{ color: '#52c41a' }}
          />
        </Card>
      </Col>

      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false}>
          <Statistic
            title="Failed"
            value={failedExecutions}
            suffix={`/ ${failureRate}%`}
            prefix={<CloseCircleOutlined style={{ color: '#ff4d4f' }} />}
            loading={loading}
            valueStyle={{ color: '#ff4d4f' }}
          />
        </Card>
      </Col>

      <Col xs={24} sm={12} lg={6}>
        <Card bordered={false}>
          <Statistic
            title="Avg Duration"
            value={formatDuration(avgDurationMs)}
            prefix={<ClockCircleOutlined style={{ color: '#fa8c16' }} />}
            loading={loading}
            valueStyle={{ color: '#fa8c16' }}
          />
        </Card>
      </Col>
    </Row>
  );
}
