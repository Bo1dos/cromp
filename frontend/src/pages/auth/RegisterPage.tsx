// ---------------------------------------------------------------------------
// RegisterPage — new user registration
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { Button, Form, Input, Typography } from 'antd';
import { MailOutlined, LockOutlined, UserOutlined } from '@ant-design/icons';

import { useAuth } from '@/hooks/useAuth';

import type { RegisterRequest } from '@/types/auth';

const { Title, Text } = Typography;

interface RegisterFormValues {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export default function RegisterPage() {
  const { register, isAuthenticated, isLoading } = useAuth();
  const [form] = Form.useForm<RegisterFormValues>();
  const [submitting, setSubmitting] = useState(false);

  // Already authenticated — skip to organisation selection
  if (!isLoading && isAuthenticated) {
    return <Navigate to="/select-organization" replace />;
  }

  const handleFinish = async (values: RegisterFormValues) => {
    setSubmitting(true);
    try {
      const req: RegisterRequest = {
        email: values.email,
        password: values.password,
        displayName: [values.firstName, values.lastName].filter(Boolean).join(' ') || values.email.split('@')[0],
        firstName: values.firstName || undefined,
        lastName: values.lastName || undefined,
      };
      await register(req);
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
        background: '#f5f5f5',
      }}
    >
      <div style={{ width: 420, padding: 32, background: '#fff', borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.09)' }}>
        <Title level={3} style={{ textAlign: 'center', marginBottom: 24 }}>
          Create your account
        </Title>

        <Form<RegisterFormValues>
          form={form}
          layout="vertical"
          onFinish={handleFinish}
          autoComplete="off"
        >
          <Form.Item
            name="firstName"
            label="First Name"
            rules={[{ required: true, message: 'Please enter your first name' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="John" size="large" />
          </Form.Item>

          <Form.Item
            name="lastName"
            label="Last Name"
          >
            <Input prefix={<UserOutlined />} placeholder="Doe" size="large" />
          </Form.Item>

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
              { required: true, message: 'Please enter a password' },
              { min: 8, message: 'Password must be at least 8 characters' },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••" size="large" />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="Confirm Password"
            dependencies={['password']}
            rules={[
              { required: true, message: 'Please confirm your password' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('Passwords do not match'));
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••" size="large" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 12 }}>
            <Button type="primary" htmlType="submit" block size="large" loading={submitting}>
              Create Account
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center' }}>
          <Text>
            Already have an account?{' '}
            <Link to="/login">Sign in</Link>
          </Text>
        </div>
      </div>
    </div>
  );
}
