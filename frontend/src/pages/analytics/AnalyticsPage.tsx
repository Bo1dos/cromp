// ---------------------------------------------------------------------------
// AnalyticsPage — main analytics dashboard
// ---------------------------------------------------------------------------

import { useState, useCallback } from 'react';
import { Row, Col, Tabs, Select, Typography, Space, Card, Alert } from 'antd';
import {
  useSummary,
  usePredictions,
  useAnomalies,
  useTimeSeries,
  useDurationDistribution,
} from '@/hooks/useAnalytics';
import { useOrganization } from '@/hooks/useOrganization';
import SummaryCards from '@/components/analytics/SummaryCards';
import ExecutionTimeSeries from '@/components/analytics/ExecutionTimeSeries';
import DurationDistribution from '@/components/analytics/DurationDistribution';
import PredictionsTable from '@/components/analytics/PredictionsTable';
import AnomaliesTable from '@/components/analytics/AnomaliesTable';

const { Title } = Typography;

const PERIODS = [
  { value: '7d', label: 'Last 7 days' },
  { value: '30d', label: 'Last 30 days' },
  { value: '90d', label: 'Last 90 days' },
];

export default function AnalyticsPage() {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.uuid ?? '';

  const [period, setPeriod] = useState('7d');
  const [activeTab, setActiveTab] = useState('predictions');

  const fromDate = new Date(
    Date.now() -
      (period === '30d' ? 30 : period === '90d' ? 90 : 7) * 86400000,
  ).toISOString();
  const toDate = new Date().toISOString();

  // Queries
  const {
    data: summary,
    isLoading: summaryLoading,
    isError: summaryError,
  } = useSummary(orgId, period);

  const {
    data: predictions,
    isLoading: predLoading,
  } = usePredictions(orgId);

  const {
    data: anomalies,
    isLoading: anomLoading,
  } = useAnomalies(orgId, fromDate, toDate);

  const {
    data: timeSeries,
    isLoading: tsLoading,
  } = useTimeSeries(orgId, period);

  const {
    data: durationDist,
    isLoading: ddLoading,
  } = useDurationDistribution(orgId);

  const handlePeriodChange = useCallback((val: string) => {
    setPeriod(val);
  }, []);

  const handleTabChange = useCallback((key: string) => {
    setActiveTab(key);
  }, []);

  return (
    <div style={{ padding: '0 0 24px' }}>
      {/* ---- Header ---- */}
      <Space
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 24,
          flexWrap: 'wrap',
          gap: 12,
        }}
      >
        <Title level={3} style={{ margin: 0 }}>
          Analytics
        </Title>
        <Select
          value={period}
          onChange={handlePeriodChange}
          style={{ width: 160 }}
          options={PERIODS}
        />
      </Space>

      {/* ---- Error banner ---- */}
      {summaryError && (
        <Alert
          message="Failed to load summary data"
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {/* ---- Summary Cards ---- */}
      {summary && (
        <SummaryCards summary={summary} loading={summaryLoading} />
      )}

      {/* ---- Charts Row ---- */}
      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <ExecutionTimeSeries data={timeSeries} loading={tsLoading} />
        </Col>
        <Col xs={24} lg={12}>
          <DurationDistribution data={durationDist} loading={ddLoading} />
        </Col>
      </Row>

      {/* ---- Tabs: Predictions / Anomalies ---- */}
      <Card style={{ marginTop: 24 }} bodyStyle={{ paddingTop: 0 }}>
        <Tabs
          activeKey={activeTab}
          onChange={handleTabChange}
          items={[
            {
              key: 'predictions',
              label: `Predictions${predictions ? ` (${predictions.predictions.length})` : ''}`,
              children: (
                <PredictionsTable
                  data={predictions?.predictions}
                  loading={predLoading}
                />
              ),
            },
            {
              key: 'anomalies',
              label: `Anomalies${anomalies ? ` (${anomalies.anomalies.length})` : ''}`,
              children: (
                <AnomaliesTable
                  data={anomalies?.anomalies}
                  loading={anomLoading}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}
