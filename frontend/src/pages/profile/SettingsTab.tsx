// ---------------------------------------------------------------------------
// SettingsTab — appearance, language & notifications settings
// ---------------------------------------------------------------------------

import { useState } from 'react';
import {
  Tabs,
  Card,
  Switch,
  Select,
  Typography,
  Space,
  List,
  Tag,
  Alert,
} from 'antd';
import {
  BgColorsOutlined,
  GlobalOutlined,
  BellOutlined,
} from '@ant-design/icons';

import { useTheme } from '@/hooks/useTheme';

const { Text, Title, Paragraph } = Typography;

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

/** Appearance — theme toggle */
function AppearanceTab() {
  const { theme, setTheme } = useTheme();
  const isDark = theme === 'dark';

  return (
    <Card>
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <div>
          <Title level={5}>Theme</Title>
          <Paragraph type="secondary">
            Choose between light and dark appearance for the application.
          </Paragraph>
        </div>

        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 16px',
            background: 'var(--color-bg-layout, #f5f5f5)',
            borderRadius: 8,
          }}
        >
          <Space>
            <BgColorsOutlined />
            <div>
              <Text strong>Dark Mode</Text>
              <br />
              <Text type="secondary" style={{ fontSize: 12 }}>
                {isDark ? 'Dark theme is active' : 'Switch to dark theme'}
              </Text>
            </div>
          </Space>
          <Switch
            checked={isDark}
            onChange={(checked) => setTheme(checked ? 'dark' : 'light')}
            checkedChildren="🌙"
            unCheckedChildren="☀️"
          />
        </div>
      </Space>
    </Card>
  );
}

/** Language — placeholder for future i18n */
function LanguageTab() {
  return (
    <Card>
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <div>
          <Title level={5}>Language</Title>
          <Paragraph type="secondary">
            Select your preferred language for the application interface.
          </Paragraph>
        </div>

        <Select
          defaultValue="en"
          style={{ width: 200 }}
          disabled
          options={[
            { value: 'en', label: '🇬🇧 English' },
            { value: 'ru', label: '🇷🇺 Русский' },
          ]}
        />

        <Alert
          type="info"
          showIcon
          message="Coming soon"
          description="Multi-language support (i18n) will be implemented in a future update."
        />
      </Space>
    </Card>
  );
}

/** Notifications — placeholder for future notification module */
function NotificationsTab() {
  const channels = [
    { key: 'email', label: 'Email', description: 'Receive notifications via email', enabled: true },
    { key: 'in-app', label: 'In-App', description: 'Show notifications inside the application', enabled: true },
    { key: 'webhook', label: 'Webhook', description: 'Send notifications to a custom URL', enabled: false },
  ];

  const eventTypes = [
    { key: 'job-failed', label: 'Job Failed', description: 'When a job execution fails', tag: 'error' as const },
    { key: 'job-succeeded', label: 'Job Succeeded', description: 'When a job execution succeeds', tag: 'success' as const },
    { key: 'job-disabled', label: 'Job Disabled', description: 'When a job is manually disabled', tag: 'warning' as const },
    { key: 'job-timeout', label: 'Job Timeout', description: 'When a job exceeds its timeout', tag: 'error' as const },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {/* ---- Channels ---- */}
      <Card title="Notification Channels">
        <List
          dataSource={channels}
          renderItem={(item) => (
            <List.Item
              extra={
                <Switch
                  defaultChecked={item.enabled}
                  disabled
                />
              }
            >
              <List.Item.Meta
                title={item.label}
                description={item.description}
              />
            </List.Item>
          )}
        />
      </Card>

      {/* ---- Event types ---- */}
      <Card title="Event Types">
        <List
          dataSource={eventTypes}
          renderItem={(item) => (
            <List.Item
              extra={
                <Switch
                  defaultChecked
                  disabled
                />
              }
            >
              <List.Item.Meta
                title={
                  <Space>
                    <Tag color={item.tag}>{item.label}</Tag>
                  </Space>
                }
                description={item.description}
              />
            </List.Item>
          )}
        />
      </Card>

      <Alert
        type="info"
        showIcon
        message="Coming soon"
        description="The notification module is under development. Preferences saved here will be applied once it launches."
      />
    </Space>
  );
}

// ---------------------------------------------------------------------------
// SettingsTab — top-level tabs
// ---------------------------------------------------------------------------
export default function SettingsTab() {
  const [tab, setTab] = useState('appearance');

  return (
    <Tabs
      activeKey={tab}
      onChange={setTab}
      tabPosition="left"
      style={{ minHeight: 360 }}
      items={[
        {
          key: 'appearance',
          label: (
            <Space>
              <BgColorsOutlined />
              <span>Appearance</span>
            </Space>
          ),
          children: <AppearanceTab />,
        },
        {
          key: 'language',
          label: (
            <Space>
              <GlobalOutlined />
              <span>Language</span>
            </Space>
          ),
          children: <LanguageTab />,
        },
        {
          key: 'notifications',
          label: (
            <Space>
              <BellOutlined />
              <span>Notifications</span>
            </Space>
          ),
          children: <NotificationsTab />,
        },
      ]}
    />
  );
}
