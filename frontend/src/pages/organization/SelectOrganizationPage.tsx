// ---------------------------------------------------------------------------
// SelectOrganizationPage — pick or create an organisation after login
// ---------------------------------------------------------------------------

import { useEffect, useState } from 'react';
import { Button, Card, Modal, Input, Spin, Typography, Row, Col } from 'antd';
import { PlusOutlined, TeamOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

import { useAuth } from '@/hooks/useAuth';
import { useOrganizations } from '@/hooks/useOrganizations';
import { useOrganization } from '@/hooks/useOrganization';
import type { OrganizationResponse } from '@/types/organization';
import apiClient from '@/api/client';

const { Title, Text } = Typography;

export default function SelectOrganizationPage() {
  const navigate = useNavigate();
  const { user, isLoading: authLoading } = useAuth();
  const { setActiveOrganization, refreshMemberships } = useOrganization();
  const { data: memberships, isLoading: orgsLoading } = useOrganizations(user?.userUuid);

  const [modalOpen, setModalOpen] = useState(false);
  const [newOrgName, setNewOrgName] = useState('');
  const [creating, setCreating] = useState(false);

  // ---- Redirect to login if session check finished and user is null -------
  useEffect(() => {
    if (!authLoading && !user) {
      navigate('/login', { replace: true });
    }
  }, [authLoading, user, navigate]);

  // ---- Sync memberships into context --------------------------------------
  useEffect(() => {
    if (memberships) {
      refreshMemberships(memberships);
    }
  }, [memberships, refreshMemberships]);

  // ---- Auto-select when only one organisation exists ----------------------
  useEffect(() => {
    if (!memberships || memberships.length !== 1) return;

    const onlyOrg = memberships[0].organization;
    setActiveOrganization(onlyOrg).then(() => {
      navigate('/dashboard', { replace: true });
    });
  }, [memberships, setActiveOrganization, navigate]);

  // ---- Pick an organisation -----------------------------------------------
  const handleSelect = async (org: OrganizationResponse) => {
    await setActiveOrganization(org);
    navigate('/dashboard', { replace: true });
  };

  // ---- Create a new organisation ------------------------------------------
  const handleCreate = async () => {
    if (!newOrgName.trim()) return;
    setCreating(true);
    try {
      const { data } = await apiClient.post<OrganizationResponse>('/organizations', {
        name: newOrgName.trim(),
      });
      await handleSelect(data);
    } catch {
      // error is handled by the global interceptor
    } finally {
      setCreating(false);
      setModalOpen(false);
      setNewOrgName('');
    }
  };

  // ---- Loading / redirecting states --------------------------------------
  if (authLoading || orgsLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <Spin size="large" tip="Loading your organisations…" />
      </div>
    );
  }

  // Auth guard — redirect handled by useEffect above; render nothing while navigating
  if (!user) return null;

  // ---- No memberships yet → show create prompt ----------------------------
  const orgs: OrganizationResponse[] =
    memberships?.map((m) => m.organization) ?? [];

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: '#f5f5f5',
      }}
    >
      <div style={{ width: 640, padding: 32 }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 8 }}>
          Select an organisation
        </Title>
        <Text
          type="secondary"
          style={{ display: 'block', textAlign: 'center', marginBottom: 32 }}
        >
          Choose the workspace you want to work in
        </Text>

        {orgs.length === 0 ? (
          <div style={{ textAlign: 'center' }}>
            <Text type="secondary">
              You are not a member of any organisation yet.
            </Text>
            <br />
            <Button
              type="primary"
              icon={<PlusOutlined />}
              size="large"
              style={{ marginTop: 16 }}
              onClick={() => setModalOpen(true)}
            >
              Create your first organisation
            </Button>
          </div>
        ) : (
          <>
            <Row gutter={[16, 16]}>
              {orgs.map((org) => (
                <Col key={org.orgUuid} xs={24} sm={12}>
                  <Card
                    hoverable
                    onClick={() => handleSelect(org)}
                    style={{ textAlign: 'center' }}
                  >
                    <TeamOutlined style={{ fontSize: 32, marginBottom: 8, color: '#1677ff' }} />
                    <Title level={5} style={{ margin: 0 }}>
                      {org.name}
                    </Title>
                    <Text type="secondary">{org.name}</Text>
                  </Card>
                </Col>
              ))}
            </Row>

            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Button
                icon={<PlusOutlined />}
                onClick={() => setModalOpen(true)}
              >
                Create organisation
              </Button>
            </div>
          </>
        )}
      </div>

      {/* ---- Create organisation modal ------------------------------------ */}
      <Modal
        title="Create Organisation"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => {
          setModalOpen(false);
          setNewOrgName('');
        }}
        confirmLoading={creating}
        okText="Create"
      >
        <Input
          placeholder="Organisation name"
          value={newOrgName}
          onChange={(e) => setNewOrgName(e.target.value)}
          onPressEnter={handleCreate}
          size="large"
          style={{ marginTop: 8 }}
        />
      </Modal>
    </div>
  );
}
