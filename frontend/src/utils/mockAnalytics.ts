// ---------------------------------------------------------------------------
// Mock analytics data generator — produces plausible data for MVP dashboard
// ---------------------------------------------------------------------------

import type {
  SummaryResponse,
  PredictionsResponse,
  PredictionItem,
  AnomaliesResponse,
  AnomalyItem,
  AnomalySeverity,
  TimeSeriesPoint,
  DurationBucket,
} from '@/types/analytics';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const JOB_NAMES = [
  'user-sync-job',
  'report-generator',
  'cache-warmer',
  'db-backup',
  'email-dispatch',
  'data-cleanup',
  'invoice-processor',
  'log-archiver',
];

const ANOMALY_DESCRIPTIONS = [
  'Execution duration exceeded 3× p95 baseline',
  'Unexpected HTTP 500 from downstream service',
  'Scheduled job missed 3 consecutive windows',
  'Memory usage spiked to 92 % during execution',
  'Response payload size increased 5× over baseline',
  'Job started with 45 min delay due to queue backpressure',
  'Retry count exceeded threshold (4 attempts)',
  'Downstream API latency jumped to 8 s (normal: 200 ms)',
];

const SEVERITIES: AnomalySeverity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

function randomInt(min: number, max: number): number {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function randomFloat(min: number, max: number, decimals = 2): number {
  return parseFloat((Math.random() * (max - min) + min).toFixed(decimals));
}

function pick<T>(arr: T[]): T {
  return arr[randomInt(0, arr.length - 1)];
}

function uuid(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });
}

function daysAgo(days: number): string {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d.toISOString();
}

function addDays(iso: string, n: number): string {
  const d = new Date(iso);
  d.setDate(d.getDate() + n);
  return d.toISOString();
}

function formatDate(date: Date): string {
  const dd = String(date.getDate()).padStart(2, '0');
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  return `${dd}.${mm}`;
}

// Simulate network latency
function delay(): Promise<void> {
  const ms = randomInt(300, 800);
  return new Promise((r) => setTimeout(r, ms));
}

// ---------------------------------------------------------------------------
// Public generators
// ---------------------------------------------------------------------------

export async function generateSummary(
  _orgId: string,
  period: string,
): Promise<SummaryResponse> {
  await delay();

  const days = period === '30d' ? 30 : period === '90d' ? 90 : 7;
  const total = randomInt(100, 1000);
  const successRate = randomFloat(0.7, 0.95);
  const successful = Math.round(total * successRate);
  const failedRate = randomFloat(0.02, 0.15);
  const failed = Math.round(total * failedRate);
  const cancelled = total - successful - failed;

  return {
    totalExecutions: total,
    successfulExecutions: successful,
    failedExecutions: failed,
    cancelledExecutions: cancelled,
    avgDurationMs: randomInt(250, 8500),
    period,
    fromDate: daysAgo(days),
    toDate: new Date().toISOString(),
  };
}

export async function generatePredictions(
  _orgId: string,
): Promise<PredictionsResponse> {
  await delay();

  const count = randomInt(3, 5);
  const predictions: PredictionItem[] = Array.from({ length: count }, () => {
    const prob = randomFloat(0.05, 0.85);
    return {
      jobUuid: uuid(),
      jobName: JOB_NAMES[randomInt(0, JOB_NAMES.length - 1)],
      failureProbability: prob,
      expectedDurationMs: randomInt(150, 12000),
      confidence: randomFloat(0.5, 0.99),
      nextExpectedRunAt: addDays(new Date().toISOString(), randomInt(1, 14)),
    };
  });

  return { orgId: _orgId, predictions };
}

export async function generateAnomalies(
  _orgId: string,
  from?: string,
  to?: string,
): Promise<AnomaliesResponse> {
  await delay();

  const count = randomInt(2, 5);
  const fromDate = from ?? daysAgo(30);
  const toDate = to ?? new Date().toISOString();

  const anomalies: AnomalyItem[] = Array.from({ length: count }, () => ({
    uuid: uuid(),
    jobUuid: uuid(),
    jobName: pick(JOB_NAMES),
    description: pick(ANOMALY_DESCRIPTIONS),
    severity: pick(SEVERITIES),
    detectedAt: new Date(
      Date.now() - randomInt(0, 30) * 86400000,
    ).toISOString(),
    relatedExecutionUuid: Math.random() > 0.3 ? uuid() : undefined,
  }));

  return { orgId: _orgId, fromDate, toDate, anomalies };
}

export async function generateTimeSeries(
  period: string,
): Promise<TimeSeriesPoint[]> {
  await delay();

  const days = period === '30d' ? 30 : period === '90d' ? 90 : 7;
  const points: TimeSeriesPoint[] = [];

  for (let i = days - 1; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    points.push({
      date: formatDate(d),
      success: randomInt(15, 80),
      failure: randomInt(0, 12),
    });
  }

  return points;
}

export async function generateDurationDistribution(): Promise<
  DurationBucket[]
> {
  await delay();

  const buckets: DurationBucket[] = [
    { bucket: '0-500ms', count: randomInt(40, 200) },
    { bucket: '500ms-1s', count: randomInt(30, 150) },
    { bucket: '1s-2s', count: randomInt(20, 100) },
    { bucket: '2s-5s', count: randomInt(10, 60) },
    { bucket: '5s-10s', count: randomInt(5, 30) },
    { bucket: '10s+', count: randomInt(0, 15) },
  ];

  return buckets;
}
