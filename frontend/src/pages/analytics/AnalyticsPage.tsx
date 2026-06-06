// ---------------------------------------------------------------------------
// AnalyticsPage — execution analytics dashboard
// ---------------------------------------------------------------------------

import { Row, Col, Card } from 'antd';
import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import SummaryCards from '@/components/analytics/SummaryCards';
import PredictionsTable from '@/components/analytics/PredictionsTable';
import AnomaliesTable from '@/components/analytics/AnomaliesTable';
import { useSummary, usePredictions, useAnomalies } from '@/hooks/useAnalytics';
import { useOrganization } from '@/hooks/useOrganization';

export default function AnalyticsPage() {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid ?? '';

  const { data: summary, isLoading: summaryLoading } = useSummary(orgId, '7d');
  const { data: predictions, isLoading: predLoading } = usePredictions(orgId);
  const { data: anomalies, isLoading: anomLoading } = useAnomalies(orgId, '', '');

  const isLoading = summaryLoading;

  if (isLoading) {
    return <LoadingSpinner tip="Loading analytics…" minHeight={400} />;
  }

  return (
    <div>
      <PageHeader
        title="Analytics"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Analytics' }]}
      />

      {summary && <SummaryCards summary={summary} />}

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24}>
          <Card title="ML Predictions">
            <PredictionsTable data={predictions?.predictions ?? []} loading={predLoading} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24}>
          <Card title="Anomalies">
            <AnomaliesTable data={anomalies?.anomalies ?? []} loading={anomLoading} />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
