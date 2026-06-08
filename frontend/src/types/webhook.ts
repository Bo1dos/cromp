export interface WebhookSubscription {
  subscriptionUuid: string;
  url: string;
  enabled: boolean;
  eventTypes: string[];
  secret?: string;
  createdAt: string;
}

export interface WebhookDelivery {
  responseCode: number;
  status: 'PENDING' | 'SUCCEEDED' | 'FAILED';
  attemptNumber: number;
  errorMessage: string | null;
  attemptedAt: string;
}

export interface CreateWebhookRequest {
  url: string;
  eventTypes: string[];
}
