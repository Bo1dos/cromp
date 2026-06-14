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
import { useLanguage } from '@/hooks/useLanguage';

export default function MembersPage() {
  const { activeOrganization } = useOrganization();
  const { t } = useLanguage();
  const [inviteModalOpen, setInviteModalOpen] = useState(false);

  if (!activeOrganization) {
    return <LoadingSpinner tip={t.common.loading} />;
  }

  return (
    <div>
      <PageHeader
        title={t.nav.members}
        breadcrumbs={[{ title: t.nav.dashboard }, { title: t.nav.members }]}
        extra={
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setInviteModalOpen(true)}
          >
            {t.members.inviteMember}
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
