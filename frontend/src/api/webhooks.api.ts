import apiClient from './client';
import type {
  WebhookSubscription,
  WebhookDelivery,
  CreateWebhookRequest,
} from '@/types/webhook';

export async function getSubscriptions(): Promise<WebhookSubscription[]> {
  const { data } = await apiClient.get('/webhook-subscriptions');
  return data;
}

export async function createSubscription(
  req: CreateWebhookRequest,
): Promise<WebhookSubscription> {
  const { data } = await apiClient.post('/webhook-subscriptions', req);
  return data;
}

export async function updateSubscription(
  uuid: string,
  req: CreateWebhookRequest,
): Promise<WebhookSubscription> {
  const { data } = await apiClient.put(`/webhook-subscriptions/${uuid}`, req);
  return data;
}

export async function deleteSubscription(uuid: string): Promise<void> {
  await apiClient.delete(`/webhook-subscriptions/${uuid}`);
}

export async function rotateSecret(uuid: string): Promise<WebhookSubscription> {
  const { data } = await apiClient.post(`/webhook-subscriptions/${uuid}/rotate-secret`);
  return data;
}

export async function getDeliveries(
  uuid: string,
  page = 0,
  size = 20,
): Promise<WebhookDelivery[]> {
  const { data } = await apiClient.get(`/webhook-subscriptions/${uuid}/deliveries`, {
    params: { page, size },
  });
  return data;
}
