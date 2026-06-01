// ---------------------------------------------------------------------------
// formatters — unified date & duration formatting using dayjs
// ---------------------------------------------------------------------------

import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

// Configure plugins once
dayjs.extend(relativeTime);
dayjs.extend(utc);
dayjs.extend(timezone);

// ---------------------------------------------------------------------------
// formatRelative — "2 hours ago", "in 3 days"
// ---------------------------------------------------------------------------
export function formatRelative(date: string | Date | number | undefined | null): string {
  if (!date) return '—';
  return dayjs(date).fromNow();
}

// ---------------------------------------------------------------------------
// formatDate — display-friendly date: "Jun 1, 2026 14:30"
// ---------------------------------------------------------------------------
export function formatDate(date: string | Date | number | undefined | null): string {
  if (!date) return '—';
  return dayjs(date).format('MMM D, YYYY HH:mm');
}

// ---------------------------------------------------------------------------
// formatDateFull — full precision: "2026-06-01 14:30:00"
// ---------------------------------------------------------------------------
export function formatDateFull(date: string | Date | number | undefined | null): string {
  if (!date) return '—';
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
}

// ---------------------------------------------------------------------------
// formatDateWithTz — date with timezone: "June 1, 2026 14:30 UTC+3"
// ---------------------------------------------------------------------------
export function formatDateWithTz(
  date: string | Date | number | undefined | null,
  tz: string = 'UTC',
): string {
  if (!date) return '—';
  return dayjs(date).tz(tz).format('MMMM D, YYYY HH:mm [UTC]Z');
}

// ---------------------------------------------------------------------------
// formatDuration — human-readable duration from milliseconds
// ---------------------------------------------------------------------------
export function formatDuration(ms: number | undefined | null): string {
  if (ms == null) return '—';
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (hours > 0) {
    const remainMin = minutes % 60;
    const remainSec = seconds % 60;
    return `${hours}h ${remainMin}m ${remainSec}s`;
  }
  if (minutes > 0) {
    const remainSec = seconds % 60;
    return `${minutes}m ${remainSec}s`;
  }
  if (seconds > 0) return `${seconds}s`;
  return `${ms}ms`;
}
