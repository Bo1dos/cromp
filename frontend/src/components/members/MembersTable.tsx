// ---------------------------------------------------------------------------
// MembersTable — Ant Design Table listing organisation members
// ---------------------------------------------------------------------------

import { Table, Space, Button, Tag, Select, Avatar, Typography } from 'antd';
import { DeleteOutlined, UserOutlined } from '@ant-design/icons';
import type { MembershipResponse, OrganizationRole } from '@/types/organization';
import { useMembersList, useChangeRole, useRemoveMember } from '@/hooks/useMembers';
import { useOrganization } from '@/hooks/useOrganization';
import { useLanguage } from '@/hooks/useLanguage';
import ConfirmModal from '@/components/common/ConfirmModal';
import EmptyState from '@/components/common/EmptyState';
import { formatDate } from '@/utils/formatters';

const { Text } = Typography;

const ROLE_CONFIG: Record<OrganizationRole, { color: string; labelKey: string }> = {
  OWNER: { color: 'gold', labelKey: 'roleOwner' },
  ADMIN: { color: 'blue', labelKey: 'roleAdmin' },
  MEMBER: { color: 'default', labelKey: 'roleDeveloper' },
};

function getRoleOptions(t: any): { label: string; value: OrganizationRole }[] {
  return [
    { label: t.members.roleAdmin, value: 'ADMIN' },
    { label: t.members.roleDeveloper, value: 'MEMBER' },
  ];
}

// ---------------------------------------------------------------------------
// Row actions
// ---------------------------------------------------------------------------
function MemberActions({ membership }: { membership: MembershipResponse }) {
  const { activeRole } = useOrganization();
  const { t } = useLanguage();
  const changeRole = useChangeRole();
  const removeMember = useRemoveMember();

  const isOwner = membership.roleName === 'OWNER';
  const canChange = !isOwner && (activeRole === 'OWNER' || activeRole === 'ADMIN');
  const canRemove = !isOwner && (activeRole === 'OWNER' || activeRole === 'ADMIN');

  const handleRoleChange = (role: OrganizationRole) => {
    changeRole.mutate({ membershipUuid: membership.membershipUuid, role });
  };

  const handleRemove = () => {
    const displayName = membership.userName || membership.userEmail || 'this member';
    ConfirmModal.show({
      title: `${t.members.removeMember} ${displayName}?`,
      content: `Are you sure you want to remove "${displayName}" from the organization?`,
      danger: true,
      okText: t.members.removeMember,
      onOk: () => removeMember.mutate(membership.membershipUuid),
    });
  };

  return (
    <Space>
      {canChange && (
        <Select
          size="small"
          style={{ width: 120 }}
          value={membership.roleName === 'ADMIN' || membership.roleName === 'MEMBER' ? membership.roleName : undefined}
          placeholder={t.members.role}
          onChange={handleRoleChange}
          options={getRoleOptions(t)}
          loading={changeRole.isPending}
        />
      )}
      {canRemove && (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          size="small"
          title={t.members.removeMember}
          onClick={handleRemove}
          loading={removeMember.isPending}
        />
      )}
    </Space>
  );
}

// ---------------------------------------------------------------------------
// Main table component
// ---------------------------------------------------------------------------
interface MembersTableProps {
  orgUuid: string;
}

export default function MembersTable({ orgUuid }: MembersTableProps) {
  const { data: members, isLoading } = useMembersList(orgUuid);
  const { t } = useLanguage();

  const columns = [
    {
      title: t.members.title,
      key: 'user',
      render: (_: unknown, record: MembershipResponse) => (
        <Space>
          <Avatar
            size={32}
            icon={<UserOutlined />}
          />
          <div>
            <Text strong>{record.userName || '—'}</Text>
            <br />
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.userEmail || record.userUuid}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: t.members.role,
      dataIndex: 'roleName',
      key: 'roleName',
      width: 120,
      render: (roleName: string) => {
        const role = roleName as OrganizationRole;
        const cfg = ROLE_CONFIG[role] ?? { color: 'default', labelKey: 'roleDeveloper' };
        const label = (t.members as any)[cfg.labelKey] || roleName;
        return <Tag color={cfg.color}>{label}</Tag>;
      },
    },
    {
      title: t.members.joinedAt,
      dataIndex: 'joinedAt',
      key: 'joinedAt',
      width: 180,
      render: (date: string) =>
        date ? formatDate(date) : '—',
    },
    {
      title: t.common.actions,
      key: 'actions',
      width: 180,
      render: (_: unknown, record: MembershipResponse) => (
        <MemberActions membership={record} />
      ),
    },
  ];

  return (
    <Table
      columns={columns}
      dataSource={members}
      rowKey="membershipUuid"
      loading={isLoading}
      locale={{
        emptyText: <EmptyState description={t.members.noMembers} hint="Invite team members to collaborate." />,
      }}
      pagination={false}
    />
  );
}
