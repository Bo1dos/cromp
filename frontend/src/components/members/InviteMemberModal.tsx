// ---------------------------------------------------------------------------
// InviteMemberModal — modal form to invite a new user to the organisation
// ---------------------------------------------------------------------------

import { Modal, Form, Input, Select } from 'antd';
import { useInviteMember } from '@/hooks/useMembers';
import { useOrganization } from '@/hooks/useOrganization';

interface InviteMemberModalProps {
  open: boolean;
  onClose: () => void;
}

interface InviteFormValues {
  email: string;
  role: 'ADMIN' | 'MEMBER';
}

export default function InviteMemberModal({ open, onClose }: InviteMemberModalProps) {
  const [form] = Form.useForm<InviteFormValues>();
  const inviteMember = useInviteMember();
  const { activeOrganization } = useOrganization();

  const handleSubmit = async (values: InviteFormValues) => {
    if (!activeOrganization) return;

    try {
      await inviteMember.mutateAsync({
        email: values.email,
        organizationUuid: activeOrganization.uuid,
        role: values.role,
      });
      form.resetFields();
      onClose();
    } catch {
      // Error notification handled by mutation hook
    }
  };

  return (
    <Modal
      title="Invite Member"
      open={open}
      onCancel={onClose}
      onOk={() => form.submit()}
      confirmLoading={inviteMember.isPending}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={{ role: 'MEMBER' }}
      >
        <Form.Item
          label="Email"
          name="email"
          rules={[
            { required: true, message: 'Email is required' },
            { type: 'email', message: 'Please enter a valid email' },
          ]}
        >
          <Input placeholder="user@example.com" />
        </Form.Item>

        <Form.Item
          label="Role"
          name="role"
          rules={[{ required: true, message: 'Role is required' }]}
        >
          <Select
            options={[
              { label: 'Admin', value: 'ADMIN' },
              { label: 'Member', value: 'MEMBER' },
            ]}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
}
