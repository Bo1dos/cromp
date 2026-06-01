// ---------------------------------------------------------------------------
// ExecutionTimeSeries — LineChart: successful (green) vs failed (red) over time
// ---------------------------------------------------------------------------

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { Card, Spin, Empty } from 'antd';
import type { TimeSeriesPoint } from '@/types/analytics';

interface Props {
  data: TimeSeriesPoint[] | undefined;
  loading?: boolean;
}

export default function ExecutionTimeSeries({ data, loading }: Props) {
  return (
    <Card title="Execution Trends" style={{ height: '100%' }}>
      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}>
          <Spin size="large" />
        </div>
      ) : !data || data.length === 0 ? (
        <Empty description="No time series data" />
      ) : (
        <ResponsiveContainer width="100%" height={320}>
          <LineChart
            data={data}
            margin={{ top: 8, right: 16, left: 0, bottom: 8 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="date" tick={{ fontSize: 12 }} />
            <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
            <Tooltip />
            <Legend />
            <Line
              type="monotone"
              dataKey="success"
              name="Successful"
              stroke="#52c41a"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4 }}
            />
            <Line
              type="monotone"
              dataKey="failure"
              name="Failed"
              stroke="#ff4d4f"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4 }}
            />
          </LineChart>
        </ResponsiveContainer>
      )}
    </Card>
  );
}
