// ---------------------------------------------------------------------------
// EmptyState — reusable empty state with optional action button
// ---------------------------------------------------------------------------

import type { ReactNode } from 'react';
import { Empty, Button, Typography } from 'antd';
import type { EmptyProps } from 'antd';

const { Text } = Typography;

interface EmptyStateProps {
  /** Main description text */
  description?: string;
  /** Optional subtitle / hint */
  hint?: string;
  /** Optional action button label */
  actionLabel?: string;
  /** Action button handler */
  onAction?: () => void;
  /** Custom image (Ant Empty preset or custom ReactNode) */
  image?: EmptyProps['image'];
  /** Additional content rendered below the description */
  children?: ReactNode;
}

export default function EmptyState({
  description = 'No data',
  hint,
  actionLabel,
  onAction,
  image = Empty.PRESENTED_IMAGE_SIMPLE,
  children,
}: EmptyStateProps) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        padding: '64px 24px',
      }}
    >
      <Empty
        image={image}
        description={
          <div>
            <Text type="secondary" style={{ fontSize: 14 }}>
              {description}
            </Text>
            {hint && (
              <>
                <br />
                <Text type="secondary" style={{ fontSize: 13 }}>
                  {hint}
                </Text>
              </>
            )}
          </div>
        }
      >
        {actionLabel && onAction && (
          <Button type="primary" onClick={onAction}>
            {actionLabel}
          </Button>
        )}
        {children}
      </Empty>
    </div>
  );
}
