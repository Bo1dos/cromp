// ---------------------------------------------------------------------------
// Analytics API — real backend endpoints (no mocks)
// ---------------------------------------------------------------------------

import apiClient from './client';
import type {
  SummaryResponse,
  PredictionsResponse,
  AnomaliesResponse,
} from '@/types/analytics';

// ---------------------------------------------------------------------------
// Summary — GET /api/v1/organizations/{orgId}/analytics/executions/summary
// ---------------------------------------------------------------------------

export interface GetSummaryParams {
  period?: string;
  jobId?: string;
}

export async function getSummary(
  orgId: string,
  params?: GetSummaryParams,
): Promise<SummaryResponse> {
  const response = await apiClient.get<SummaryResponse>(
    `/organizations/${orgId}/analytics/executions/summary`,
    { params },
  );
  return response.data;
}

// ---------------------------------------------------------------------------
// Predictions — GET /api/v1/organizations/{orgId}/analytics/predictions
// ---------------------------------------------------------------------------

export interface GetPredictionsParams {
  jobId?: string;
}

export async function getPredictions(
  orgId: string,
  params?: GetPredictionsParams,
): Promise<PredictionsResponse> {
  const response = await apiClient.get<PredictionsResponse>(
    `/organizations/${orgId}/analytics/predictions`,
    { params },
  );
  return response.data;
}

// ---------------------------------------------------------------------------
// Anomalies — GET /api/v1/organizations/{orgId}/analytics/anomalies
// ---------------------------------------------------------------------------

export interface GetAnomaliesParams {
  jobId?: string;
  from?: string;
  to?: string;
}

export async function getAnomalies(
  orgId: string,
  params?: GetAnomaliesParams,
): Promise<AnomaliesResponse> {
  const response = await apiClient.get<AnomaliesResponse>(
    `/organizations/${orgId}/analytics/anomalies`,
    { params },
  );
  return response.data;
}
