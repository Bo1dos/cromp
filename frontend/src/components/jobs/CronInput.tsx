// ---------------------------------------------------------------------------
// CronInput — manual input with preset buttons + live preview
// ---------------------------------------------------------------------------

import { useState, useMemo } from 'react';
import { Input, Space, Button, List, Typography, Popover, Tag } from 'antd';
import { ClockCircleOutlined, DownOutlined } from '@ant-design/icons';
import { CronExpressionParser } from 'cron-parser';
import dayjs from 'dayjs';

const { Text } = Typography;

interface CronInputProps {
  value?: string;
  onChange?: (value: string) => void;
}

const PRESETS: { label: string; value: string }[] = [
  { label: 'Every minute',      value: '* * * * *' },
  { label: 'Every 5 min',       value: '*/5 * * * *' },
  { label: 'Every 15 min',      value: '*/15 * * * *' },
  { label: 'Every 30 min',      value: '*/30 * * * *' },
  { label: 'Every hour',        value: '0 * * * *' },
  { label: 'Every 6 hours',     value: '0 */6 * * *' },
  { label: 'Every 12 hours',    value: '0 */12 * * *' },
  { label: 'Daily at midnight', value: '0 0 * * *' },
  { label: 'Mon-Fri at 9 AM',   value: '0 9 * * 1-5' },
  { label: '1st of month 3 AM', value: '0 3 1 * *' },
];

export default function CronInput({ value = '', onChange }: CronInputProps) {
  const [showPresets, setShowPresets] = useState(false);
  const [previewOpen, setPreviewOpen] = useState(false);

  // ---- Parse next 5 run dates -----------------------------------------
  const nextRuns = useMemo<string[]>(() => {
    const parts = value.trim().split(/\s+/);
    if (!value || parts.length < 5 || parts.length > 7) return [];
    try {
      const interval = CronExpressionParser.parse(value);
      const dates: string[] = [];
      for (let i = 0; i < 5; i++) {
        const next = interval.next().toISOString();
        if (next) dates.push(next);
      }
      return dates;
    } catch {
      return [];
    }
  }, [value]);

  const previewContent = useMemo(() => {
    if (nextRuns.length === 0) {
      return <Text type="secondary">Enter a valid cron expression to see preview.</Text>;
    }
    return (
      <List
        size="small"
        dataSource={nextRuns}
        renderItem={(date) => (
          <List.Item>
            <Text>{dayjs(date).format('MMMM D, YYYY HH:mm')}</Text>
          </List.Item>
        )}
      />
    );
  }, [nextRuns]);

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Input
        placeholder="* * * * *"
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
      />

      <Space wrap>
        <Button
          size="small"
          icon={<DownOutlined rotate={showPresets ? 180 : 0} />}
          onClick={() => setShowPresets(!showPresets)}
        >
          Quick presets
        </Button>
        <Popover
          content={previewContent}
          title="Next 5 runs"
          trigger="click"
          open={previewOpen}
          onOpenChange={setPreviewOpen}
        >
          <Button icon={<ClockCircleOutlined />} size="small" disabled={!value}>
            Preview next runs
          </Button>
        </Popover>
      </Space>

      {showPresets && (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
          {PRESETS.map((p) => (
            <Tag.CheckableTag
              key={p.value}
              checked={value === p.value}
              onChange={() => onChange?.(p.value)}
              style={{ cursor: 'pointer', padding: '4px 12px' }}
            >
              {p.label}
            </Tag.CheckableTag>
          ))}
        </div>
      )}
    </Space>
  );
}
