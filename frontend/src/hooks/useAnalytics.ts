// ---------------------------------------------------------------------------
// useAnalytics — TanStack Query hooks for the Analytics domain
// ---------------------------------------------------------------------------

import { useQuery } from '@tanstack/react-query';
import * as analyticsApi from '@/api/analytics.api';
import type {
  SummaryResponse,
  PredictionsResponse,
  AnomaliesResponse,
  TimeSeriesPoint,
  DurationBucket,
} from '@/types/analytics';
import { QUERY_KEYS } from '@/utils/constants';

// ---------------------------------------------------------------------------
// Summary
// ---------------------------------------------------------------------------

export function useSummary(orgId: string, period: string) {
  return useQuery<SummaryResponse, Error>({
    queryKey: [QUERY_KEYS.ANALYTICS_SUMMARY, orgId, period],
    queryFn: () => analyticsApi.getSummary(orgId, { period }),
    staleTime: 5 * 60 * 1000, // 5 min
    enabled: Boolean(orgId),
  });
}

// ---------------------------------------------------------------------------
// Predictions
// ---------------------------------------------------------------------------

export function usePredictions(orgId: string) {
  return useQuery<PredictionsResponse, Error>({
    queryKey: [QUERY_KEYS.ANALYTICS_PREDICTIONS, orgId],
    queryFn: () => analyticsApi.getPredictions(orgId),
    staleTime: 10 * 60 * 1000, // 10 min
    enabled: Boolean(orgId),
  });
}

// ---------------------------------------------------------------------------
// Anomalies
// ---------------------------------------------------------------------------

export function useAnomalies(orgId: string, from: string, to: string) {
  return useQuery<AnomaliesResponse, Error>({
    queryKey: [QUERY_KEYS.ANALYTICS_ANOMALIES, orgId, from, to],
    queryFn: () => analyticsApi.getAnomalies(orgId, { from, to }),
    staleTime: 5 * 60 * 1000, // 5 min
    enabled: Boolean(orgId),
  });
}

// ---------------------------------------------------------------------------
// Time series chart data
// ---------------------------------------------------------------------------

export function useTimeSeries(orgId: string, period: string) {
  return useQuery<TimeSeriesPoint[], Error>({
    queryKey: [QUERY_KEYS.ANALYTICS_TIMESERIES, orgId, period],
    queryFn: () => analyticsApi.getTimeSeries(period),
    staleTime: 5 * 60 * 1000,
    enabled: Boolean(orgId),
  });
}

// ---------------------------------------------------------------------------
// Duration distribution chart data
// ---------------------------------------------------------------------------

export function useDurationDistribution(orgId: string) {
  return useQuery<DurationBucket[], Error>({
    queryKey: [QUERY_KEYS.ANALYTICS_DURATION_DIST, orgId],
    queryFn: () => analyticsApi.getDurationDistribution(),
    staleTime: 10 * 60 * 1000,
    enabled: Boolean(orgId),
  });
}
