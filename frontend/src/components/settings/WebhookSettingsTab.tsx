import { useState } from 'react';
import {
  Card, Table, Button, Modal, Form, Input, Select, Switch,
  Tag, Space, Typography, Alert, Popconfirm, List, Empty,
} from 'antd';
import {
  PlusOutlined, ApiOutlined, CopyOutlined,
  ReloadOutlined, DeleteOutlined, HistoryOutlined,
} from '@ant-design/icons';
import {
  useWebhookSubscriptions,
  useCreateWebhook,
  useUpdateWebhook,
  useDeleteWebhook,
  useRotateSecret,
  useWebhookDeliveries,
} from '@/hooks/useWebhooks';
import { useLanguage } from '@/hooks/useLanguage';
import type { WebhookSubscription } from '@/types/webhook';

const { Text, Paragraph } = Typography;

const EVENT_OPTIONS = [
  'JOB_EXECUTION_FAILED', 'JOB_EXECUTION_SUCCEEDED', 'JOB_DISABLED', 'JOB_EXECUTION_TIMEOUT',
  'INVITATION_CREATED', 'INVITATION_ACCEPTED', 'MEMBER_ADDED', 'MEMBER_REMOVED',
];

export default function WebhookSettingsTab() {
  const { data: subs, isLoading } = useWebhookSubscriptions();
  const createMut = useCreateWebhook();
  const updateMut = useUpdateWebhook();
  const deleteMut = useDeleteWebhook();
  const rotateMut = useRotateSecret();
  const { t } = useLanguage();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<WebhookSubscription | null>(null);
  const [newSecret, setNewSecret] = useState<string | null>(null);
  const [deliveriesUuid, setDeliveriesUuid] = useState<string | null>(null);
  const [form] = Form.useForm();

  const openCreate = () => {
    setEditing(null);
    setNewSecret(null);
    form.resetFields();
    form.setFieldsValue({ eventTypes: ['JOB_EXECUTION_FAILED'] });
    setModalOpen(true);
  };

  const openEdit = (sub: WebhookSubscription) => {
    setEditing(sub);
    setNewSecret(null);
    form.setFieldsValue({ url: sub.url, eventTypes: sub.eventTypes });
    setModalOpen(true);
  };

  const handleSubmit = () => {
    form.validateFields().then((values: { url: string; eventTypes: string[] }) => {
      if (editing) {
        updateMut.mutate({ uuid: editing.subscriptionUuid, req: values });
      } else {
        createMut.mutate(values, {
          onSuccess: (data) => {
            if (data.secret) setNewSecret(data.secret);
          },
        });
      }
      setModalOpen(false);
    });
  };

  const { data: deliveries } = useWebhookDeliveries(deliveriesUuid ?? '');

  const columns = [
    {
      title: 'URL',
      dataIndex: 'url',
      key: 'url',
      ellipsis: true,
      render: (url: string) => <Text copyable>{url}</Text>,
    },
    {
      title: 'Events',
      dataIndex: 'eventTypes',
      key: 'eventTypes',
      render: (types: string[]) => (
        <Space size={4} wrap>
          {types.map((t) => (
            <Tag key={t} color="blue">{t.replace(/_/g, ' ').substring(0, 20)}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 80,
      render: (_: boolean, record: WebhookSubscription) => (
        <Switch
          size="small"
          checked={record.enabled}
          onChange={() =>
            updateMut.mutate({
              uuid: record.subscriptionUuid,
              req: { url: record.url, eventTypes: record.eventTypes },
            })
          }
        />
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 180,
      render: (_: unknown, record: WebhookSubscription) => (
        <Space>
          <Button size="small" icon={<HistoryOutlined />}
            onClick={() => setDeliveriesUuid(record.subscriptionUuid)} />
          <Button size="small" icon={<ReloadOutlined />}
            onClick={() => rotateMut.mutate(record.subscriptionUuid)} />
          <Popconfirm title="Delete this webhook?" onConfirm={() => deleteMut.mutate(record.subscriptionUuid)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card
        title={<Space><ApiOutlined />Webhooks</Space>}
        extra={<Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Add Webhook</Button>}
      >
        <Table
          dataSource={subs ?? []}
          columns={columns}
          rowKey="subscriptionUuid"
          loading={isLoading}
          pagination={false}
          locale={{ emptyText: <Empty description="No webhooks configured" /> }}
          onRow={(record) => ({
            onDoubleClick: () => openEdit(record),
          })}
        />
      </Card>

      {/* Create/Edit Modal */}
      <Modal
        title={editing ? 'Edit Webhook' : 'Add Webhook'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        confirmLoading={createMut.isPending || updateMut.isPending}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="url" label="Webhook URL" rules={[{ required: true, type: 'url', message: 'Enter a valid HTTPS URL' }]}>
            <Input placeholder="https://hooks.example.com/cromp" />
          </Form.Item>
          <Form.Item name="eventTypes" label="Event Types" rules={[{ required: true }]}>
            <Select mode="multiple" placeholder="Select events" options={EVENT_OPTIONS.map((e) => ({ value: e, label: e.replace(/_/g, ' ') }))} />
          </Form.Item>
        </Form>
        {newSecret && (
          <Alert
            type="warning"
            showIcon
            message="Save this secret now — it won't be shown again!"
            description={
              <Paragraph copyable code style={{ wordBreak: 'break-all' }}>
                {newSecret}
              </Paragraph>
            }
          />
        )}
      </Modal>

      {/* Deliveries Modal */}
      <Modal
        title="Delivery History"
        open={!!deliveriesUuid}
        onCancel={() => setDeliveriesUuid(null)}
        footer={null}
        width={700}
      >
        <List
          dataSource={deliveries ?? []}
          locale={{ emptyText: <Empty description="No deliveries yet" /> }}
          renderItem={(d) => (
            <List.Item>
              <List.Item.Meta
                title={
                  <Space>
                    <Tag color={d.status === 'SUCCEEDED' ? 'green' : d.status === 'FAILED' ? 'red' : 'default'}>
                      {d.status}
                    </Tag>
                    <Text>HTTP {d.responseCode}</Text>
                    <Text type="secondary">Attempt #{d.attemptNumber}</Text>
                  </Space>
                }
                description={d.errorMessage || d.attemptedAt}
              />
            </List.Item>
          )}
        />
      </Modal>
    </Space>
  );
}
