import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notification as antNotification } from 'antd';
import {
  getNotifications,
  getUnreadCount,
  markRead,
  markAllRead,
  deleteNotification,
  getPreferences,
  updatePreferences,
} from '@/api/notifications.api';
import { QUERY_KEYS } from '@/utils/constants';
import type { NotificationPreferences } from '@/types/notification';

export function useNotifications(status?: string, page = 0, size = 20) {
  return useQuery({
    queryKey: [QUERY_KEYS.NOTIFICATIONS, status, page],
    queryFn: () => getNotifications(status, page, size),
  });
}

export function useUnreadCount() {
  return useQuery({
    queryKey: [QUERY_KEYS.UNREAD_COUNT],
    queryFn: getUnreadCount,
    refetchInterval: 30_000,
  });
}

export function useMarkRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: markRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.UNREAD_COUNT] });
    },
  });
}

export function useMarkAllRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: markAllRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.UNREAD_COUNT] });
      antNotification.success({ message: 'All notifications marked as read' });
    },
  });
}

export function useDeleteNotification() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: deleteNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.NOTIFICATIONS] });
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.UNREAD_COUNT] });
    },
  });
}

export function useNotificationPreferences() {
  return useQuery({
    queryKey: [QUERY_KEYS.NOTIFICATION_PREFS],
    queryFn: getPreferences,
    retry: false,
  });
}

export function useUpdateNotificationPreferences() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updatePreferences,
    onSuccess: (data) => {
      queryClient.setQueryData([QUERY_KEYS.NOTIFICATION_PREFS], data);
      antNotification.success({ message: 'Preferences saved' });
    },
  });
}
