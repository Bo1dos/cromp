// ---------------------------------------------------------------------------
// ArtifactViewer — shows all artifacts with download/preview per kind
// ---------------------------------------------------------------------------

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Tag, Empty, Space, Image, Spin, message } from 'antd';
import { DownloadOutlined, FileTextOutlined, FileImageOutlined } from '@ant-design/icons';
import { useArtifacts } from '@/hooks/useExecutions';
import { useOrganization } from '@/hooks/useOrganization';
import { formatDateFull } from '@/utils/formatters';
import apiClient from '@/api/client';
import type { ArtifactResponse } from '@/types/execution';

interface ArtifactViewerProps {
  executionId: string;
}

function isImage(contentType: string): boolean {
  return contentType.startsWith('image/');
}

/** Relative path for axios calls (apiClient already prepends /api/v1) */
function getDownloadPath(orgId: string, executionId: string, artifactId: number): string {
  return `/organizations/${orgId}/executions/${executionId}/artifacts/${artifactId}/download`;
}

/** Fetches an image via axios (with auth) and returns a blob URL */
function useImageBlobUrl(orgId: string, executionId: string, artifact: ArtifactResponse): string | null {
  const [url, setUrl] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const fetchImage = async () => {
      try {
        const resp = await apiClient.get(
          getDownloadPath(orgId, executionId, artifact.id),
          { responseType: 'blob' },
        );
        if (!cancelled) {
          setUrl(URL.createObjectURL(resp.data));
        }
      } catch {
        // ignore auth errors
      }
    };
    if (isImage(artifact.contentType)) {
      fetchImage();
    }
    return () => {
      cancelled = true;
    };
  }, [orgId, executionId, artifact.id, artifact.contentType]);

  return url;
}

export default function ArtifactViewer({ executionId }: ArtifactViewerProps) {
  const { data: artifacts, isLoading } = useArtifacts(executionId);
  const { activeOrganization } = useOrganization();
  const orgId = activeOrganization?.orgUuid ?? '';

  if (!artifacts || artifacts.length === 0) {
    return <Empty description="No artifacts" />;
  }

  const columns = [
    {
      title: 'Kind',
      dataIndex: 'kind',
      key: 'kind',
      width: 150,
      render: (kind: string) => {
        const colorMap: Record<string, string> = {
          OUTPUT_PAYLOAD: 'blue',
          LOG_STDOUT: 'green',
          LOG_STDERR: 'red',
          DEBUG_SNAPSHOT: 'purple',
        };
        return <Tag color={colorMap[kind] || 'default'}>{kind}</Tag>;
      },
    },
    {
      title: 'Size',
      dataIndex: 'sizeBytes',
      key: 'size',
      width: 100,
      render: (bytes: number) => {
        if (bytes < 1024) return `${bytes} B`;
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
      },
    },
    {
      title: 'Type',
      dataIndex: 'contentType',
      key: 'contentType',
      width: 180,
      render: (ct: string) => (
        <Space size={4}>
          {isImage(ct) ? <FileImageOutlined /> : <FileTextOutlined />}
          <span>{ct}</span>
        </Space>
      ),
    },
    {
      title: 'Uploaded',
      dataIndex: 'uploadedAt',
      key: 'uploadedAt',
      width: 170,
      render: (val: string) => formatDateFull(val),
    },
    {
      title: '',
      key: 'actions',
      width: 120,
      render: (_: unknown, record: ArtifactResponse) => (
        <DownloadButton
          orgId={orgId}
          executionId={executionId}
          artifact={record}
        />
      ),
    },
  ];

  const imageArtifacts = artifacts.filter((a) => isImage(a.contentType));

  return (
    <div>
      {imageArtifacts.length > 0 && (
        <Space wrap style={{ marginBottom: 16 }}>
          {imageArtifacts.map((a) => (
            <ImagePreview
              key={a.id}
              orgId={orgId}
              executionId={executionId}
              artifact={a}
            />
          ))}
        </Space>
      )}

      <Table
        columns={columns}
        dataSource={artifacts}
        rowKey="id"
        loading={isLoading}
        pagination={false}
        size="small"
      />
    </div>
  );
}

function ImagePreview({ orgId, executionId, artifact }: {
  orgId: string;
  executionId: string;
  artifact: ArtifactResponse;
}) {
  const blobUrl = useImageBlobUrl(orgId, executionId, artifact);

  if (!blobUrl) {
    return (
      <div style={{ width: 120, height: 120, display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f5f5f5', borderRadius: 6 }}>
        <Spin size="small" />
      </div>
    );
  }

  return (
    <Image
      width={120}
      height={120}
      style={{ objectFit: 'cover', borderRadius: 6 }}
      src={blobUrl}
    />
  );
}

function DownloadButton({ orgId, executionId, artifact }: {
  orgId: string;
  executionId: string;
  artifact: ArtifactResponse;
}) {
  const [downloading, setDownloading] = useState(false);

  const handleDownload = useCallback(async () => {
    setDownloading(true);
    try {
      const resp = await apiClient.get(
        getDownloadPath(orgId, executionId, artifact.id),
        { responseType: 'blob' },
      );
      // Create a download link from the blob
      const blobUrl = URL.createObjectURL(resp.data);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = `artifact-${artifact.id}-${artifact.kind.toLowerCase()}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(blobUrl);
    } catch {
      message.error('Failed to download artifact');
    } finally {
      setDownloading(false);
    }
  }, [orgId, executionId, artifact.id, artifact.kind]);

  return (
    <Button
      type="primary"
      size="small"
      icon={<DownloadOutlined />}
      loading={downloading}
      onClick={handleDownload}
    >
      Download
    </Button>
  );
}
