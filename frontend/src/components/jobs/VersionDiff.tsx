// ---------------------------------------------------------------------------
// VersionDiff — renders JSON diff for two job configurations
// ---------------------------------------------------------------------------

import { useMemo } from 'react';
import ReactDiffViewer from 'react-diff-viewer';
import { theme } from 'antd';

interface VersionDiffProps {
  oldConfig: Record<string, unknown>;
  newConfig: Record<string, unknown>;
  splitView?: boolean;
  showDiffOnly?: boolean;
}

export default function VersionDiff({
  oldConfig,
  newConfig,
  splitView = true,
  showDiffOnly = false,
}: VersionDiffProps) {
  const { token } = theme.useToken();
  const isDark = token.colorBgLayout === '#141414' || token.colorBgLayout?.startsWith('#1');

  const oldText = useMemo(
    () => JSON.stringify(oldConfig, null, 2),
    [oldConfig],
  );
  const newText = useMemo(
    () => JSON.stringify(newConfig, null, 2),
    [newConfig],
  );

  return (
    <ReactDiffViewer
      oldValue={oldText}
      newValue={newText}
      splitView={splitView}
      showDiffOnly={showDiffOnly}
      useDarkTheme={isDark}
      styles={{
        diffContainer: {
          maxHeight: 500,
          overflow: 'auto',
          fontSize: 12,
        },
        titleBlock: {
          fontSize: 12,
          fontWeight: 600,
        },
        contentText: {
          fontSize: 12,
        },
      }}
    />
  );
}
