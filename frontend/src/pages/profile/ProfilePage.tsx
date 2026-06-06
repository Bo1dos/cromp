// ---------------------------------------------------------------------------
// ProfilePage — user profile & settings (tabs: Profile | Settings)
// ---------------------------------------------------------------------------

import { useState } from 'react';
import {
  Tabs,
  Descriptions,
  Avatar,
  Typography,
  Card,
  Space,
  Tag,
} from 'antd';
import { UserOutlined } from '@ant-design/icons';

import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import { useAuth } from '@/hooks/useAuth';
import { useOrganization } from '@/hooks/useOrganization';
import { formatDateFull } from '@/utils/formatters';
import SettingsTab from './SettingsTab';

const { Text, Title } = Typography;

export default function ProfilePage() {
  const { user, isLoading } = useAuth();
  const { memberships } = useOrganization();
  const [activeTab, setActiveTab] = useState('profile');

  if (isLoading || !user) {
    return <LoadingSpinner tip="Loading profile…" />;
  }

  const orgCount = memberships.length;

  return (
    <div>
      <PageHeader
        title="Profile"
        breadcrumbs={[{ title: 'Profile' }]}
      />

      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={[
          {
            key: 'profile',
            label: 'Profile',
            children: (
              <Space direction="vertical" size="large" style={{ width: '100%' }}>
                {/* ---- Identity card ---- */}
                <Card>
                  <Space align="start" size={24}>
                    <Avatar
                      size={80}
                      icon={<UserOutlined />}
                      src={user.profile?.avatarUrl as string | undefined}
                    />
                    <div>
                      <Title level={4} style={{ marginTop: 0 }}>
                        {user.displayName ||
                          `${user.firstName} ${user.lastName}`.trim() ||
                          user.email}
                      </Title>
                      <Text type="secondary">{user.email}</Text>
                    </div>
                  </Space>
                </Card>

                {/* ---- Details ---- */}
                <Card title="Account Details">
                  <Descriptions bordered column={{ xs: 1, sm: 2 }} size="small">
                    <Descriptions.Item label="User UUID">
                      <Text code>{user.userUuid}</Text>
                    </Descriptions.Item>
                    <Descriptions.Item label="Email">{user.email}</Descriptions.Item>
                    <Descriptions.Item label="First Name">
                      {user.firstName || <Text type="secondary">—</Text>}
                    </Descriptions.Item>
                    <Descriptions.Item label="Last Name">
                      {user.lastName || <Text type="secondary">—</Text>}
                    </Descriptions.Item>
                    <Descriptions.Item label="Display Name">
                      {user.displayName || <Text type="secondary">—</Text>}
                    </Descriptions.Item>
                    <Descriptions.Item label="Organisations">
                      <Tag color="blue">{orgCount}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="Registered">
                      {formatDateFull(user.createdAt)}
                    </Descriptions.Item>
                    <Descriptions.Item label="Last Updated">
                      {formatDateFull(user.updatedAt)}
                    </Descriptions.Item>
                  </Descriptions>
                </Card>
              </Space>
            ),
          },
          {
            key: 'settings',
            label: 'Settings',
            children: <SettingsTab />,
          },
        ]}
      />
    </div>
  );
}
