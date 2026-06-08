export interface NotificationResponse {
  notificationUuid: string;
  eventType: string;
  channel: 'IN_APP' | 'EMAIL';
  title: string;
  body: string;
  metadataJson: string | null;
  status: 'UNREAD' | 'READ' | 'ARCHIVED';
  readAt: string | null;
  createdAt: string;
}

export interface NotificationPreferences {
  channels: Record<string, boolean>;
  eventTypes: Record<string, boolean>;
}

export interface UnreadCountResponse {
  count: number;
}

export interface PagedNotificationResponse {
  items: NotificationResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
