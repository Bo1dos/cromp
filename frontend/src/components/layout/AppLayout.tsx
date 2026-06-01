// ---------------------------------------------------------------------------
// AppLayout — main application shell with responsive Sider/Drawer + Content
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Layout, Drawer, Button } from 'antd';
import { MenuOutlined } from '@ant-design/icons';

import AppSider from './AppSider';
import AppHeader from './AppHeader';
import { useBreakpoint } from '@/hooks/useBreakpoint';

const { Content } = Layout;

export default function AppLayout() {
  const { isMobile } = useBreakpoint();
  const [drawerOpen, setDrawerOpen] = useState(false);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {isMobile ? (
        <>
          {/* Mobile: Sider as Drawer */}
          <Layout>
            <AppHeader
              extraLeft={
                <Button
                  type="text"
                  icon={<MenuOutlined />}
                  onClick={() => setDrawerOpen(true)}
                />
              }
            />
            <Content
              style={{
                padding: 16,
                minHeight: 'calc(100vh - 64px)',
                background: '#f5f5f5',
              }}
            >
              <Outlet />
            </Content>
          </Layout>
          <Drawer
            placement="left"
            open={drawerOpen}
            onClose={() => setDrawerOpen(false)}
            styles={{ body: { padding: 0, background: '#001529' } }}
            width={240}
          >
            <AppSider isDrawer onNavigate={() => setDrawerOpen(false)} />
          </Drawer>
        </>
      ) : (
        <>
          {/* Desktop: Fixed Sider */}
          <AppSider />
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
        </>
      )}
    </Layout>
  );
}
