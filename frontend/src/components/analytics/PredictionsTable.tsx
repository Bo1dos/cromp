// ---------------------------------------------------------------------------
// PredictionsTable — ML predictions with color-coded failure probability
// ---------------------------------------------------------------------------

import { Table, Progress, Tag, Empty } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { PredictionItem } from '@/types/analytics';
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

const columns: ColumnsType<PredictionItem> = [
  {
    title: 'Job',
    dataIndex: 'jobName',
    key: 'jobName',
    sorter: (a: PredictionItem, b: PredictionItem) => a.jobName.localeCompare(b.jobName),
  },
  {
    title: 'Failure Probability',
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
    title: 'Expected Duration',
    dataIndex: 'expectedDurationMs',
    key: 'expectedDurationMs',
    sorter: (a: PredictionItem, b: PredictionItem) => a.expectedDurationMs - b.expectedDurationMs,
    render: (ms: number) => {
      if (ms < 1000) return `${ms}ms`;
      return `${(ms / 1000).toFixed(1)}s`;
    },
  },
  {
    title: 'Confidence',
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
    title: 'Next Run',
    dataIndex: 'nextExpectedRunAt',
    key: 'nextExpectedRunAt',
    sorter: (a: PredictionItem, b: PredictionItem) =>
      new Date(a.nextExpectedRunAt).getTime() -
      new Date(b.nextExpectedRunAt).getTime(),
    render: (iso: string) => {
      const d = dayjs(iso);
      const now = dayjs();
      const diffDays = d.diff(now, 'day');
      return (
        <span>
          {d.format('DD.MM.YYYY HH:mm')}
          {diffDays <= 3 && diffDays >= 0 && (
            <Tag color="blue" style={{ marginLeft: 8 }}>
              soon
            </Tag>
          )}
        </span>
      );
    },
  },
];

export default function PredictionsTable({ data, loading }: Props) {
  return (
    <Table<PredictionItem>
      columns={columns}
      dataSource={data}
      loading={loading}
      rowKey="jobUuid"
      locale={{ emptyText: <Empty description="No predictions available" /> }}
      pagination={false}
    />
  );
}
