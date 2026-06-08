// ---------------------------------------------------------------------------
// SettingsTab — appearance, language & notifications settings
// ---------------------------------------------------------------------------

import { useState, useCallback, useEffect } from 'react';
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
  ApiOutlined,
} from '@ant-design/icons';

import { useTheme } from '@/hooks/useTheme';
import { useLanguage, type Locale } from '@/hooks/useLanguage';
import { useNotificationPreferences, useUpdateNotificationPreferences } from '@/hooks/useNotifications';
import WebhookSettingsTab from '@/components/settings/WebhookSettingsTab';
import type { NotificationPreferences } from '@/types/notification';

const { Text, Title, Paragraph } = Typography;

// ---------------------------------------------------------------------------
// Notification prefs — localStorage helpers (for migration only)
// ---------------------------------------------------------------------------
const NOTIF_CHANNELS_KEY = 'caas-notif-channels';
const NOTIF_EVENTS_KEY = 'caas-notif-events';

function readLegacyPrefs(): { channels?: Record<string, boolean>; events?: Record<string, boolean> } {
  try {
    const channelsRaw = localStorage.getItem(NOTIF_CHANNELS_KEY);
    const eventsRaw = localStorage.getItem(NOTIF_EVENTS_KEY);
    return {
      channels: channelsRaw ? JSON.parse(channelsRaw) : undefined,
      events: eventsRaw ? JSON.parse(eventsRaw) : undefined,
    };
  } catch {
    return {};
  }
}

function clearLegacyPrefs() {
  try {
    localStorage.removeItem(NOTIF_CHANNELS_KEY);
    localStorage.removeItem(NOTIF_EVENTS_KEY);
  } catch { /* ignore */ }
}

// ---------------------------------------------------------------------------
// Sub-components
// ---------------------------------------------------------------------------

