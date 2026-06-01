// ---------------------------------------------------------------------------
// JobForm — multi-step form for creating & editing jobs
// ---------------------------------------------------------------------------

import { useMemo } from 'react';
import {
  Form,
  Input,
  InputNumber,
  Select,
  Button,
  Steps,
  Typography,
  Space,
  Divider,
} from 'antd';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import type { CreateJobRequest, HttpMethod, JobResponse } from '@/types/job';
import CronInput from './CronInput';

const { TextArea } = Input;
const { Text } = Typography;

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------
const HTTP_METHODS: HttpMethod[] = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS'];

const COMMON_TIMEZONES = [
  'UTC',
  'America/New_York',
  'America/Chicago',
  'America/Denver',
  'America/Los_Angeles',
  'Europe/London',
  'Europe/Berlin',
  'Europe/Paris',
  'Europe/Moscow',
  'Asia/Dubai',
  'Asia/Tokyo',
  'Asia/Shanghai',
  'Asia/Singapore',
  'Australia/Sydney',
  'Pacific/Auckland',
];

// ---------------------------------------------------------------------------
// Props
// ---------------------------------------------------------------------------
interface JobFormProps {
  mode: 'create' | 'edit';
  initialValues?: JobResponse;
  onSubmit: (values: CreateJobRequest) => void;
  loading?: boolean;
}

// ---------------------------------------------------------------------------
// Steps configuration
// ---------------------------------------------------------------------------
const STEPS = ['Basic', 'HTTP Config', 'Retry Policy', 'Schedule'];

