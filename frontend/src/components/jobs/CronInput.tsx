// ---------------------------------------------------------------------------
// CronInput — wrapper around react-cron-generator with human-readable preview
// ---------------------------------------------------------------------------

import { useState, useMemo } from 'react';
import { Input, Space, Button, List, Typography, Popover } from 'antd';
import { ClockCircleOutlined } from '@ant-design/icons';
import CronGenerator from 'react-cron-generator';
import 'react-cron-generator/dist/cron-builder.css';
import { CronExpressionParser } from 'cron-parser';
import dayjs from 'dayjs';

const { Text } = Typography;

interface CronInputProps {
  value?: string;
  onChange?: (value: string) => void;
}

export default function CronInput({ value = '', onChange }: CronInputProps) {
  const [showGenerator, setShowGenerator] = useState(false);
  const [previewOpen, setPreviewOpen] = useState(false);

  // ---- Parse next 5 run dates -----------------------------------------
  const nextRuns = useMemo<string[]>(() => {
    if (!value || value.split(/\s+/).length !== 5) return [];
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
        onFocus={() => setShowGenerator(true)}
      />

      <Space>
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

      {showGenerator && (
        <div
          style={{
            border: '1px solid #d9d9d9',
            borderRadius: 6,
            padding: 16,
            background: '#fafafa',
          }}
        >
          <CronGenerator
            onChange={(expr: string) => onChange?.(expr)}
            value={value}
            showResultText
            showResultCron
          />
        </div>
      )}
    </Space>
  );
}
