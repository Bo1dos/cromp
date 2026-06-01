// ---------------------------------------------------------------------------
// RotateSecretModal — modal for rotating a secret's value
// IMPORTANT: The new value is transmitted once and NEVER stored in state.
// ---------------------------------------------------------------------------

import { useEffect } from 'react';
import { Modal, Form, Input, Alert } from 'antd';
import { useRotateSecret } from '@/hooks/useSecrets';

interface RotateSecretModalProps {
  secretUuid: string;
  secretName: string;
  open: boolean;
  onClose: () => void;
}

export default function RotateSecretModal({
  secretUuid,
  secretName,
  open,
  onClose,
}: RotateSecretModalProps) {
  const [form] = Form.useForm();
  const rotateMutation = useRotateSecret(secretUuid);

  // Reset form whenever modal opens
  useEffect(() => {
    if (open) {
      form.resetFields();
    }
  }, [open, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      rotateMutation.mutate(values, {
        onSuccess: () => {
          form.resetFields();
          onClose();
        },
      });
    } catch {
      // validation failed
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onClose();
  };

  return (
    <Modal
      title={`Rotate Secret — ${secretName}`}
      open={open}
      onOk={handleSubmit}
      onCancel={handleCancel}
      confirmLoading={rotateMutation.isPending}
      okText="Rotate"
      destroyOnClose
      maskClosable={false}
    >
      <Alert
        type="warning"
        showIcon
        message="After rotation, the old value will become invalid immediately."
        description="Any jobs using this secret should be updated if they cache the old value."
        style={{ marginBottom: 16 }}
      />

      <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
        <Form.Item
          name="value"
          label="New Value"
          rules={[
            { required: true, message: 'Please enter the new value' },
            { min: 3, message: 'Value must be at least 3 characters' },
          ]}
          extra="The new value will never be displayed again after rotation."
        >
          <Input.Password placeholder="Enter new secret value" autoFocus />
        </Form.Item>
      </Form>
    </Modal>
  );
}
