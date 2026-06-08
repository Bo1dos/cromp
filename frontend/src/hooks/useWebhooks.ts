import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notification } from 'antd';
import {
  getSubscriptions,
  createSubscription,
  updateSubscription,
  deleteSubscription,
  rotateSecret,
  getDeliveries,
} from '@/api/webhooks.api';
import type { CreateWebhookRequest } from '@/types/webhook';

export const WEBHOOKS_KEY = 'webhooks';

export function useWebhookSubscriptions() {
  return useQuery({
    queryKey: [WEBHOOKS_KEY],
    queryFn: getSubscriptions,
  });
}

export function useCreateWebhook() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: CreateWebhookRequest) => createSubscription(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [WEBHOOKS_KEY] });
      notification.success({ message: 'Webhook created' });
    },
  });
}

export function useUpdateWebhook() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ uuid, req }: { uuid: string; req: CreateWebhookRequest }) =>
      updateSubscription(uuid, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [WEBHOOKS_KEY] });
      notification.success({ message: 'Webhook updated' });
    },
  });
}

export function useDeleteWebhook() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (uuid: string) => deleteSubscription(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [WEBHOOKS_KEY] });
      notification.success({ message: 'Webhook deleted' });
    },
  });
}

export function useRotateSecret() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (uuid: string) => rotateSecret(uuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [WEBHOOKS_KEY] });
      notification.success({ message: 'Secret rotated' });
    },
  });
}

export function useWebhookDeliveries(uuid: string) {
  return useQuery({
    queryKey: [WEBHOOKS_KEY, 'deliveries', uuid],
    queryFn: () => getDeliveries(uuid),
    enabled: !!uuid,
  });
}
