// ---------------------------------------------------------------------------
// AnalyticsPage — execution analytics dashboard
// ---------------------------------------------------------------------------

import { useMemo } from 'react';
import { Row, Col, Card, Typography, Table } from 'antd';
import {
  WarningOutlined,
  ClockCircleOutlined,
  BugOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import SummaryCards from '@/components/analytics/SummaryCards';
import PredictionsTable from '@/components/analytics/PredictionsTable';
import AnomaliesTable from '@/components/analytics/AnomaliesTable';
import { useSummary, usePredictions, useAnomalies } from '@/hooks/useAnalytics';
import { useOrganization } from '@/hooks/useOrganization';
import { useLanguage } from '@/hooks/useLanguage';
import type { AnomalyItem } from '@/types/analytics';

const { Text } = Typography;

interface Recommendation {
  jobId: number;
  icon: React.ReactNode;
  color: string;
  text: string;
}

function buildRecommendations(
  anomalies: AnomalyItem[] | undefined,
  metricsMap: Record<string, string>,
  t: any,
): Recommendation[] {
  if (!anomalies || anomalies.length === 0) return [];

  const recTemplates = t.analytics.recs;

  // Group by (jobId, metric) — keep only the most severe per group
  const best: Record<string, AnomalyItem> = {};
  const severityOrder: Record<string, number> = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };

  for (const a of anomalies) {
    const key = `${a.jobId}|${a.metric}`;
    const existing = best[key];
    if (!existing
      || severityOrder[a.severity] > severityOrder[existing.severity]
      || (severityOrder[a.severity] === severityOrder[existing.severity] && a.value > existing.value)
    ) {
      best[key] = a;
    }
  }

  const recs: Recommendation[] = [];
  for (const a of Object.values(best)) {
    const label = metricsMap[a.metric] || a.metric;
    const jobRef = `#${a.jobId}`;

    if (a.metric === 'error_rate' && a.value > 0.3) {
      recs.push({
        jobId: a.jobId, icon: <BugOutlined />, color: 'red',
        text: recTemplates.highErrorRate
          .replace('{jobRef}', jobRef)
          .replace('{label}', label)
          .replace('{value}', `${(a.value * 100).toFixed(1)}%`)
          .replace('{threshold}', `${(a.expectedMax * 100).toFixed(0)}%`),
      });
    }
    if (a.metric === 'avg_duration_ms' && a.value > a.expectedMax) {
      const valSec = (a.value / 1000).toFixed(1);
      const maxSec = (a.expectedMax / 1000).toFixed(1);
      recs.push({
        jobId: a.jobId, icon: <ClockCircleOutlined />, color: 'orange',
        text: recTemplates.highDuration
          .replace('{jobRef}', jobRef)
          .replace('{label}', label)
          .replace('{value}', `${valSec}s`)
          .replace('{threshold}', `${maxSec}s`),
      });
    }
    if (a.metric === 'failure_count' && a.value > 0) {
      recs.push({
        jobId: a.jobId, icon: <WarningOutlined />, color: 'volcano',
        text: recTemplates.failures
          .replace('{jobRef}', jobRef)
          .replace('{value}', String(Math.round(a.value))),
      });
    }
    if (a.metric === 'success_rate' && a.value < a.expectedMin) {
      recs.push({
        jobId: a.jobId, icon: <InfoCircleOutlined />, color: 'gold',
        text: recTemplates.lowSuccessRate
          .replace('{jobRef}', jobRef)
          .replace('{value}', `${(a.value * 100).toFixed(1)}%`)
          .replace('{threshold}', `${(a.expectedMin * 100).toFixed(0)}%`),
      });
    }
    if (a.severity === 'CRITICAL' || a.severity === 'HIGH') {
      recs.push({
        jobId: a.jobId, icon: <WarningOutlined />, color: 'red',
        text: recTemplates.criticalAnomaly
          .replace('{jobRef}', jobRef)
          .replace('{label}', label)
          .replace('{severity}', a.severity === 'CRITICAL'
            ? recTemplates.severity_critical
            : recTemplates.severity_high),
      });
    }
  }

  // Sort: CRITICAL/HIGH first, then by jobId
  return recs.sort((a, b) => {
    const aCrit = a.color === 'red' ? 0 : 1;
    const bCrit = b.color === 'red' ? 0 : 1;
    if (aCrit !== bCrit) return aCrit - bCrit;
    return a.jobId - b.jobId;
  });
}

export default function AnalyticsPage() {
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid ?? '';
  const { t } = useLanguage();

  const { data: summary, isLoading: summaryLoading } = useSummary(orgId, '7d');
  const { data: predictions, isLoading: predLoading } = usePredictions(orgId);
  const { data: anomalies, isLoading: anomLoading } = useAnomalies(orgId, '', '');

  const metricsMap = t.analytics.metrics as Record<string, string>;
  const recommendations = useMemo(
    () => buildRecommendations(anomalies?.anomalies, metricsMap, t),
    [anomalies, metricsMap, t],
  );

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

      {/* Recommendations */}
      {recommendations.length > 0 && (
        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24}>
            <Card
              title={
                <span>
                  <InfoCircleOutlined style={{ marginRight: 8, color: '#1677ff' }} />
                  {t.analytics.recommendations}
                </span>
              }
            >
              <Table
                dataSource={recommendations}
                rowKey={(r) => `${r.jobId}-${r.text.slice(0, 30)}`}
                pagination={{ defaultPageSize: 10, showSizeChanger: true, pageSizeOptions: ['10', '20', '50'] }}
                size="small"
                showHeader={false}
                columns={[
                  {
                    key: 'text',
                    render: (_: unknown, rec: Recommendation) => (
                      <Text style={{ color: rec.color === 'red' ? '#ff4d4f' : undefined }}>
                        <span style={{ marginRight: 8 }}>{rec.icon}</span>
                        {rec.text}
                      </Text>
                    ),
                  },
                ]}
              />
            </Card>
          </Col>
        </Row>
      )}

      {!anomalies?.anomalies?.length && !predictions?.predictions?.length && (
        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24}>
            <Card>
              <Text type="secondary">
                <InfoCircleOutlined style={{ marginRight: 8 }} />
                {t.analytics.noDataYet}
              </Text>
            </Card>
          </Col>
        </Row>
      )}
    </div>
  );
}
