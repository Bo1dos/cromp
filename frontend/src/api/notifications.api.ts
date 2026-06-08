import apiClient from './client';
import type {
  NotificationPreferences,
  PagedNotificationResponse,
  UnreadCountResponse,
} from '@/types/notification';

export async function getNotifications(
  status?: string,
  page = 0,
  size = 20,
): Promise<PagedNotificationResponse> {
  const params: Record<string, string | number> = { page, size };
  if (status) params.status = status;
  const { data } = await apiClient.get('/notifications', { params });
  return data;
}

export async function getUnreadCount(): Promise<UnreadCountResponse> {
  const { data } = await apiClient.get('/notifications/unread-count');
  return data;
}

export async function markRead(uuid: string): Promise<void> {
  await apiClient.post(`/notifications/${uuid}/mark-read`);
}

export async function markAllRead(): Promise<void> {
  await apiClient.post('/notifications/mark-all-read');
}

export async function deleteNotification(uuid: string): Promise<void> {
  await apiClient.delete(`/notifications/${uuid}`);
}

export async function getPreferences(): Promise<NotificationPreferences> {
  const { data } = await apiClient.get('/notification-preferences');
  return data;
}

export async function updatePreferences(
  prefs: NotificationPreferences,
): Promise<NotificationPreferences> {
  const { data } = await apiClient.put('/notification-preferences', prefs);
  return data;
}
