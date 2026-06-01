// ---------------------------------------------------------------------------
// DurationDistribution — BarChart: execution duration buckets
// ---------------------------------------------------------------------------

import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from 'recharts';
import { Card, Spin, Empty } from 'antd';
import type { DurationBucket } from '@/types/analytics';

interface Props {
  data: DurationBucket[] | undefined;
  loading?: boolean;
}

const BAR_COLORS = [
  '#bae637',
  '#73d13d',
  '#36cfc9',
  '#40a9ff',
  '#597ef7',
  '#b37feb',
];

export default function DurationDistribution({ data, loading }: Props) {
  return (
    <Card title="Duration Distribution" style={{ height: '100%' }}>
      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}>
          <Spin size="large" />
        </div>
      ) : !data || data.length === 0 ? (
        <Empty description="No distribution data" />
      ) : (
        <ResponsiveContainer width="100%" height={320}>
          <BarChart
            data={data}
            margin={{ top: 8, right: 16, left: 0, bottom: 8 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="bucket" tick={{ fontSize: 12 }} />
            <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
            <Tooltip />
            <Bar dataKey="count" name="Executions" radius={[4, 4, 0, 0]}>
              {data.map((_, idx) => (
                <Cell key={idx} fill={BAR_COLORS[idx % BAR_COLORS.length]} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
