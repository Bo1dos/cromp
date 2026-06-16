// ---------------------------------------------------------------------------
// AnomaliesTable — anomaly list with severity filter and human-readable descriptions
// ---------------------------------------------------------------------------

import { useMemo, useState } from 'react';
import { Table, Tag, Select, Space, Empty, Tooltip, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { AnomalyItem, AnomalySeverity } from '@/types/analytics';
import { useLanguage } from '@/hooks/useLanguage';
import dayjs from 'dayjs';

const { Text } = Typography;

interface Props {
  data: AnomalyItem[] | undefined;
  loading?: boolean;
}

const SEVERITY_COLOR: Record<AnomalySeverity, string> = {
  LOW: 'default',
  MEDIUM: 'warning',
  HIGH: 'orange',
  CRITICAL: 'red',
};

const SEVERITY_ORDER: Record<AnomalySeverity, number> = {
  LOW: 0,
  MEDIUM: 1,
  HIGH: 2,
  CRITICAL: 3,
};

/** Format a metric value according to its type */
function formatMetricValue(metric: string, value: number): string {
  if (metric.includes('rate') || metric.includes('Rate')) {
    return `${(value * 100).toFixed(1)}%`;
  }
  if (metric.includes('duration') || metric.includes('Duration')) {
    if (value < 1000) return `${value.toFixed(1)}ms`;
    return `${(value / 1000).toFixed(2)}s`;
  }
  if (metric.includes('count') || metric.includes('Count')) {
    return String(Math.round(value));
  }
  if (metric.includes('executions')) {
    return String(Math.round(value));
  }
  // Default: 2 decimal places
  return value % 1 === 0 ? String(value) : value.toFixed(2);
}

/** Generate a human-readable description from an anomaly record */
function buildDescription(
  record: AnomalyItem,
  metricsMap: Record<string, string>,
): string {
  const label = metricsMap[record.metric] || record.metric;
  const val = formatMetricValue(record.metric, record.value);
  const minVal = formatMetricValue(record.metric, record.expectedMin);
  const maxVal = formatMetricValue(record.metric, record.expectedMax);
  return `${label}: ${val} (expected ${minVal}–${maxVal})`;
}

export default function AnomaliesTable({ data, loading }: Props) {
  const { t } = useLanguage();
  const [severityFilter, setSeverityFilter] = useState<AnomalySeverity | 'ALL'>('ALL');

  const metricsMap = t.analytics.metrics as Record<string, string>;

  const filtered = useMemo(() => {
    if (!data) return [];
    if (severityFilter === 'ALL') return data;
    return data.filter((a) => a.severity === severityFilter);
  }, [data, severityFilter]);

  const columns: ColumnsType<AnomalyItem> = [
    {
      title: t.analytics.job,
      dataIndex: 'jobId',
      key: 'jobId',
      sorter: (a: AnomalyItem, b: AnomalyItem) => a.jobId - b.jobId,
      render: (id: number) => `#${id}`,
    },
    {
      title: t.analytics.description,
      key: 'description',
      ellipsis: { showTitle: false },
      render: (_: unknown, record: AnomalyItem) => {
        const desc = buildDescription(record, metricsMap);
        return (
          <Tooltip title={desc} placement="topLeft">
            <Text>{desc}</Text>
          </Tooltip>
        );
      },
    },
    {
      title: t.analytics.severity,
      dataIndex: 'severity',
      key: 'severity',
      sorter: (a: AnomalyItem, b: AnomalyItem) =>
        SEVERITY_ORDER[a.severity] - SEVERITY_ORDER[b.severity],
      render: (sev: AnomalySeverity) => (
        <Tag color={SEVERITY_COLOR[sev]}>{sev}</Tag>
      ),
    },
    {
      title: t.analytics.detectedAt,
      dataIndex: 'detectedAt',
      key: 'detectedAt',
      sorter: (a: AnomalyItem, b: AnomalyItem) =>
        new Date(a.detectedAt).getTime() - new Date(b.detectedAt).getTime(),
      defaultSortOrder: 'descend',
      render: (iso: string) => dayjs(iso).format('DD.MM.YYYY HH:mm:ss'),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <span>{t.analytics.severity}:</span>
        <Select
          value={severityFilter}
          onChange={(v) => setSeverityFilter(v)}
          style={{ width: 140 }}
          options={[
            { value: 'ALL', label: t.analytics.allSeverities },
            { value: 'LOW', label: 'Low' },
            { value: 'MEDIUM', label: 'Medium' },
            { value: 'HIGH', label: 'High' },
            { value: 'CRITICAL', label: 'Critical' },
          ]}
        />
      </Space>

      <Table<AnomalyItem>
        columns={columns}
        dataSource={filtered}
        loading={loading}
        rowKey={(record) => `${record.jobId}-${record.detectedAt}`}
        locale={{ emptyText: <Empty description={t.analytics.noAnomalies} /> }}
        pagination={{ defaultPageSize: 10, showSizeChanger: true, pageSizeOptions: ['10', '20', '50'] }}
      />
    </div>
  );
}
