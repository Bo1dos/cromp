// ---------------------------------------------------------------------------
// AppSider — collapsible side navigation with role-based menu items
// ---------------------------------------------------------------------------

import { useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Layout, Menu } from 'antd';
import {
  DashboardOutlined,
  ScheduleOutlined,
  HistoryOutlined,
  KeyOutlined,
  BarChartOutlined,
  TeamOutlined,
  AuditOutlined,
  UserOutlined,
} from '@ant-design/icons';

import { useOrganization } from '@/hooks/useOrganization';
import { useLanguage } from '@/hooks/useLanguage';
import { isOwnerOrAdmin, isAdmin } from '@/utils/permissions';

const { Sider } = Layout;

interface AppSiderProps {
  /** When true, renders without the Sider wrapper (used inside Drawer) */
  isDrawer?: boolean;
  /** Called after navigation (to close mobile drawer) */
  onNavigate?: () => void;
}

export default function AppSider({ isDrawer = false, onNavigate }: AppSiderProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const { activeRole } = useOrganization();
  const { t } = useLanguage();

  const menuItems = useMemo(() => {
    const items = [
      {
        key: '/dashboard',
        icon: <DashboardOutlined />,
        label: t.nav.dashboard,
      },
      {
        key: '/dashboard/jobs',
        icon: <ScheduleOutlined />,
        label: t.nav.jobs,
      },
      {
        key: '/dashboard/executions',
        icon: <HistoryOutlined />,
        label: t.nav.executions,
      },
      {
        key: '/dashboard/secrets',
        icon: <KeyOutlined />,
        label: t.nav.secrets,
      },
      {
        key: '/dashboard/analytics',
        icon: <BarChartOutlined />,
        label: t.nav.analytics,
      },
    ];

    // Members — visible to OWNER or ADMIN
    if (isOwnerOrAdmin(activeRole)) {
      items.push({
        key: '/dashboard/members',
        icon: <TeamOutlined />,
        label: t.nav.members,
      });
    }

    // Audit — visible to OWNER or ADMIN
    if (isOwnerOrAdmin(activeRole)) {
      items.push({
        key: '/dashboard/audit',
        icon: <AuditOutlined />,
        label: t.nav.audit,
      });
    }

    // Profile — always visible
    items.push({
      key: '/dashboard/profile',
      icon: <UserOutlined />,
      label: t.nav.profile,
    });

    return items;
  }, [activeRole, t]);

  /** Determine the selected key based on the current pathname */
  const selectedKey = useMemo(() => {
    const matched = menuItems.find((item) =>
      location.pathname.startsWith(item.key),
    );
    return matched?.key ?? '/dashboard/jobs';
  }, [location.pathname, menuItems]);

  const handleClick = ({ key }: { key: string }) => {
    navigate(key);
    onNavigate?.();
  };

  const navContent = (
    <>
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
        onClick={handleClick}
      />
    </>
  );

  if (isDrawer) {
    return <div style={{ height: '100%' }}>{navContent}</div>;
  }

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
      {navContent}
    </Sider>
  );
}
