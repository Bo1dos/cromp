"""Validate synthetic data quality for analytics ML training."""
import pandas as pd
import numpy as np

df = pd.read_csv('test_data.csv')
df['error_rate'] = df['failed'] / df['total_executions']

print('=== OVERALL STATS ===')
print(f'Total rows: {len(df)}')
print(f'Unique job_ids: {df["job_id"].nunique()}')
print(f'Unique org_ids: {df["organization_id"].nunique()}')
print(f'Date range: {df["day"].min()} .. {df["day"].max()}')

print()
print('=== ERROR RATE BY JOB_ID ===')
err_stats = df.groupby('job_id')['error_rate'].describe()
print(err_stats.to_string())

print()
print('=== DURATION (avg_attempt_duration_ms) ===')
dur = df['avg_attempt_duration_ms']
print(f'  mean={dur.mean():.0f}  median={dur.median():.0f}  min={dur.min():.0f}  max={dur.max():.0f}')

print()
print('=== PROFILE COVERAGE (by mean error_rate) ===')
means = df.groupby('job_id')['error_rate'].mean().sort_values()
for jid, val in means.items():
    if val < 0.05:
        tag = 'stable'
    elif val > 0.6:
        tag = 'failing'
    elif 0.2 < val < 0.5:
        tag = 'recovering (split period)'
    else:
        tag = 'mixed (degrading/spiky)'
    print(f'  job_id={int(jid):3d}  mean_err={val:.4f}  -> {tag}')

print()
print('=== DURATION BY JOB_ID (avg, p95, max) ===')
dur_stats = df.groupby('job_id')[['avg_attempt_duration_ms', 'p95_duration_ms', 'max_duration_ms']].mean()
print(dur_stats.to_string())

print()
print('=== NULL CHECKS ===')
for col in ['job_id', 'organization_id', 'day', 'total_executions', 'succeeded', 'failed', 
            'avg_attempt_duration_ms', 'p95_duration_ms', 'max_duration_ms', 'total_attempts']:
    nulls = df[col].isna().sum()
    print(f'  {col}: {nulls} nulls')

print()
print('=== INVARIANTS ===')
# succeeded + failed <= total
violations = (df['succeeded'] + df['failed'] > df['total_executions']).sum()
print(f'  succeeded+failed > total: {violations} violations (should be 0)')
# p95 <= max
violations = (df['p95_duration_ms'] > df['max_duration_ms']).sum()
print(f'  p95 > max: {violations} violations (should be 0)')
# duration >= 1
violations = (df['avg_attempt_duration_ms'] < 1.0).sum()
print(f'  avg_duration < 1ms: {violations} violations (should be 0)')
