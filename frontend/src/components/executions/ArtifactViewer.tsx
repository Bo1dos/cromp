// ---------------------------------------------------------------------------
// ArtifactViewer — tabs with stdout / stderr content, copy button
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Tabs, Button, Spin, Empty, message } from 'antd';
import { CopyOutlined } from '@ant-design/icons';
import { useArtifacts } from '@/hooks/useExecutions';

interface ArtifactViewerProps {
  executionId: string;
}

export default function ArtifactViewer({ executionId }: ArtifactViewerProps) {
  const { data: artifacts, isLoading } = useArtifacts(executionId);
  const [activeTab, setActiveTab] = useState<string>('stdout');

  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: 40 }}>
        <Spin />
      </div>
    );
  }

  if (!artifacts || artifacts.length === 0) {
    return <Empty description="No artifacts" />;
  }

  const stdoutArtifact = artifacts.find((a) => a.type === 'stdout');
  const stderrArtifact = artifacts.find((a) => a.type === 'stderr');

  const handleCopy = (content: string) => {
    navigator.clipboard.writeText(content).then(() => {
      message.success('Copied to clipboard');
    });
  };

  const tabItems = [
    ...(stdoutArtifact
      ? [
          {
            key: 'stdout',
            label: `stdout (${(stdoutArtifact.sizeBytes / 1024).toFixed(1)} KB)`,
            children: (
              <div style={{ position: 'relative' }}>
                <Button
                  icon={<CopyOutlined />}
                  size="small"
                  style={{ position: 'absolute', top: 8, right: 8, zIndex: 1 }}
                  onClick={() => handleCopy(stdoutArtifact.content)}
                >
                  Copy
                </Button>
                <pre
                  style={{
                    margin: 0,
                    padding: 16,
                    background: 'var(--color-bg-layout, #f6f8fa)',
                    border: '1px solid #d9d9d9',
                    borderRadius: 6,
                    maxHeight: 400,
                    overflow: 'auto',
                    fontFamily: "'SF Mono', 'Fira Code', 'Consolas', monospace",
                    fontSize: 13,
                    lineHeight: 1.5,
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-all',
                  }}
                >
                  {stdoutArtifact.content}
                </pre>
              </div>
            ),
          },
        ]
      : []),
    ...(stderrArtifact
      ? [
          {
            key: 'stderr',
            label: `stderr (${(stderrArtifact.sizeBytes / 1024).toFixed(1)} KB)`,
            children: (
              <div style={{ position: 'relative' }}>
                <Button
                  icon={<CopyOutlined />}
                  size="small"
                  style={{ position: 'absolute', top: 8, right: 8, zIndex: 1 }}
                  onClick={() => handleCopy(stderrArtifact.content)}
                >
                  Copy
                </Button>
                <pre
                  style={{
                    margin: 0,
                    padding: 16,
                    background: 'var(--color-bg-layout, #fff1f0)',
                    border: '1px solid #ffa39e',
                    borderRadius: 6,
                    maxHeight: 400,
                    overflow: 'auto',
                    fontFamily: "'SF Mono', 'Fira Code', 'Consolas', monospace",
                    fontSize: 13,
                    lineHeight: 1.5,
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-all',
                    color: '#cf1322',
                  }}
                >
                  {stderrArtifact.content}
                </pre>
              </div>
            ),
          },
        ]
      : []),
  ];

  if (tabItems.length === 0) {
    return <Empty description="No artifacts" />;
  }

  return (
    <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
  );
}
