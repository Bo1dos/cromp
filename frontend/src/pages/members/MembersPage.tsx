// ---------------------------------------------------------------------------
// MembersPage — organisation members management
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Button } from 'antd';
import { UserAddOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import MembersTable from '@/components/members/MembersTable';
import InviteMemberModal from '@/components/members/InviteMemberModal';
import { useOrganization } from '@/hooks/useOrganization';

export default function MembersPage() {
  const { activeOrganization } = useOrganization();
  const [inviteModalOpen, setInviteModalOpen] = useState(false);

  return (
    <div>
      <PageHeader
        title="Members"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Members' }]}
        extra={
          <Button
            type="primary"
            icon={<UserAddOutlined />}
            onClick={() => setInviteModalOpen(true)}
          >
            Invite Member
          </Button>
        }
      />

      <MembersTable orgUuid={activeOrganization?.uuid ?? ''} />

      <InviteMemberModal
        open={inviteModalOpen}
        onClose={() => setInviteModalOpen(false)}
      />
    </div>
  );
}
