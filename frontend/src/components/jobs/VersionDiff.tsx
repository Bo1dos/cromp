// ---------------------------------------------------------------------------
// VersionDiff — renders JSON diff with syntax highlighting for two job configs
// ---------------------------------------------------------------------------

import { useMemo } from 'react';
import ReactDiffViewer from 'react-diff-viewer';
import { theme } from 'antd';
import { useTheme } from '@/hooks/useTheme';

interface VersionDiffProps {
  oldConfig: Record<string, unknown>;
  newConfig: Record<string, unknown>;
  oldVersion?: number;
  newVersion?: number;
  splitView?: boolean;
  showDiffOnly?: boolean;
}

// ---------------------------------------------------------------------------
// JSON syntax highlighting — returns HTML-safe spans for a single line
// ---------------------------------------------------------------------------
const TOKEN_RE =
  /("(?:\\.|[^"\\])*")\s*:|("(?:\\.|[^"\\])*")|(-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?)\b|\b(true|false|null)\b|([{}[\]])/g;

function highlightLine(line: string): JSX.Element {
  const parts: React.ReactNode[] = [];
  let last = 0;
  let match: RegExpExecArray | null;

  while ((match = TOKEN_RE.exec(line)) !== null) {
    if (match.index > last) {
      parts.push(line.slice(last, match.index));
    }
    const [, key, str, num, literal, bracket] = match;
    if (key) {
      parts.push(<span key={match.index} style={{ color: '#0550ae' }}>{key}</span>);
      parts.push(':');
    } else if (str) {
      parts.push(<span key={match.index} style={{ color: '#0a3069' }}>{str}</span>);
    } else if (num) {
      parts.push(<span key={match.index} style={{ color: '#0550ae' }}>{num}</span>);
    } else if (literal) {
      parts.push(<span key={match.index} style={{ color: '#cf222e' }}>{literal}</span>);
    } else if (bracket) {
      parts.push(<span key={match.index} style={{ color: '#656d76' }}>{bracket}</span>);
    }
    last = match.index + match[0].length;
  }
  if (last < line.length) {
    parts.push(line.slice(last));
  }
  return <>{parts.length > 0 ? parts : line}</>;
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------
export default function VersionDiff({
  oldConfig,
  newConfig,
  oldVersion,
  newVersion,
  splitView = true,
  showDiffOnly = false,
}: VersionDiffProps) {
  const { token } = theme.useToken();
  const { theme: appTheme } = useTheme();
  const isDark = appTheme === 'dark';

  // Plain JSON — no HTML
  const oldText = useMemo(
    () => JSON.stringify(oldConfig, null, 2),
    [oldConfig],
  );
  const newText = useMemo(
    () => JSON.stringify(newConfig, null, 2),
    [newConfig],
  );

  // Custom line renderer with syntax highlighting
  const renderContent = useMemo(
    () => (source: string) => highlightLine(source),
    [],
  );

  const diffStyles = {
    diffContainer: {
      maxHeight: 500,
      overflow: 'auto',
      fontSize: 12,
      fontFamily: "'SF Mono', 'Fira Code', 'Fira Mono', Menlo, Consolas, monospace",
      background: isDark ? '#1e1e1e' : '#ffffff',
      border: `1px solid ${token.colorBorderSecondary}`,
      borderRadius: 6,
    },
    titleBlock: {
      fontSize: 12,
      fontWeight: 600,
      background: isDark ? '#2d2d2d' : '#f6f8fa',
      color: isDark ? '#e1e4e8' : '#24292e',
      borderBottom: `1px solid ${token.colorBorderSecondary}`,
    },
    contentText: {
      fontSize: 12,
      lineHeight: '20px',
      color: isDark ? '#e1e4e8' : '#24292e',
    },
    line: {
      padding: '0 8px',
    },
    diffAdded: {
      background: isDark ? 'rgba(70, 149, 74, 0.15)' : '#e6ffec',
    },
    diffRemoved: {
      background: isDark ? 'rgba(229, 83, 75, 0.15)' : '#ffebe9',
    },
    gutter: {
      minWidth: 40,
      padding: '0 8px',
      background: isDark ? '#252526' : '#f6f8fa',
      color: isDark ? '#8b949e' : '#656d76',
      borderRight: `1px solid ${token.colorBorderSecondary}`,
    },
    marker: {
      padding: '0 4px',
    },
  };

  return (
    <ReactDiffViewer
      oldValue={oldText}
      newValue={newText}
      splitView={splitView}
      showDiffOnly={showDiffOnly}
      useDarkTheme={isDark}
      disableWordDiff={false}
      renderContent={renderContent}
      styles={diffStyles}
      leftTitle={`v${oldVersion ?? '?'}`}
      rightTitle={`v${newVersion ?? '?'}`}
    />
  );
}

