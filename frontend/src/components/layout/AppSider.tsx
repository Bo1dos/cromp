// ---------------------------------------------------------------------------
// AppSider — collapsible side navigation with role-based menu items
// ---------------------------------------------------------------------------

import { useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import {
  ScheduleOutlined,
  HistoryOutlined,
  KeyOutlined,
  BarChartOutlined,
  TeamOutlined,
  AuditOutlined,
} from '@ant-design/icons';

import { useOrganization } from '@/hooks/useOrganization';
import { isOwnerOrAdmin, isAdmin } from '@/utils/permissions';

const { Sider } = Layout;

export default function AppSider() {
  const location = useLocation();
  const navigate = useNavigate();
  const { activeRole } = useOrganization();

  const menuItems = useMemo(() => {
    const items = [
      {
        key: '/dashboard/jobs',
        icon: <ScheduleOutlined />,
        label: 'Jobs',
      },
      {
        key: '/dashboard/executions',
        icon: <HistoryOutlined />,
        label: 'Executions',
      },
      {
        key: '/dashboard/secrets',
        icon: <KeyOutlined />,
        label: 'Secrets',
      },
      {
        key: '/dashboard/analytics',
        icon: <BarChartOutlined />,
        label: 'Analytics',
      },
    ];

    // Members — visible to OWNER or ADMIN
    if (isOwnerOrAdmin(activeRole)) {
      items.push({
        key: '/dashboard/members',
        icon: <TeamOutlined />,
        label: 'Members',
      });
    }

    // Audit — visible only to ADMIN
    if (isAdmin(activeRole)) {
      items.push({
        key: '/dashboard/audit',
        icon: <AuditOutlined />,
        label: 'Audit',
      });
    }

    return items;
  }, [activeRole]);

  /** Determine the selected key based on the current pathname */
  const selectedKey = useMemo(() => {
    // Match the most specific path first
    const matched = menuItems.find((item) =>
      location.pathname.startsWith(item.key),
    );
    return matched?.key ?? '/dashboard/jobs';
  }, [location.pathname, menuItems]);

  return (
    <Sider
      width={240}
      collapsible
      breakpoint="lg"
      theme="dark"
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
      }}
    >
      {/* Logo */}
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <span
          style={{
            color: '#fff',
            fontSize: 20,
            fontWeight: 700,
            letterSpacing: 1,
          }}
        >
          CaaS
        </span>
      </div>

      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={[selectedKey]}
        items={menuItems}
        onClick={({ key }) => navigate(key)}
      />
    </Sider>
  );
}
