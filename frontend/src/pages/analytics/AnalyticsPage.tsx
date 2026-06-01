// ---------------------------------------------------------------------------
// AnalyticsPage — execution analytics dashboard
// ---------------------------------------------------------------------------

import { Row, Col, Card } from 'antd';
import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import SummaryCards from '@/components/analytics/SummaryCards';
import ExecutionTimeSeries from '@/components/analytics/ExecutionTimeSeries';
import DurationDistribution from '@/components/analytics/DurationDistribution';
import PredictionsTable from '@/components/analytics/PredictionsTable';
import AnomaliesTable from '@/components/analytics/AnomaliesTable';
import { useSummary, useTimeSeries, useDurationDistribution, usePredictions, useAnomalies } from '@/hooks/useAnalytics';
import { useOrganization } from '@/hooks/useOrganization';

export default function AnalyticsPage() {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.uuid ?? '';

  const { data: summary, isLoading: summaryLoading } = useSummary(orgId, '7d');
  const { data: timeSeries, isLoading: tsLoading } = useTimeSeries(orgId, '7d');
  const { data: distribution, isLoading: distLoading } = useDurationDistribution(orgId);
  const { data: predictions, isLoading: predLoading } = usePredictions(orgId);
  const { data: anomalies, isLoading: anomLoading } = useAnomalies(orgId, '', '');

  const isLoading = summaryLoading || tsLoading || distLoading;

  if (isLoading) {
    return <LoadingSpinner tip="Loading analytics…" minHeight={400} />;
  }

  return (
    <div>
      <PageHeader
        title="Analytics"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Analytics' }]}
      />

      <SummaryCards summary={summary!} />

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="Execution Time Series">
            <ExecutionTimeSeries data={timeSeries ?? []} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Duration Distribution">
            <DurationDistribution data={distribution ?? []} />
          </Card>
        </Col>
      </Row>

      <Card title="ML Predictions" style={{ marginTop: 24 }}>
        <PredictionsTable data={predictions?.predictions ?? []} loading={predLoading} />
      </Card>

      <Card title="Anomalies" style={{ marginTop: 24 }}>
        <AnomaliesTable data={anomalies?.anomalies ?? []} loading={anomLoading} />
      </Card>
    </div>
  );
}
