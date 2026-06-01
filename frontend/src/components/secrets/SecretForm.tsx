// ---------------------------------------------------------------------------
// SecretForm — modal form for creating a new secret
// IMPORTANT: The secret value is NEVER stored in React state after submission.
// It is only held transiently in the form and cleared on close.
// ---------------------------------------------------------------------------

import { useEffect } from 'react';
import { Modal, Form, Input, Select } from 'antd';
import type { CreateSecretRequest, SecretScope } from '@/types/secret';
import { useCreateSecret } from '@/hooks/useSecrets';

const { TextArea } = Input;

interface SecretFormProps {
  open: boolean;
  onClose: () => void;
  /** Called after successful creation with the created secret */
  onCreated?: (secret: { uuid: string; name: string }) => void;
}

export default function SecretForm({ open, onClose, onCreated }: SecretFormProps) {
  const [form] = Form.useForm<CreateSecretRequest>();
  const createMutation = useCreateSecret();

  // Reset form whenever modal opens
  useEffect(() => {
    if (open) {
      form.resetFields();
    }
  }, [open, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      createMutation.mutate(values, {
        onSuccess: (secret) => {
          form.resetFields();
          onClose();
          onCreated?.({ uuid: secret.uuid, name: secret.name });
        },
      });
    } catch {
      // validation failed — do nothing
    }
  };

  const handleCancel = () => {
    // Clear any sensitive data from the form
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title="Create Secret"
      open={open}
      onOk={handleSubmit}
      onCancel={handleCancel}
      confirmLoading={createMutation.isPending}
      okText="Create"
      destroyOnClose
      maskClosable={false}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{ scope: 'JOB' as SecretScope }}
        style={{ marginTop: 16 }}
      >
        <Form.Item
          name="name"
          label="Name"
          rules={[
            { required: true, message: 'Please enter a secret name' },
            { min: 2, message: 'Name must be at least 2 characters' },
          ]}
        >
          <Input placeholder="e.g. DATABASE_PASSWORD" autoFocus />
        </Form.Item>

        <Form.Item
          name="value"
          label="Value"
          rules={[
            { required: true, message: 'Please enter the secret value' },
            { min: 3, message: 'Value must be at least 3 characters' },
          ]}
          extra="The value will be encrypted at rest. It will never be displayed again after creation."
        >
          <Input.Password placeholder="Enter secret value" />
        </Form.Item>

        <Form.Item name="description" label="Description">
          <TextArea rows={3} placeholder="Optional description…" />
        </Form.Item>

        <Form.Item
          name="scope"
          label="Scope"
          rules={[{ required: true }]}
        >
          <Select
            options={[
              { label: 'Job', value: 'JOB' },
              { label: 'Organization', value: 'ORGANIZATION' },
            ]}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
