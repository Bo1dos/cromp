import { useState } from 'react';
import { Typography, Tabs, List, Tag, Button, Space, Empty, notification as antNotification, theme } from 'antd';
import { CheckOutlined, DeleteOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import {
  useNotifications,
  useMarkRead,
  useMarkAllRead,
  useDeleteNotification,
} from '@/hooks/useNotifications';
import { useLanguage } from '@/hooks/useLanguage';
import PageHeader from '@/components/common/PageHeader';
import { useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import apiClient from '@/api/client';
import type { NotificationResponse } from '@/types/notification';

const { Text, Paragraph } = Typography;

const EVENT_TAGS: Record<string, { color: string; label: string }> = {
  INVITATION_CREATED: { color: 'blue', label: 'Invite' },
  INVITATION_ACCEPTED: { color: 'green', label: 'Accepted' },
  INVITATION_REJECTED: { color: 'red', label: 'Rejected' },
  MEMBER_ADDED: { color: 'cyan', label: 'Member' },
  MEMBER_REMOVED: { color: 'orange', label: 'Removed' },
  MEMBER_ROLE_CHANGED: { color: 'purple', label: 'Role' },
  JOB_EXECUTION_FAILED: { color: 'red', label: 'Failed' },
  JOB_EXECUTION_SUCCEEDED: { color: 'green', label: 'Success' },
  JOB_DISABLED: { color: 'orange', label: 'Disabled' },
  JOB_EXECUTION_TIMEOUT: { color: 'red', label: 'Timeout' },
};

export default function NotificationsPage() {
  const { t } = useLanguage();
  const [tab, setTab] = useState<string>('ALL');
  const [page, setPage] = useState(0);
  const status = tab === 'UNREAD' ? 'UNREAD' : undefined;

  const { data, isLoading } = useNotifications(status, page, 20);
  const markRead = useMarkRead();
  const markAllRead = useMarkAllRead();
  const deleteNotif = useDeleteNotification();
  const { token } = theme.useToken();
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const handleMarkRead = (uuid: string) => markRead.mutate(uuid);
  const handleDelete = (uuid: string) => deleteNotif.mutate(uuid);

  const handleAccept = async (notificationUuid: string, invitationUuid: string) => {
    try {
      await apiClient.post(`/invitations/${invitationUuid}/accept`);
      markRead.mutate(notificationUuid);
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
      queryClient.invalidateQueries({ queryKey: ['members'] });
      antNotification.success({ message: 'Invitation accepted! Select your new organization.', duration: 5 });
      setTimeout(() => navigate('/select-organization'), 1500);
    } catch {
      antNotification.error({ message: 'Failed to accept invitation' });
    }
  };

  const handleReject = async (notificationUuid: string, invitationUuid: string) => {
    try {
      await apiClient.post(`/invitations/${invitationUuid}/reject`);
      markRead.mutate(notificationUuid);
      antNotification.success({ message: 'Invitation rejected' });
    } catch {
      antNotification.error({ message: 'Failed to reject invitation' });
    }
  };

  const items = data?.items ?? [];
  const total = data?.totalElements ?? 0;

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '0 16px' }}>
      <PageHeader
        title={t.notifications?.title ?? 'Notifications'}
        extra={
          <Button onClick={() => markAllRead.mutate()} icon={<CheckOutlined />}>
            {t.notifications?.markAllRead ?? 'Mark all read'}
          </Button>
        }
      />

      <Tabs
        activeKey={tab}
        onChange={(k) => { setTab(k); setPage(0); }}
        items={[
          { key: 'ALL', label: t.notifications?.all ?? 'All' },
          { key: 'UNREAD', label: t.notifications?.unread ?? 'Unread' },
        ]}
      />

      <List
        loading={isLoading}
        dataSource={items}
        locale={{ emptyText: <Empty description={t.notifications?.noNotifications ?? 'No notifications'} /> }}
        pagination={{
          current: page + 1,
          total,
          pageSize: 20,
          onChange: (p) => setPage(p - 1),
          showSizeChanger: false,
        }}
        renderItem={(item: NotificationResponse) => {
          const tag = EVENT_TAGS[item.eventType];
          return (
            <List.Item
              style={{
                background: item.status === 'UNREAD' ? token.colorPrimaryBg : undefined,
                padding: '16px',
              }}
              actions={[
                (() => {
                  let meta: Record<string, string> | null = null;
                  try { if (item.metadataJson) meta = JSON.parse(item.metadataJson); } catch { /* */ }
                  const isInvite = item.eventType === 'INVITATION_CREATED'
                    && meta?.invitationUuid
                    && item.status === 'UNREAD'
                    && meta?.own !== 'true';
                  if (isInvite) {
                    return (
                      <Space key="invite-actions">
                        <Button type="primary" size="small" icon={<CheckCircleOutlined />}
                          onClick={() => handleAccept(item.notificationUuid, meta!.invitationUuid)}>Accept</Button>
                        <Button size="small" danger icon={<CloseCircleOutlined />}
                          onClick={() => handleReject(item.notificationUuid, meta!.invitationUuid)}>Reject</Button>
                      </Space>
                    );
                  }
                  return null;
                })(),
                item.status === 'UNREAD' && (
                  <Button key="mark-read" type="text" size="small" icon={<CheckOutlined />}
                    onClick={() => handleMarkRead(item.notificationUuid)} />
                ),
                <Button key="delete" type="text" size="small" danger icon={<DeleteOutlined />}
                  onClick={() => handleDelete(item.notificationUuid)} />,
              ].filter(Boolean)}
            >
              <List.Item.Meta
                title={
                  <Space>
                    {tag && <Tag color={tag.color}>{tag.label}</Tag>}
                    <Text strong={item.status === 'UNREAD'}>{item.title}</Text>
                  </Space>
                }
                description={
                  <Space direction="vertical" size={2}>
                    {item.body && <Paragraph ellipsis={{ rows: 1 }} style={{ margin: 0 }}>{item.body}</Paragraph>}
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {dayjs(item.createdAt).format('MMM D, YYYY HH:mm')}
                    </Text>
                  </Space>
                }
              />
            </List.Item>
          );
        }}
      />
    </div>
  );
}
