// ---------------------------------------------------------------------------
// AppLayout — main application shell with Sider, Header, and Content area
// ---------------------------------------------------------------------------

import { Outlet } from 'react-router-dom';
import { Layout } from 'antd';

import AppSider from './AppSider';
import AppHeader from './AppHeader';

const { Content } = Layout;

export default function AppLayout() {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* Fixed Sider */}
      <AppSider />

      {/* Main area — offset by sider width */}
      <Layout style={{ marginLeft: 240 }}>
        <AppHeader />

        <Content
          style={{
            padding: 24,
            minHeight: 'calc(100vh - 64px)',
            background: '#f5f5f5',
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
