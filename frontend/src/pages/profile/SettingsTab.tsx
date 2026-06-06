// ---------------------------------------------------------------------------
// SettingsTab — appearance, language & notifications settings
// ---------------------------------------------------------------------------

import { useState, useCallback } from 'react';
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
import { useLanguage, type Locale } from '@/hooks/useLanguage';

const { Text, Title, Paragraph } = Typography;

// ---------------------------------------------------------------------------
// Notification prefs — localStorage helpers
// ---------------------------------------------------------------------------
const NOTIF_CHANNELS_KEY = 'caas-notif-channels';
const NOTIF_EVENTS_KEY = 'caas-notif-events';

interface ChannelPrefs {
  email: boolean;
  inApp: boolean;
  webhook: boolean;
}

interface EventPrefs {
  jobFailed: boolean;
  jobSucceeded: boolean;
  jobDisabled: boolean;
  jobTimeout: boolean;
}

const defaultChannels: ChannelPrefs = { email: true, inApp: true, webhook: false };
const defaultEvents: EventPrefs = { jobFailed: true, jobSucceeded: true, jobDisabled: true, jobTimeout: true };

function readPrefs<T>(key: string, defaults: T): T {
  try {
    const raw = localStorage.getItem(key);
    if (raw) return { ...defaults, ...JSON.parse(raw) };
  } catch { /* ignore */ }
  return defaults;
}

function writePrefs<T>(key: string, val: T) {
  try { localStorage.setItem(key, JSON.stringify(val)); } catch { /* ignore */ }
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

/** Notifications — functional toggles with localStorage persistence */
function NotificationsTab() {
  const { t } = useLanguage();

  const [channels, setChannels] = useState<ChannelPrefs>(() =>
    readPrefs(NOTIF_CHANNELS_KEY, defaultChannels),
  );
  const [events, setEvents] = useState<EventPrefs>(() =>
    readPrefs(NOTIF_EVENTS_KEY, defaultEvents),
  );

  const toggleChannel = useCallback(
    (key: keyof ChannelPrefs) => {
      setChannels((prev) => {
        const next = { ...prev, [key]: !prev[key] };
        writePrefs(NOTIF_CHANNELS_KEY, next);
        return next;
      });
    },
    [],
  );

  const toggleEvent = useCallback(
    (key: keyof EventPrefs) => {
      setEvents((prev) => {
        const next = { ...prev, [key]: !prev[key] };
        writePrefs(NOTIF_EVENTS_KEY, next);
        return next;
      });
    },
    [],
  );

  const channelItems = [
    { key: 'email' as const, label: t.notifications.emailChannel, description: t.notifications.emailDesc },
    { key: 'inApp' as const, label: t.notifications.inAppChannel, description: t.notifications.inAppDesc },
    { key: 'webhook' as const, label: t.notifications.webhookChannel, description: t.notifications.webhookDesc },
  ];

  const eventItems = [
    { key: 'jobFailed' as const, label: t.notifications.jobFailed, description: t.notifications.jobFailedDesc, tag: 'error' as const },
    { key: 'jobSucceeded' as const, label: t.notifications.jobSucceeded, description: t.notifications.jobSucceededDesc, tag: 'success' as const },
    { key: 'jobDisabled' as const, label: t.notifications.jobDisabled, description: t.notifications.jobDisabledDesc, tag: 'warning' as const },
    { key: 'jobTimeout' as const, label: t.notifications.jobTimeout, description: t.notifications.jobTimeoutDesc, tag: 'error' as const },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {/* ---- Channels ---- */}
      <Card title={t.notifications.channels}>
        <List
          dataSource={channelItems}
          renderItem={(item) => (
            <List.Item
              extra={
                <Switch
                  checked={channels[item.key]}
                  onChange={() => toggleChannel(item.key)}
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
      <Card title={t.notifications.eventTypes}>
        <List
          dataSource={eventItems}
          renderItem={(item) => (
            <List.Item
              extra={
                <Switch
                  checked={events[item.key]}
                  onChange={() => toggleEvent(item.key)}
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
        message={t.common.loading.replace('…', '')}
        description={t.settings.notificationsComingSoon}
      />
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
      ]}
    />
  );
}
