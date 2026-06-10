// ---------------------------------------------------------------------------
// JobForm — multi-step form for creating & editing jobs
// ---------------------------------------------------------------------------

import { useEffect, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
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
  notification,
} from 'antd';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import type { CreateJobRequest, HttpMethod, JobResponse } from '@/types/job';
import type { SecretResponse } from '@/types/secret';
import { listSecrets } from '@/api/secrets.api';
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
const STEPS = ['Basic', 'HTTP Config', 'Retry Policy', 'Secrets', 'Schedule'];

// Fields per step — used to validate only visible fields on Next
const STEP_FIELDS: (string | string[])[] = [
  ['name'],                                                              // Step 0: Basic
  [['httpConfig', 'url'], ['httpConfig', 'method'], ['httpConfig', 'timeoutMs']], // Step 1: HTTP
  [['retryPolicy', 'maxAttempts'], ['retryPolicy', 'backoffMs'], ['retryPolicy', 'backoffMultiplier']], // Step 2: Retry
  [],                                                                    // Step 3: Secrets (optional)
  [],                                                                    // Step 4: Schedule (optional)
];

export default function JobForm({ mode, initialValues, onSubmit, loading }: JobFormProps) {
  const [form] = Form.useForm();
  const currentStep = Form.useWatch('step', form) ?? 0;

  // Derive default values for create / edit mode.
  // Applied once via useEffect — NOT via Form initialValues prop, because
  // Form.useWatch re-renders cause initialValues to overwrite user input.
  const defaultValues = useMemo(() => {
    const cfg = initialValues?.currentConfig;
    return {
      step: 0,
      name: initialValues?.name ?? '',
      description: initialValues?.description ?? '',
      httpConfig: {
        url: cfg?.target?.url ?? '',
        method: (cfg?.target?.method ?? 'GET') as HttpMethod,
        headers: cfg?.target?.headers
          ? Object.entries(cfg.target.headers).map(([key, value]) => ({ key, value }))
          : [],
        body: cfg?.target?.body ?? '',
        timeoutMs: cfg?.timeoutMs ?? 30_000,
      },
      retryPolicy: {
        maxAttempts: cfg?.retryPolicy?.maxAttempts ?? 3,
        backoffMs: cfg?.retryPolicy?.backoffMs ?? 1_000,
        backoffMultiplier: cfg?.retryPolicy?.backoffMultiplier ?? 2,
      },
      secrets: cfg?.secrets?.map((s) => ({ secretId: s.secretId, envName: s.envName })) ?? [],
      cronExpression: initialValues?.cronExpression ?? '',
      timezone: initialValues?.timezone ?? 'UTC',
    };
  }, [initialValues]);

  useEffect(() => {
    form.setFieldsValue(defaultValues);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [defaultValues]);

  // Fetch secrets for the dropdown (page 1, large pageSize to get all)
  const { data: secretsData } = useQuery<SecretResponse[], Error>({
    queryKey: ['secrets'],
    queryFn: () => listSecrets({ limit: 200, offset: 0 }),
    staleTime: 120_000,
  });
  const secretsList: SecretResponse[] = secretsData ?? [];

  const handleNext = async () => {
    try {
      // Validate only fields visible on the current step
      const fields = STEP_FIELDS[currentStep];
      if (fields.length > 0) {
        await form.validateFields(fields as any);
      }
      form.setFieldValue('step', currentStep + 1);
    } catch {
      // validation failed — Ant Design shows inline errors
    }
  };

  const handlePrev = () => {
    form.setFieldValue('step', currentStep - 1);
  };

  const handleFinish = () => {
    // Read ALL fields directly from the store (including unmounted ones).
    // The onFinish argument can be stale when Form.useWatch re-renders
    // cause initialValues to race against user input.
    const values = form.getFieldsValue(true) as Record<string, any>;
    console.log('[JobForm] handleFinish — raw store values:', values);

    // Transform headers from array of {key, value} to Record<string, string>
    const headersRecord: Record<string, string> = {};
    if (values.httpConfig?.headers) {
      for (const h of values.httpConfig.headers) {
        if (h.key) headersRecord[h.key] = h.value ?? '';
      }
    }

    // Map form fields → backend CreateJobRequest shape:
    //   httpConfig.{url,method,headers,body} → config.target.{url,method,headers,body}
    //   httpConfig.timeoutMs               → config.timeoutMs
    //   retryPolicy                        → config.retryPolicy
    const payload: CreateJobRequest = {
      name: values.name,
      description: values.description || '',
      config: {
        target: {
          type: 'HTTP',
          url: values.httpConfig?.url ?? '',
          method: values.httpConfig?.method ?? 'GET',
          headers: headersRecord,
          body: values.httpConfig?.body || undefined,
        },
        retryPolicy: {
          maxAttempts: values.retryPolicy?.maxAttempts ?? 3,
          backoffMs: values.retryPolicy?.backoffMs ?? 1_000,
          backoffMultiplier: values.retryPolicy?.backoffMultiplier ?? 2,
        },
        timeoutMs: values.httpConfig?.timeoutMs ?? 30_000,
        secrets: (values.secrets ?? [])
        .filter((s: any) => s && s.secretId)
        .map((s: any) => ({ secretId: s.secretId, envName: s.envName || s.secretId })),
      },
      queueName: undefined,
      priority: 0,
    };

    console.log('[JobForm] Submitting payload:', payload);
    onSubmit(payload);
  };

  const isLastStep = currentStep === STEPS.length - 1;

  return (
    <Form
      form={form}
      layout="vertical"
      onFinish={handleFinish}
      onFinishFailed={({ errorFields, values }) => {
        console.error('[JobForm] Validation failed:', { errorFields, values });

        // Show notification with human-readable field names
        const fieldNames = errorFields
          .map((f) => (Array.isArray(f.name) ? f.name.join(' > ') : String(f.name)))
          .join(', ');
        notification.error({
          message: 'Validation failed',
          description: `Please fix the following fields: ${fieldNames || 'unknown fields'}`,
          duration: 5,
        });

        // Scroll to the first field with an error
        if (errorFields?.length > 0) {
          form.scrollToField(errorFields[0].name, { behavior: 'smooth' });
        }
      }}
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
      {/* STEP 4 — Secrets (optional)                                     */}
      {/* ================================================================ */}
      {currentStep === 3 && (
        <>
          <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
            Bind secrets from the secrets vault to HTTP headers. The secret value
            will be injected as a header named <Text code>envName</Text> when the job executes.
          </Text>

          <Form.List name="secrets">
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...rest }) => (
                  <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                    <Form.Item
                      {...rest}
                      name={[name, 'secretId']}
                      label="Secret"
                      rules={[{ required: true, message: 'Select a secret' }]}
                    >
                      <Select
                        showSearch
                        placeholder="Choose secret…"
                        style={{ width: 220 }}
                        filterOption={(input, option) =>
                          (option?.label as string)?.toLowerCase().includes(input.toLowerCase())
                        }
                        options={secretsList.map((s) => ({
                          label: `${s.name} (${s.scope})`,
                          value: s.secretUuid,
                        }))}
                      />
                    </Form.Item>
                    <Form.Item
                      {...rest}
                      name={[name, 'envName']}
                      label="Header Name"
                      rules={[{ required: true, message: 'Header name required' }]}
                    >
                      <Input placeholder="e.g. X-API-Key" style={{ width: 200 }} />
                    </Form.Item>
                    <MinusCircleOutlined
                      onClick={() => remove(name)}
                      style={{ marginTop: 30 }}
                    />
                  </Space>
                ))}
                <Button
                  type="dashed"
                  onClick={() => add({ secretId: undefined, envName: '' })}
                  icon={<PlusOutlined />}
                  size="small"
                  disabled={secretsList.length === 0}
                >
                  {secretsList.length === 0 ? 'No secrets available' : 'Bind Secret'}
                </Button>
              </>
            )}
          </Form.List>
        </>
      )}

      {/* ================================================================ */}
      {/* STEP 5 — Schedule (optional)                                    */}
      {/* ================================================================ */}
      {currentStep === 4 && (
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
          <Button
            type="primary"
            loading={loading}
            onClick={() => {
              console.log('[JobForm] Submit button clicked, calling form.submit()');
              form.submit();
            }}
          >
            {mode === 'create' ? 'Create Job' : 'Save Changes'}
          </Button>
        )}
      </Space>
    </Form>
  );
}
