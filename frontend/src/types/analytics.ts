// ---------------------------------------------------------------------------
// Analytics types — mirrors backend AnalyticsDtos (real API only, no mocks)
// ---------------------------------------------------------------------------

/** Backend SummaryResponse */
export interface SummaryResponse {
  organizationId: number;
  jobId: number | null;
  period: string;
  total: number;
  succeeded: number;
  failed: number;
  errorRate: number;
  avgDurationMs: number | null;
  p95DurationMs: number | null;
  calculatedAt: string | null;
}

/** Backend PredictionItem */
export interface PredictionItem {
  jobId: number;
  nextRunAt: string | null;
  failureProbability: number;
  expectedDurationMs: number | null;
  confidence: number;
}

/** Backend PredictionsResponse */
export interface PredictionsResponse {
  organizationId: number;
  jobId: number | null;
  predictions: PredictionItem[];
}

export type AnomalySeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

/** Backend AnomalyItem */
export interface AnomalyItem {
  jobId: number;
  jobName: string;
  metric: string;
  value: number;
  expectedMin: number;
  expectedMax: number;
  severity: AnomalySeverity;
  detectedAt: string;
}

/** Backend AnomaliesResponse */
export interface AnomaliesResponse {
  organizationId: number;
  jobId: number | null;
  fromDate: string;
  toDate: string;
  anomalies: AnomalyItem[];
}
