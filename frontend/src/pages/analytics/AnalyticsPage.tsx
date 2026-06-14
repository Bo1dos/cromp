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
import { useLanguage } from '@/hooks/useLanguage';

export default function AnalyticsPage() {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid ?? '';
  const { t } = useLanguage();

  const { data: summary, isLoading: summaryLoading } = useSummary(orgId, '7d');
  const { data: predictions, isLoading: predLoading } = usePredictions(orgId);
  const { data: anomalies, isLoading: anomLoading } = useAnomalies(orgId, '', '');

  const isLoading = summaryLoading;

  if (isLoading) {
    return <LoadingSpinner tip={t.analytics.loadingAnalytics} minHeight={400} />;
  }

  return (
    <div>
      <PageHeader
        title={t.nav.analytics}
        breadcrumbs={[{ title: t.nav.dashboard }, { title: t.nav.analytics }]}
      />

      {summary && <SummaryCards summary={summary} />}

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24}>
          <Card title={t.analytics.mlPredictions}>
            <PredictionsTable data={predictions?.predictions ?? []} loading={predLoading} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24}>
          <Card title={t.analytics.anomalies}>
            <AnomaliesTable data={anomalies?.anomalies ?? []} loading={anomLoading} />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
