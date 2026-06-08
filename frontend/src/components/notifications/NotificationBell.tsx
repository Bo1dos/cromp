import { Badge, Popover, List, Typography, Button, Space, theme } from 'antd';
import { BellOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { useUnreadCount, useNotifications, useMarkRead } from '@/hooks/useNotifications';
import { useLanguage } from '@/hooks/useLanguage';
import type { NotificationResponse } from '@/types/notification';

dayjs.extend(relativeTime);

const { Text } = Typography;

export default function NotificationBell() {
  const { data: unreadData } = useUnreadCount();
  const { data: notifData } = useNotifications(undefined, 0, 5);
  const markReadMutation = useMarkRead();
  const navigate = useNavigate();
  const { t } = useLanguage();
  const { token } = theme.useToken();

  const unreadCount = unreadData?.count ?? 0;
  const items = notifData?.items ?? [];

  const handleItemClick = (item: NotificationResponse) => {
    if (item.status === 'UNREAD') {
      markReadMutation.mutate(item.notificationUuid);
    }
    navigate('/dashboard/notifications');
  };

  const handleViewAll = () => {
    navigate('/dashboard/notifications');
  };

  const dropdownContent = (
    <div style={{ width: 360, maxHeight: 400, overflow: 'auto' }}>
      {items.length === 0 ? (
        <div style={{ padding: 24, textAlign: 'center' }}>
          <Text type="secondary">{t.notifications?.empty ?? 'No notifications'}</Text>
        </div>
      ) : (
        <>
          <List
            dataSource={items}
            renderItem={(item) => (
              <List.Item
                onClick={() => handleItemClick(item)}
                style={{
                  cursor: 'pointer',
                  padding: '12px 16px',
                  background: item.status === 'UNREAD' ? token.colorPrimaryBg : undefined,
                }}
              >
                <List.Item.Meta
                  title={
                    <Text strong={item.status === 'UNREAD'} style={{ fontSize: 13 }}>
                      {item.title}
                    </Text>
                  }
                  description={
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {dayjs(item.createdAt).fromNow()}
                    </Text>
                  }
                />
              </List.Item>
            )}
          />
          <div style={{ textAlign: 'center', padding: 8, borderTop: `1px solid ${token.colorBorderSecondary}` }}>
            <Button type="link" size="small" onClick={handleViewAll}>
              {t.notifications?.viewAll ?? 'View all'}
            </Button>
          </div>
        </>
      )}
    </div>
  );

  return (
    <Popover content={dropdownContent} trigger="click" placement="bottomRight">
      <Space style={{ cursor: 'pointer', padding: '0 8px' }}>
        <Badge count={unreadCount} size="small" offset={[-2, 2]}>
          <BellOutlined style={{ fontSize: 18 }} />
        </Badge>
      </Space>
    </Popover>
  );
}
