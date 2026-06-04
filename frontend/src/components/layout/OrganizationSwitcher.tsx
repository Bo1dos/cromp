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
  VIEWER: 'green',
};

export default function OrganizationSwitcher() {
  const { activeOrganization, memberships, setActiveOrganization } =
    useOrganization();

  const handleChange = useCallback(
    async (orgId: string) => {
      const membership = memberships.find(
        (m) => m.organization.orgUuid === orgId,
      );
      if (!membership) return;

      await setActiveOrganization(membership.organization);
    },
    [memberships, setActiveOrganization],
  );

  return (
    <Select
      value={activeOrganization?.orgUuid ?? undefined}
      onChange={handleChange}
      style={{ minWidth: 200 }}
      placeholder="Выберите организацию"
      loading={memberships.length === 0 && !!activeOrganization}
      options={memberships.map((m) => ({
        value: m.organization.orgUuid,
        label: m.organization.name,
      }))}
      optionRender={(option) => (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
          }}
        >
          <Text strong>{option.label}</Text>
          <Tag color={ROLE_COLORS[memberships.find(m => m.organization.orgUuid === option.value)?.roleName as keyof typeof ROLE_COLORS] ?? 'default'}>
            {memberships.find(m => m.organization.orgUuid === option.value)?.roleName}
          </Tag>
        </div>
      )}
    />
  );
}