export default function JobForm({ mode, initialValues, onSubmit, loading }: JobFormProps) {
  const [form] = Form.useForm();
  const currentStep = Form.useWatch('step', form) ?? 0;

  // Derive default values for create mode
  const defaultValues = useMemo(
    () => ({
      step: 0,
      name: initialValues?.name ?? '',
      description: initialValues?.description ?? '',
      httpConfig: initialValues?.httpConfig ?? {
        url: '',
        method: 'GET' as HttpMethod,
        headers: [],
        body: '',
        timeoutMs: 30_000,
      },
      retryPolicy: initialValues?.retryPolicy ?? {
        maxAttempts: 3,
        backoffMs: 1_000,
        backoffMultiplier: 2,
      },
      cronExpression: initialValues?.cronExpression ?? '',
      timezone: initialValues?.timezone ?? 'UTC',
    }),
    [initialValues],
  );

  const handleNext = async () => {
    try {
      await form.validateFields();
      form.setFieldValue('step', currentStep + 1);
    } catch {
      // validation failed
    }
  };

  const handlePrev = () => {
    form.setFieldValue('step', currentStep - 1);
  };

  const handleFinish = async () => {
    try {
      const values = await form.validateFields();
      // Transform headers from array of {key, value} to Record<string, string>
      const headersRecord: Record<string, string> = {};
      if (values.httpConfig?.headers) {
        for (const h of values.httpConfig.headers) {
          if (h.key) headersRecord[h.key] = h.value ?? '';
        }
      }

      const payload: CreateJobRequest = {
        name: values.name,
        description: values.description,
        httpConfig: {
          url: values.httpConfig.url,
          method: values.httpConfig.method,
          headers: headersRecord,
          body: values.httpConfig.body || undefined,
          timeoutMs: values.httpConfig.timeoutMs,
        },
        retryPolicy: {
          maxAttempts: values.retryPolicy.maxAttempts,
          backoffMs: values.retryPolicy.backoffMs,
          backoffMultiplier: values.retryPolicy.backoffMultiplier,
        },
        cronExpression: values.cronExpression || undefined,
        timezone: values.timezone || undefined,
      };

      onSubmit(payload);
    } catch {
      // validation failed
    }
  };

  const isLastStep = currentStep === STEPS.length - 1;

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={defaultValues}
      onFinish={handleFinish}
      style={{ maxWidth: 800 }}
    >
      {/* Hidden field to track current step */}
      <Form.Item name="step" hidden />

      <Steps
        current={currentStep}
        items={STEPS.map((s) => ({ title: s }))}
        style={{ marginBottom: 32 }}
      />

      {/* ================================================================ */}
      {/* STEP 1 — Basic                                                  */}
      {/* ================================================================ */}
      {currentStep === 0 && (
        <>
          <Form.Item
            name="name"
            label="Job Name"
            rules={[{ required: true, message: 'Please enter a job name' }]}
          >
            <Input placeholder="e.g. Sync users with CRM" />
          </Form.Item>

          <Form.Item name="description" label="Description">
            <TextArea rows={4} placeholder="Optional description…" />
          </Form.Item>
        </>
      )}

      {/* ================================================================ */}
      {/* STEP 2 — HTTP Config                                            */}
      {/* ================================================================ */}
      {currentStep === 1 && (
        <>
          <Form.Item
            name={['httpConfig', 'url']}
            label="URL"
            rules={[
              { required: true, message: 'Please enter a URL' },
              { type: 'url', message: 'Please enter a valid URL' },
            ]}
          >
            <Input placeholder="https://example.com/api/webhook" />
          </Form.Item>

          <Form.Item
            name={['httpConfig', 'method']}
            label="HTTP Method"
            rules={[{ required: true }]}
          >
            <Select options={HTTP_METHODS.map((m) => ({ label: m, value: m }))} />
          </Form.Item>

          <Form.Item label="Headers">
            <Form.List name={['httpConfig', 'headers']}>
              {(fields, { add, remove }) => (
                <div>
                  {fields.map(({ key, name, ...rest }) => (
                    <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                      <Form.Item {...rest} name={[name, 'key']} noStyle>
                        <Input placeholder="Header name" style={{ width: 200 }} />
                      </Form.Item>
                      <Form.Item {...rest} name={[name, 'value']} noStyle>
                        <Input placeholder="Value" style={{ width: 300 }} />
                      </Form.Item>
                      <MinusCircleOutlined onClick={() => remove(name)} />
                    </Space>
                  ))}
                  <Button type="dashed" onClick={() => add()} icon={<PlusOutlined />} size="small">
                    Add Header
                  </Button>
                </div>
              )}
            </Form.List>
          </Form.Item>

          <Form.Item name={['httpConfig', 'body']} label="Request Body (JSON)">
            <TextArea rows={5} placeholder='{"key": "value"}' />
          </Form.Item>

          <Form.Item
            name={['httpConfig', 'timeoutMs']}
            label="Timeout (ms)"
            rules={[{ required: true, message: 'Please set a timeout' }]}
          >
            <InputNumber min={1_000} max={300_000} step={1_000} style={{ width: 200 }} />
          </Form.Item>
        </>
      )}

      {/* ================================================================ */}
      {/* STEP 3 — Retry Policy                                           */}
      {/* ================================================================ */}
      {currentStep === 2 && (
        <>
          <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
            Configure how many times the job should retry on failure and the backoff strategy.
          </Text>

          <Form.Item
            name={['retryPolicy', 'maxAttempts']}
            label="Max Attempts"
            rules={[{ required: true, message: 'Required' }]}
          >
            <InputNumber min={1} max={10} style={{ width: 200 }} />
          </Form.Item>

          <Form.Item
            name={['retryPolicy', 'backoffMs']}
            label="Initial Backoff (ms)"
            rules={[{ required: true, message: 'Required' }]}
          >
            <InputNumber min={100} max={300_000} step={100} style={{ width: 200 }} />
          </Form.Item>

          <Form.Item
            name={['retryPolicy', 'backoffMultiplier']}
            label="Backoff Multiplier"
            rules={[{ required: true, message: 'Required' }]}
          >
            <InputNumber min={1} max={10} step={0.5} style={{ width: 200 }} />
          </Form.Item>
        </>
      )}

      {/* ================================================================ */}
      {/* STEP 4 — Schedule (optional)                                    */}
      {/* ================================================================ */}
      {currentStep === 3 && (
        <>
          <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
            Optionally set a cron schedule. Leave empty for manual-only jobs.
          </Text>

          <Form.Item name="cronExpression" label="Cron Expression">
            <CronInput />
          </Form.Item>

          <Form.Item name="timezone" label="Timezone">
            <Select
              allowClear
              showSearch
              placeholder="UTC"
              options={COMMON_TIMEZONES.map((tz) => ({ label: tz, value: tz }))}
              style={{ width: 280 }}
            />
          </Form.Item>
        </>
      )}

      {/* ================================================================ */}
      {/* Navigation buttons                                              */}
      {/* ================================================================ */}
      <Divider />

      <Space>
        {currentStep > 0 && (
          <Button onClick={handlePrev}>Previous</Button>
        )}
        {!isLastStep ? (
          <Button type="primary" onClick={handleNext}>
            Next
          </Button>
        ) : (
          <Button type="primary" htmlType="submit" loading={loading}>
            {mode === 'create' ? 'Create Job' : 'Save Changes'}
          </Button>
        )}
      </Space>
    </Form>
  );
}
