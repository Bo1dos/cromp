// ---------------------------------------------------------------------------
// MembersPage — manage organisation members
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import LoadingSpinner from '@/components/common/LoadingSpinner';
import MembersTable from '@/components/members/MembersTable';
import InviteMemberModal from '@/components/members/InviteMemberModal';
import { useOrganization } from '@/hooks/useOrganization';

export default function MembersPage() {
  const { activeOrganization } = useOrganization();
  const [inviteModalOpen, setInviteModalOpen] = useState(false);

  if (!activeOrganization) {
    return <LoadingSpinner tip="Loading organization…" />;
  }

  return (
    <div>
      <PageHeader
        title="Members"
        breadcrumbs={[{ title: 'Dashboard' }, { title: 'Members' }]}
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setInviteModalOpen(true)}
          >
            Invite Member
          </Button>
        }
      />

      <MembersTable orgUuid={activeOrganization.orgUuid} />

      <InviteMemberModal
        open={inviteModalOpen}
        onClose={() => setInviteModalOpen(false)}
      />
    </div>
  );
}
