// ---------------------------------------------------------------------------
// LoginPage — email / password authentication
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { Button, Form, Input, Typography } from 'antd';
import { MailOutlined, LockOutlined } from '@ant-design/icons';

import { useAuth } from '@/hooks/useAuth';

const { Title, Text } = Typography;

interface LoginFormValues {
  email: string;
  password: string;
}

export default function LoginPage() {
  const { login, isAuthenticated, isLoading } = useAuth();
  const [form] = Form.useForm<LoginFormValues>();
  const [submitting, setSubmitting] = useState(false);

  // Already authenticated — skip to organisation selection
  if (!isLoading && isAuthenticated) {
    return <Navigate to="/select-organization" replace />;
  }

  const handleFinish = async (values: LoginFormValues) => {
    setSubmitting(true);
    try {
      await login(values.email, values.password);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--color-bg-layout, #f5f5f5)',
      }}
    >
      <div style={{ width: 400, padding: 32, background: 'var(--color-bg-base, #fff)', borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.09)' }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 24 }}>
          Sign in to CaaS
        </Title>

        <Form<LoginFormValues>
          form={form}
          layout="vertical"
          onFinish={handleFinish}
          autoComplete="off"
        >
          <Form.Item
            name="email"
            label="Email"
            rules={[
              { required: true, message: 'Please enter your email' },
              { type: 'email', message: 'Please enter a valid email address' },
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="you@example.com" size="large" />
          </Form.Item>

          <Form.Item
            name="password"
            label="Password"
            rules={[
              { required: true, message: 'Please enter your password' },
              { min: 8, message: 'Password must be at least 8 characters' },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••" size="large" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 12 }}>
            <Button type="primary" htmlType="submit" block size="large" loading={submitting}>
              Sign In
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center' }}>
          <Text>
            Don&apos;t have an account?{' '}
            <Link to="/register">Register now</Link>
          </Text>
        </div>
      </div>
    </div>
  );
}
