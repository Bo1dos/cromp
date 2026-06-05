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
    const parts = value.trim().split(/\s+/);
    // Accept 5-field, 6-field (with seconds), or 7-field (with seconds + year)
    // react-cron-generator produces 7-field expressions
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
            onChange={(expr: string) => {
              // react-cron-generator outputs Quartz-style cron (with ? and optional seconds/year).
              // Backend cron-utils expects UNIX cron (5-field, no ?, no seconds, no year).
              const parts = expr.trim().split(/\s+/);
              let normalized: string;
              if (parts.length >= 7) {
                // 7-field: drop seconds [0] and year [6], keep middle 5
                normalized = parts.slice(1, 6).join(' ');
              } else if (parts.length === 6) {
                // 6-field: drop seconds [0], keep remaining 5
                normalized = parts.slice(1).join(' ');
              } else {
                // 5-field or less: use as-is
                normalized = expr;
              }
              // Replace Quartz ? with * for UNIX compatibility
              normalized = normalized.replace(/\?/g, '*');
              onChange?.(normalized);
            }}
            value={value}
            showResultText
            showResultCron
          />
        </div>
      )}
    </Space>
  );
}
