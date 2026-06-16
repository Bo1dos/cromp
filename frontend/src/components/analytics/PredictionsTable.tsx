// ---------------------------------------------------------------------------
// PredictionsTable — ML predictions with color-coded failure probability
// ---------------------------------------------------------------------------

import { Table, Progress, Tag, Empty } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { PredictionItem } from '@/types/analytics';
import { useLanguage } from '@/hooks/useLanguage';
import dayjs from 'dayjs';

interface Props {
  data: PredictionItem[] | undefined;
  loading?: boolean;
}

function getProbabilityColor(prob: number): string {
  if (prob < 0.2) return '#52c41a';
  if (prob <= 0.5) return '#faad14';
  return '#ff4d4f';
}

export default function PredictionsTable({ data, loading }: Props) {
  const { t } = useLanguage();

  const columns: ColumnsType<PredictionItem> = [
    {
      title: t.analytics.job,
      dataIndex: 'jobId',
      key: 'jobId',
      sorter: (a: PredictionItem, b: PredictionItem) => a.jobId - b.jobId,
      render: (id: number) => `#${id}`,
    },
    {
      title: t.analytics.failureProbability,
      dataIndex: 'failureProbability',
      key: 'failureProbability',
      sorter: (a: PredictionItem, b: PredictionItem) => a.failureProbability - b.failureProbability,
      defaultSortOrder: 'descend',
      render: (prob: number) => (
        <Progress
          percent={Math.round(prob * 100)}
          size="small"
          strokeColor={getProbabilityColor(prob)}
          style={{ minWidth: 120 }}
        />
      ),
    },
    {
      title: t.analytics.expectedDuration,
      dataIndex: 'expectedDurationMs',
      key: 'expectedDurationMs',
      sorter: (a: PredictionItem, b: PredictionItem) => (a.expectedDurationMs ?? 0) - (b.expectedDurationMs ?? 0),
      render: (ms: number | null) => {
        if (ms == null) return '—';
        if (ms < 1000) return `${ms}ms`;
        return `${(ms / 1000).toFixed(1)}s`;
      },
    },
    {
      title: t.analytics.confidence,
      dataIndex: 'confidence',
      key: 'confidence',
      sorter: (a: PredictionItem, b: PredictionItem) => a.confidence - b.confidence,
      render: (conf: number) => (
        <Progress
          percent={Math.round(conf * 100)}
          size="small"
          strokeColor="#1677ff"
          style={{ minWidth: 100 }}
        />
      ),
    },
    {
      title: t.analytics.nextRun,
      dataIndex: 'nextRunAt',
      key: 'nextRunAt',
      sorter: (a: PredictionItem, b: PredictionItem) =>
        new Date(a.nextRunAt ?? 0).getTime() -
        new Date(b.nextRunAt ?? 0).getTime(),
      render: (iso: string | null) => {
        if (!iso) return '—';
        return dayjs(iso).format('DD.MM.YYYY HH:mm');
      },
    },
  ];

  return (
    <Table<PredictionItem>
      columns={columns}
      dataSource={data}
      loading={loading}
      rowKey="jobId"
      locale={{ emptyText: <Empty description={t.analytics.noPredictions} /> }}
      pagination={{ defaultPageSize: 10, showSizeChanger: true, pageSizeOptions: ['10', '20', '50'] }}
    />
  );
}
