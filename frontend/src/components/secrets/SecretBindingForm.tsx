// ---------------------------------------------------------------------------
// SecretBindingForm — select secrets to bind to a job (for use in JobForm)
// Uses Transfer component for picking secrets from the available list.
// Includes a "Create New Secret" button that opens SecretForm in a modal.
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Select, Button, Space } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useSecretsList } from '@/hooks/useSecrets';
import SecretForm from './SecretForm';

interface SecretBindingFormProps {
  /** Currently selected secret UUIDs */
  value?: string[];
  /** Called when selection changes */
  onChange?: (selectedIds: string[]) => void;
}

export default function SecretBindingForm({ value = [], onChange }: SecretBindingFormProps) {
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const { data: secrets, isLoading } = useSecretsList({
    scope: '',
    search: '',
    page: 1,
    pageSize: 100,
  });

  const handleCreated = (secret: { uuid: string; name: string }) => {
    // Auto-select the newly created secret
    if (!value.includes(secret.secretUuid)) {
      onChange?.([...value, secret.secretUuid]);
    }
  };

  return (
    <div>
      <Space direction="vertical" style={{ width: '100%' }}>
        <Select
          mode="multiple"
          placeholder="Select secrets to bind to this job…"
          value={value}
          onChange={onChange}
          loading={isLoading}
          style={{ width: '100%' }}
          options={(secrets ?? []).map((s) => ({
            label: s.name,
            value: s.uuid,
          }))}
          showSearch
          optionFilterProp="label"
          notFoundContent={isLoading ? 'Loading…' : 'No secrets found'}
          allowClear
        />

        <Button
          type="dashed"
          icon={<PlusOutlined />}
          onClick={() => setCreateModalOpen(true)}
        >
          Create New Secret
        </Button>
      </Space>

      <SecretForm
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onCreated={handleCreated}
      />
    </div>
  );
}
