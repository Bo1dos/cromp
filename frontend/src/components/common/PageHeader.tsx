// ---------------------------------------------------------------------------
// PageHeader — reusable page header with title, breadcrumb & extra actions
// ---------------------------------------------------------------------------

import type { ReactNode } from 'react';
import { Typography, Breadcrumb, Divider, Space } from 'antd';
import type { BreadcrumbItemType } from 'antd/es/breadcrumb/Breadcrumb';

const { Title } = Typography;

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  breadcrumbs?: BreadcrumbItemType[];
  extra?: ReactNode;
}

export default function PageHeader({
  title,
  subtitle,
  breadcrumbs,
  extra,
}: PageHeaderProps) {
  return (
    <div style={{ marginBottom: 24 }}>
      {breadcrumbs && breadcrumbs.length > 0 && (
        <Breadcrumb
          items={breadcrumbs}
          style={{ marginBottom: 8 }}
        />
      )}

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
        }}
      >
        <div>
          <Title level={3} style={{ margin: 0 }}>
            {title}
          </Title>
          {subtitle && (
            <Typography.Paragraph
              type="secondary"
              style={{ margin: '4px 0 0 0' }}
            >
              {subtitle}
            </Typography.Paragraph>
          )}
        </div>

        {extra && <Space>{extra}</Space>}
      </div>

      <Divider style={{ margin: '16px 0 0 0' }} />
    </div>
  );
}
