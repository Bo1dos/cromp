// ---------------------------------------------------------------------------
// JobScheduleTab — displays and manages cron schedule for a job
// ---------------------------------------------------------------------------

import { useState } from 'react';
import {
  Descriptions,
  Tag,
  Button,
  Space,
  Spin,
  Empty,
  Typography,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useSchedule, useDeleteSchedule } from '@/hooks/useSchedules';
import ScheduleForm from '@/components/jobs/ScheduleForm';
import ConfirmModal from '@/components/common/ConfirmModal';
import { formatDateWithTz, formatDateFull } from '@/utils/formatters';

const { Text } = Typography;

interface JobScheduleTabProps {
  jobUuid: string;
}

export default function JobScheduleTab({ jobUuid }: JobScheduleTabProps) {
  const [formOpen, setFormOpen] = useState(false);

  const { data: schedule, isLoading, isError } = useSchedule(jobUuid);
  const deleteMutation = useDeleteSchedule(jobUuid);

  const hasSchedule = !!schedule;

  // ---- Format nextRunAt with timezone --------------------------------
  const renderDate = (dateStr: string | undefined, tz: string) => {
    if (!dateStr) return <Text type="secondary">—</Text>;
    return <Text>{formatDateWithTz(dateStr, tz)}</Text>;
  };

  // ---- Delete handler ------------------------------------------------
  const handleDelete = () => {
    ConfirmModal.show({
      title: 'Remove schedule?',
      content: 'The job will stop running on a schedule. This action can be undone by adding a new schedule.',
      danger: true,
      okText: 'Remove',
      onOk: () => deleteMutation.mutate(),
    });
  };

  // ---- Loading / Empty states ----------------------------------------
  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 48 }}>
        <Spin />
      </div>
    );
  }

  if (isError || !hasSchedule) {
    return (
      <Empty
        image={Empty.PRESENTED_IMAGE_SIMPLE}
        description={
          <Space direction="vertical" align="center">
            <Text type="secondary">No schedule configured</Text>
            <Text type="secondary" style={{ fontSize: 13 }}>
              This job runs manually. Add a cron schedule to automate it.
            </Text>
          </Space>
        }
      >
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setFormOpen(true)}>
          Add Schedule
        </Button>
        <ScheduleForm jobUuid={jobUuid} open={formOpen} onClose={() => setFormOpen(false)} />
      </Empty>
    );
  }

  // ---- Render schedule details ---------------------------------------
  return (
    <div>
      <Descriptions bordered column={1} size="small">
        <Descriptions.Item label="Cron Expression">
          <Tag color="blue" style={{ fontFamily: 'monospace', fontSize: 14 }}>
            {schedule.cronExpression}
          </Tag>
        </Descriptions.Item>

        <Descriptions.Item label="Timezone">
          {schedule.timezone || 'UTC'}
        </Descriptions.Item>

        <Descriptions.Item label="Next Run">
          {renderDate(schedule.nextRunAt, schedule.timezone)}
        </Descriptions.Item>

        <Descriptions.Item label="Status">
          <Tag color={schedule.status === 'ACTIVE' ? 'green' : 'orange'}>
            {schedule.status}
          </Tag>
        </Descriptions.Item>

        <Descriptions.Item label="Created">
          {formatDateFull(schedule.createdAt)}
        </Descriptions.Item>

        <Descriptions.Item label="Updated">
          {formatDateFull(schedule.updatedAt)}
        </Descriptions.Item>
      </Descriptions>

      <Space style={{ marginTop: 16 }}>
        <Button icon={<EditOutlined />} onClick={() => setFormOpen(true)}>
          Edit
        </Button>
        <Button
          danger
          icon={<DeleteOutlined />}
          onClick={handleDelete}
          loading={deleteMutation.isPending}
        >
          Delete
        </Button>
      </Space>

      <ScheduleForm
        jobUuid={jobUuid}
        open={formOpen}
        onClose={() => setFormOpen(false)}
        initialValues={schedule}
      />
    </div>
  );
}
