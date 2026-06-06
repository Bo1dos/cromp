// ---------------------------------------------------------------------------
// useAnalytics — TanStack Query hooks for the Analytics domain
// ---------------------------------------------------------------------------

import { useQuery } from '@tanstack/react-query';
import * as analyticsApi from '@/api/analytics.api';
import type {
  SummaryResponse,
  PredictionsResponse,
  AnomaliesResponse,
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
    retry: false,
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
    retry: false,
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
    retry: false,
  });
}
