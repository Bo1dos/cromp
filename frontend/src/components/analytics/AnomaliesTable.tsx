// ---------------------------------------------------------------------------
// AnomaliesTable — anomaly list with severity filter
// ---------------------------------------------------------------------------

import { useMemo, useState } from 'react';
import { Table, Tag, Select, Space, Empty } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { AnomalyItem, AnomalySeverity } from '@/types/analytics';
import dayjs from 'dayjs';

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

export default function AnomaliesTable({ data, loading }: Props) {
  const [severityFilter, setSeverityFilter] = useState<AnomalySeverity | 'ALL'>(
    'ALL',
  );

  const filtered = useMemo(() => {
    if (!data) return [];
    if (severityFilter === 'ALL') return data;
    return data.filter((a) => a.severity === severityFilter);
  }, [data, severityFilter]);

  const columns: ColumnsType<AnomalyItem> = [
    {
      title: 'Job',
      dataIndex: 'jobName',
      key: 'jobName',
      sorter: (a: AnomalyItem, b: AnomalyItem) => a.jobName.localeCompare(b.jobName),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Severity',
      dataIndex: 'severity',
      key: 'severity',
      sorter: (a: AnomalyItem, b: AnomalyItem) => SEVERITY_ORDER[a.severity] - SEVERITY_ORDER[b.severity],
      render: (sev: AnomalySeverity) => (
        <Tag color={SEVERITY_COLOR[sev]}>{sev}</Tag>
      ),
    },
    {
      title: 'Detected At',
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
        <span>Severity:</span>
        <Select
          value={severityFilter}
          onChange={(v) => setSeverityFilter(v)}
          style={{ width: 140 }}
          options={[
            { value: 'ALL', label: 'All' },
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
        rowKey="uuid"
        locale={{ emptyText: <Empty description="No anomalies detected" /> }}
        pagination={false}
      />
    </div>
  );
}
