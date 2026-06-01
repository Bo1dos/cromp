// ---------------------------------------------------------------------------
// JobExecutionsTab — ExecutionTable pre-filtered by jobId, used in JobDetailPage
// ---------------------------------------------------------------------------

import { Button, Typography } from 'antd';
import { LinkOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import ExecutionTable from './ExecutionTable';

interface JobExecutionsTabProps {
  jobUuid: string;
}

const { Text } = Typography;

export default function JobExecutionsTab({ jobUuid }: JobExecutionsTabProps) {
  const navigate = useNavigate();

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Text type="secondary">Showing executions for the current job</Text>
        <Button
          icon={<LinkOutlined />}
          onClick={() => navigate(`/dashboard/executions?jobId=${jobUuid}`)}
        >
          View all executions
        </Button>
      </div>
      <ExecutionTable jobId={jobUuid} />
    </div>
  );
}
