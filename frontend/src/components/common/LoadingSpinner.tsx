// ---------------------------------------------------------------------------
// LoadingSpinner — centered Spin for full-page / section loading states
// ---------------------------------------------------------------------------

import { Spin } from 'antd';
import type { SpinProps } from 'antd';

interface LoadingSpinnerProps {
  /** Optional tip text */
  tip?: string;
  /** Spin size */
  size?: SpinProps['size'];
  /** Minimum height of the container */
  minHeight?: string | number;
}

export default function LoadingSpinner({
  tip,
  size = 'large',
  minHeight = 200,
}: LoadingSpinnerProps) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight,
        padding: 24,
      }}
    >
      <Spin size={size} tip={tip}>
        {/* Spin requires a child for tip to render */}
        <div style={{ minWidth: 100, minHeight: 40 }} />
      </Spin>
    </div>
  );
}
