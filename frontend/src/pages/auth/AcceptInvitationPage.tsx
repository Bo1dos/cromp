import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Result, Button, Spin, Typography } from 'antd';
import { CheckCircleOutlined } from '@ant-design/icons';
import apiClient from '@/api/client';

const { Text } = Typography;

export default function AcceptInvitationPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const [state, setState] = useState<'loading' | 'success' | 'error'>('loading');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (!token) {
      setState('error');
      setErrorMsg('No invitation token found in the link.');
      return;
    }

    apiClient.post('/invitations/accept', { token })
      .then(() => setState('success'))
      .catch((err) => {
        setState('error');
        setErrorMsg(err.response?.data?.message || err.message || 'Failed to accept invitation');
      });
  }, [token]);

  if (state === 'loading') {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin size="large" tip="Accepting invitation…">
          <div style={{ padding: 50 }} />
        </Spin>
      </div>
    );
  }

  if (state === 'success') {
    return (
      <Result
        status="success"
        icon={<CheckCircleOutlined />}
        title="Invitation Accepted!"
        subTitle="You have successfully joined the organization."
        extra={[
          <Button type="primary" key="dashboard" onClick={() => navigate('/dashboard')}>
            Go to Dashboard
          </Button>,
          <Button key="login" onClick={() => navigate('/login')}>
            Go to Login
          </Button>,
        ]}
      />
    );
  }

  return (
    <Result
      status="error"
      title="Failed to Accept Invitation"
      subTitle={<Text type="danger">{errorMsg}</Text>}
      extra={
        <Button type="primary" onClick={() => navigate('/login')}>
          Go to Login
        </Button>
      }
    />
  );
}
