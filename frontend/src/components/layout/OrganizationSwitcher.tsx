// ---------------------------------------------------------------------------
// OrganizationSwitcher — dropdown to switch between organisations
// ---------------------------------------------------------------------------

import { useCallback } from 'react';
import { Select, Tag, Typography } from 'antd';

import { useOrganization } from '@/hooks/useOrganization';
import type { OrganizationRole } from '@/types/organization';

const { Text } = Typography;

/** Map role → colour for Ant Design Tag */
const ROLE_COLORS: Record<OrganizationRole, string> = {
  OWNER: 'red',
  ADMIN: 'orange',
  MEMBER: 'blue',
};

export default function OrganizationSwitcher() {
  const { activeOrganization, memberships, setActiveOrganization } =
    useOrganization();

  const handleChange = useCallback(
    async (orgId: string) => {
      const membership = memberships.find(
        (m) => m.organization.uuid === orgId,
      );
      if (!membership) return;

      await setActiveOrganization(membership.organization);
    },
    [memberships, setActiveOrganization],
  );

  return (
    <Select
      value={activeOrganization?.uuid ?? undefined}
      onChange={handleChange}
      style={{ minWidth: 200 }}
      placeholder="Выберите организацию"
      options={memberships.map((m) => ({
        value: m.organization.uuid,
        label: (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
            }}
          >
            <Text strong>{m.organization.name}</Text>
            <Tag color={ROLE_COLORS[m.role]}>{m.role}</Tag>
          </div>
        ),
      }))}
    />
  );
}
