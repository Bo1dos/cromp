// ---------------------------------------------------------------------------
// MembersTable — Ant Design Table listing organisation members
// ---------------------------------------------------------------------------

import { Table, Space, Button, Tag, Select, Modal, Avatar, Typography } from 'antd';
import { DeleteOutlined, UserOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import type { MembershipResponse, OrganizationRole } from '@/types/organization';
import { useMembersList, useChangeRole, useRemoveMember } from '@/hooks/useMembers';
import { useOrganization } from '@/hooks/useOrganization';

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

  const isOwner = membership.role === 'OWNER';
  const canChange = !isOwner && (activeRole === 'OWNER' || activeRole === 'ADMIN');
  const canRemove = !isOwner && (activeRole === 'OWNER' || activeRole === 'ADMIN');

  const handleRoleChange = (role: OrganizationRole) => {
    changeRole.mutate({ membershipUuid: membership.uuid, role });
  };

  const handleRemove = () => {
    const displayName = membership.user?.name ?? membership.user?.email ?? 'this member';
    Modal.confirm({
      title: `Remove ${displayName}?`,
      content: `Are you sure you want to remove "${displayName}" from the organization?`,
      okText: 'Remove',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: () => removeMember.mutate(membership.uuid),
    });
  };

  return (
    <Space>
      {canChange && (
        <Select
          size="small"
          style={{ width: 100 }}
          value={membership.role === 'ADMIN' || membership.role === 'MEMBER' ? membership.role : undefined}
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
            src={record.user?.avatarUrl}
          />
          <div>
            <Text strong>{record.user?.name ?? '—'}</Text>
            <br />
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.user?.email ?? record.userUuid}
            </Text>
          </div>
        </Space>
      ),
    },
    {
      title: 'Role',
      dataIndex: 'role',
      key: 'role',
      width: 120,
      render: (role: OrganizationRole) => {
        const cfg = ROLE_CONFIG[role] ?? { color: 'default' };
        return <Tag color={cfg.color}>{role}</Tag>;
      },
    },
    {
      title: 'Joined At',
      dataIndex: 'joinedAt',
      key: 'joinedAt',
      width: 180,
      render: (date: string) =>
        date ? dayjs(date).format('MMM D, YYYY HH:mm') : '—',
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
      rowKey="uuid"
      loading={isLoading}
      pagination={false}
    />
  );
}
