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
  Modal,
  Typography,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';
import { useSchedule, useDeleteSchedule } from '@/hooks/useSchedules';
import ScheduleForm from '@/components/jobs/ScheduleForm';

dayjs.extend(utc);
dayjs.extend(timezone);

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
  const formatDate = (dateStr: string | undefined, tz: string) => {
    if (!dateStr) return <Text type="secondary">—</Text>;
    return <Text>{dayjs(dateStr).tz(tz).format('MMMM D, YYYY HH:mm [UTC]Z')}</Text>;
  };

  // ---- Delete handler ------------------------------------------------
  const handleDelete = () => {
    Modal.confirm({
      title: 'Remove schedule?',
      content: 'The job will stop running on a schedule. This action can be undone by adding a new schedule.',
      okText: 'Remove',
      okType: 'danger',
      cancelText: 'Cancel',
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
      <Descriptions bordered column={1} size="small" style={{ background: '#fff' }}>
        <Descriptions.Item label="Cron Expression">
          <Tag color="blue" style={{ fontFamily: 'monospace', fontSize: 14 }}>
            {schedule.cronExpression}
          </Tag>
        </Descriptions.Item>

        <Descriptions.Item label="Timezone">
          {schedule.timezone || 'UTC'}
        </Descriptions.Item>

        <Descriptions.Item label="Next Run">
          {formatDate(schedule.nextRunAt, schedule.timezone)}
        </Descriptions.Item>

        <Descriptions.Item label="Status">
          <Tag color={schedule.status === 'ACTIVE' ? 'green' : 'orange'}>
            {schedule.status}
          </Tag>
        </Descriptions.Item>

        <Descriptions.Item label="Created">
          {dayjs(schedule.createdAt).format('YYYY-MM-DD HH:mm:ss')}
        </Descriptions.Item>

        <Descriptions.Item label="Updated">
          {dayjs(schedule.updatedAt).format('YYYY-MM-DD HH:mm:ss')}
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
