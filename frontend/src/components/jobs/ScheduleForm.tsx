// ---------------------------------------------------------------------------
// ScheduleForm — modal for creating / editing a cron schedule
// ---------------------------------------------------------------------------

import { useEffect } from 'react';
import { Modal, Form, Select } from 'antd';
import CronInput from '@/components/jobs/CronInput';
import type { CreateUpdateScheduleRequest, ScheduleResponse } from '@/types/schedule';
import { useUpdateSchedule } from '@/hooks/useSchedules';

// Common timezone list (can be expanded)
const COMMON_TIMEZONES = [
  'UTC',
  'America/New_York',
  'America/Chicago',
  'America/Denver',
  'America/Los_Angeles',
  'Europe/London',
  'Europe/Paris',
  'Europe/Berlin',
  'Europe/Moscow',
  'Europe/Istanbul',
  'Asia/Dubai',
  'Asia/Kolkata',
  'Asia/Shanghai',
  'Asia/Tokyo',
  'Asia/Seoul',
  'Asia/Singapore',
  'Australia/Sydney',
  'Pacific/Auckland',
];

interface ScheduleFormProps {
  jobUuid: string;
  open: boolean;
  onClose: () => void;
  initialValues?: ScheduleResponse | null;
}

export default function ScheduleForm({ jobUuid, open, onClose, initialValues }: ScheduleFormProps) {
  const [form] = Form.useForm<CreateUpdateScheduleRequest>();
  const updateMutation = useUpdateSchedule(jobUuid);

  // Reset form when modal opens
  useEffect(() => {
    if (open) {
      form.resetFields();
      if (initialValues) {
        form.setFieldsValue({
          cronExpression: initialValues.cronExpression,
          timezone: initialValues.timezone,
        });
      }
    }
  }, [open, initialValues, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      await updateMutation.mutateAsync(values);
      onClose();
    } catch {
      // validation failed — form will show errors
    }
  };

  return (
    <Modal
      title={initialValues ? 'Edit Schedule' : 'Add Schedule'}
      open={open}
      onOk={handleOk}
      onCancel={onClose}
      confirmLoading={updateMutation.isPending}
      destroyOnClose
      width={560}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{ cronExpression: '', timezone: 'UTC' }}
      >
        <Form.Item
          name="cronExpression"
          label="Cron Expression"
          rules={[
            { required: true, message: 'Please enter a cron expression' },
            {
              pattern: /^(\*|([0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9])|\*\/[0-9]+)(\s+(\*|([0-9]|1[0-9]|2[0-3])|\*\/[0-9]+)){4}$/,
              message: 'Invalid 5-field cron expression',
            },
          ]}
        >
          <CronInput />
        </Form.Item>

        <Form.Item
          name="timezone"
          label="Timezone"
          rules={[{ required: true, message: 'Please select a timezone' }]}
        >
          <Select
            showSearch
            placeholder="Select timezone"
            options={COMMON_TIMEZONES.map((tz) => ({ label: tz, value: tz }))}
            filterOption={(input, option) =>
              (option?.label as string ?? '').toLowerCase().includes(input.toLowerCase())
            }
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
