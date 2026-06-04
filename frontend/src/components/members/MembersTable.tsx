// ---------------------------------------------------------------------------
// MembersTable — Ant Design Table listing organisation members
// ---------------------------------------------------------------------------

import { Table, Space, Button, Tag, Select, Avatar, Typography } from 'antd';
import { DeleteOutlined, UserOutlined } from '@ant-design/icons';
import type { MembershipResponse, OrganizationRole } from '@/types/organization';
import { useMembersList, useChangeRole, useRemoveMember } from '@/hooks/useMembers';
import { useOrganization } from '@/hooks/useOrganization';
import ConfirmModal from '@/components/common/ConfirmModal';
import EmptyState from '@/components/common/EmptyState';
import { formatDate } from '@/utils/formatters';

const { Text } = Typography;

const ROLE_CONFIG: Record<OrganizationRole, { color: string }> = {
  OWNER: { color: 'gold' },
  ADMIN: { color: 'blue' },
  MEMBER: { color: 'default' },
};

const ROLE_OPTIONS: { label: string; value: OrganizationRole }[] = [
  { label: 'Admin', value: 'ADMIN' },
  { label: 'Member', value: 'MEMBER' },
];

// ---------------------------------------------------------------------------
// Row actions
// ---------------------------------------------------------------------------
function MemberActions({ membership }: { membership: MembershipResponse }) {
  const { activeRole } = useOrganization();
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
      title: `Remove ${displayName}?`,
      content: `Are you sure you want to remove "${displayName}" from the organization?`,
      danger: true,
      okText: 'Remove',
      onOk: () => removeMember.mutate(membership.membershipUuid),
    });
  };

  return (
    <Space>
      {canChange && (
        <Select
          size="small"
          style={{ width: 100 }}
          value={membership.roleName === 'ADMIN' || membership.roleName === 'MEMBER' ? membership.roleName : undefined}
          placeholder="Role"
          onChange={handleRoleChange}
          options={ROLE_OPTIONS}
          loading={changeRole.isPending}
        />
      )}
      {canRemove && (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          size="small"
          title="Remove member"
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

  const columns = [
    {
      title: 'User',
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
      title: 'Role',
      dataIndex: 'roleName',
      key: 'roleName',
      width: 120,
      render: (roleName: string) => {
        const role = roleName as OrganizationRole;
        const cfg = ROLE_CONFIG[role] ?? { color: 'default' };
        return <Tag color={cfg.color}>{roleName}</Tag>;
      },
    },
    {
      title: 'Joined At',
      dataIndex: 'joinedAt',
      key: 'joinedAt',
      width: 180,
      render: (date: string) =>
        date ? formatDate(date) : '—',
    },
    {
      title: 'Actions',
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
        emptyText: <EmptyState description="No members" hint="Invite team members to collaborate." />,
      }}
      pagination={false}
    />
  );
}
