// ---------------------------------------------------------------------------
// Analytics API layer — mock-aware: uses mock when VITE_USE_MOCK_ANALYTICS=true
// ---------------------------------------------------------------------------

import apiClient from './client';
import type {
  SummaryResponse,
  PredictionsResponse,
  AnomaliesResponse,
  TimeSeriesPoint,
  DurationBucket,
} from '@/types/analytics';
import {
  generateSummary,
  generatePredictions,
  generateAnomalies,
  generateTimeSeries,
  generateDurationDistribution,
} from '@/utils/mockAnalytics';

// ---------------------------------------------------------------------------
// Feature flag
// ---------------------------------------------------------------------------

const USE_MOCK =
  import.meta.env.VITE_USE_MOCK_ANALYTICS === 'true';

// ---------------------------------------------------------------------------
// Summary
// ---------------------------------------------------------------------------

export interface GetSummaryParams {
  period?: string;
  jobId?: string;
}

export async function getSummary(
  orgId: string,
  params?: GetSummaryParams,
): Promise<SummaryResponse> {
  if (USE_MOCK) {
    return generateSummary(orgId, params?.period ?? '7d');
  }
  const response = await apiClient.get<SummaryResponse>(
    `/organizations/${orgId}/analytics/executions/summary`,
    { params },
  );
  return response.data;
}

// ---------------------------------------------------------------------------
// Predictions
// ---------------------------------------------------------------------------

export interface GetPredictionsParams {
  jobId?: string;
}

export async function getPredictions(
  orgId: string,
  params?: GetPredictionsParams,
): Promise<PredictionsResponse> {
  if (USE_MOCK) {
    return generatePredictions(orgId);
  }
  const response = await apiClient.get<PredictionsResponse>(
    `/organizations/${orgId}/analytics/predictions`,
    { params },
  );
  return response.data;
}

// ---------------------------------------------------------------------------
// Anomalies
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
  if (USE_MOCK) {
    return generateAnomalies(orgId, params?.from, params?.to);
  }
  const response = await apiClient.get<AnomaliesResponse>(
    `/organizations/${orgId}/analytics/anomalies`,
    { params },
  );
  return response.data;
}

// ---------------------------------------------------------------------------
// Time series (chart data) — always mock for now, replace with real endpoint later
// ---------------------------------------------------------------------------

export async function getTimeSeries(
  period: string,
): Promise<TimeSeriesPoint[]> {
  if (USE_MOCK) {
    return generateTimeSeries(period);
  }
  // TODO: real endpoint when available
  const response = await apiClient.get<TimeSeriesPoint[]>(
    '/analytics/executions/timeseries',
    { params: { period } },
  );
  return response.data;
}

// ---------------------------------------------------------------------------
// Duration distribution (chart data)
// ---------------------------------------------------------------------------

export async function getDurationDistribution(): Promise<DurationBucket[]> {
  if (USE_MOCK) {
    return generateDurationDistribution();
  }
  // TODO: real endpoint when available
  const response = await apiClient.get<DurationBucket[]>(
    '/analytics/executions/duration-distribution',
  );
  return response.data;
}