/** Appearance — theme toggle */
function AppearanceTab() {
  const { theme, setTheme } = useTheme();
  const { t } = useLanguage();
  const isDark = theme === 'dark';

  return (
    <Card>
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <div>
          <Title level={5}>{t.settings.theme}</Title>
          <Paragraph type="secondary">{t.settings.themeDescription}</Paragraph>
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
              <Text strong>{t.settings.darkMode}</Text>
              <br />
              <Text type="secondary" style={{ fontSize: 12 }}>
                {isDark ? t.settings.darkActive : t.settings.darkInactive}
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

/** Language — functional locale switcher */
function LanguageTab() {
  const { locale, setLocale, t } = useLanguage();

  return (
    <Card>
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <div>
          <Title level={5}>{t.settings.language}</Title>
          <Paragraph type="secondary">{t.settings.languageDescription}</Paragraph>
        </div>

        <Select
          value={locale}
          onChange={(val) => setLocale(val as Locale)}
          style={{ width: 220 }}
          options={[
            { value: 'en', label: '🇬🇧 English' },
            { value: 'ru', label: '🇷🇺 Русский' },
          ]}
        />

        <Alert
          type="info"
          showIcon
          message={t.common.loading.replace('…', '')}
          description={t.settings.languageComingSoon}
        />
      </Space>
    </Card>
  );
}

/** Notifications — API-driven toggles with localStorage migration */
function NotificationsTab() {
  const { t } = useLanguage();
  const { data: prefs, isLoading } = useNotificationPreferences();
  const updateMutation = useUpdateNotificationPreferences();

  // Migrate localStorage → API on first load
  useEffect(() => {
    if (prefs || isLoading) return;
    const legacy = readLegacyPrefs();
    if (legacy.channels || legacy.events) {
      const merged: NotificationPreferences = {
        channels: {
          EMAIL: legacy.channels?.email ?? true,
          IN_APP: legacy.channels?.inApp ?? true,
          WEBHOOK: legacy.channels?.webhook ?? false,
        },
        eventTypes: {
          JOB_EXECUTION_FAILED: legacy.events?.jobFailed ?? true,
          JOB_EXECUTION_SUCCEEDED: legacy.events?.jobSucceeded ?? true,
          JOB_DISABLED: legacy.events?.jobDisabled ?? true,
          JOB_EXECUTION_TIMEOUT: legacy.events?.jobTimeout ?? true,
          INVITATION_CREATED: true,
          INVITATION_ACCEPTED: true,
          INVITATION_REJECTED: true,
          INVITATION_REVOKED: true,
          INVITATION_EXPIRED: true,
          MEMBER_ADDED: true,
          MEMBER_REMOVED: true,
          MEMBER_ROLE_CHANGED: true,
          SECRET_EXPIRING: true,
          SECRET_ROTATED: true,
          BILLING_TRIAL_ENDING: true,
        },
      };
      updateMutation.mutate(merged);
      clearLegacyPrefs();
    }
  }, [prefs, isLoading]);

  const channels = prefs?.channels ?? {};
  const eventTypes = prefs?.eventTypes ?? {};

  const toggleChannel = (key: string) => {
    if (!prefs) return;
    updateMutation.mutate({
      ...prefs,
      channels: { ...prefs.channels, [key]: !prefs.channels[key] },
    });
  };

  const toggleEvent = (key: string) => {
    if (!prefs) return;
    updateMutation.mutate({
      ...prefs,
      eventTypes: { ...prefs.eventTypes, [key]: !prefs.eventTypes[key] },
    });
  };

  const channelItems = [
    { key: 'EMAIL', label: t.notifications.emailChannel, description: t.notifications.emailDesc },
    { key: 'IN_APP', label: t.notifications.inAppChannel, description: t.notifications.inAppDesc },
    { key: 'WEBHOOK', label: t.notifications.webhookChannel, description: t.notifications.webhookDesc },
  ];

  const eventItems = [
    { key: 'JOB_EXECUTION_FAILED', label: t.notifications.jobFailed, description: t.notifications.jobFailedDesc, tag: 'error' as const },
    { key: 'JOB_EXECUTION_SUCCEEDED', label: t.notifications.jobSucceeded, description: t.notifications.jobSucceededDesc, tag: 'success' as const },
    { key: 'JOB_DISABLED', label: t.notifications.jobDisabled, description: t.notifications.jobDisabledDesc, tag: 'warning' as const },
    { key: 'JOB_EXECUTION_TIMEOUT', label: t.notifications.jobTimeout, description: t.notifications.jobTimeoutDesc, tag: 'error' as const },
    { key: 'INVITATION_CREATED', label: 'Invitation Created', description: 'When you receive an invitation', tag: 'blue' as const },
    { key: 'INVITATION_ACCEPTED', label: 'Invitation Accepted', description: 'When someone accepts your invitation', tag: 'green' as const },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card title={t.notifications.channels} loading={isLoading}>
        <List
          dataSource={channelItems}
          renderItem={(item) => (
            <List.Item
              extra={
                <Switch
                  checked={channels[item.key] ?? false}
                  onChange={() => toggleChannel(item.key)}
                />
              }
            >
              <List.Item.Meta title={item.label} description={item.description} />
            </List.Item>
          )}
        />
      </Card>

      <Card title={t.notifications.eventTypes} loading={isLoading}>
        <List
          dataSource={eventItems}
          renderItem={(item) => (
            <List.Item
              extra={
                <Switch
                  checked={eventTypes[item.key] ?? true}
                  onChange={() => toggleEvent(item.key)}
                />
              }
            >
              <List.Item.Meta
                title={<Space><Tag color={item.tag}>{item.label}</Tag></Space>}
                description={item.description}
              />
            </List.Item>
          )}
        />
      </Card>
    </Space>
  );
}

// ---------------------------------------------------------------------------
// SettingsTab — top-level tabs
// ---------------------------------------------------------------------------
export default function SettingsTab() {
  const { t } = useLanguage();
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
              <span>{t.settings.appearance}</span>
            </Space>
          ),
          children: <AppearanceTab />,
        },
        {
          key: 'language',
          label: (
            <Space>
              <GlobalOutlined />
              <span>{t.settings.language}</span>
            </Space>
          ),
          children: <LanguageTab />,
        },
        {
          key: 'notifications',
          label: (
            <Space>
              <BellOutlined />
              <span>{t.settings.notifications}</span>
            </Space>
          ),
          children: <NotificationsTab />,
        },
        {
          key: 'webhooks',
          label: (
            <Space>
              <ApiOutlined />
              <span>Webhooks</span>
            </Space>
          ),
          children: <WebhookSettingsTab />,
        },
      ]}
    />
  );
}
