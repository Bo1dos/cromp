// ---------------------------------------------------------------------------
// AppHeader — top bar with organisation name, switcher, and user profile
// ---------------------------------------------------------------------------

import { useCallback } from 'react';
import { Layout, Avatar, Dropdown, Typography, Space } from 'antd';
import {
  UserOutlined,
  LogoutOutlined,
  ProfileOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { useNavigate } from 'react-router-dom';

import { useAuth } from '@/hooks/useAuth';
import { useOrganization } from '@/hooks/useOrganization';
import OrganizationSwitcher from './OrganizationSwitcher';

const { Header } = Layout;
const { Text } = Typography;

export default function AppHeader() {
  const { user, logout } = useAuth();
  const { activeOrganization } = useOrganization();
  const navigate = useNavigate();

  const handleLogout = useCallback(() => {
    logout();
  }, [logout]);

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <ProfileOutlined />,
      label: 'Профиль',
      onClick: () => navigate('/profile'),
    },
    { type: 'divider' },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Выход',
      danger: true,
      onClick: handleLogout,
    },
  ];

  const orgName = activeOrganization?.name ?? 'CaaS';

  return (
    <Header
      style={{
        height: 64,
        padding: '0 24px',
        background: '#fff',
        borderBottom: '1px solid #f0f0f0',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
      }}
    >
      {/* Left — organisation name */}
      <Text strong style={{ fontSize: 16 }}>
        {orgName}
      </Text>

      {/* Right — organisation switcher + user profile */}
      <Space size={16}>
        <OrganizationSwitcher />

        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Space
            style={{ cursor: 'pointer' }}
            size={8}
          >
            <Avatar
              size="small"
              icon={<UserOutlined />}
              src={user?.avatarUrl}
              alt={user?.name}
            />
            <Text>{user?.name ?? 'Пользователь'}</Text>
          </Space>
        </Dropdown>
      </Space>
    </Header>
  );
}
