// ---------------------------------------------------------------------------
// CronInput — wrapper around react-cron-generator with human-readable preview
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Input, Space } from 'antd';
import CronGenerator from 'react-cron-generator';
import 'react-cron-generator/dist/cron-builder.css';

interface CronInputProps {
  value?: string;
  onChange?: (value: string) => void;
}

export default function CronInput({ value = '', onChange }: CronInputProps) {
  const [showGenerator, setShowGenerator] = useState(false);

  const handleChange = (_expr: string, getVal?: () => string) => {
    // react-cron-generator's onChange receives (newVal, getVal)
    // getVal() returns the standard 5-field cron expression
    const cronExpr = getVal ? getVal() : _expr;
    onChange?.(cronExpr);
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Input
        placeholder="* * * * *"
        value={value}
        onChange={(e) => onChange?.(e.target.value)}
        onFocus={() => setShowGenerator(true)}
      />

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
