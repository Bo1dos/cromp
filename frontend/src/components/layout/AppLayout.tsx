// ---------------------------------------------------------------------------
// AppLayout — main application shell with responsive Sider/Drawer + Content
// ---------------------------------------------------------------------------

import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Layout, Drawer, Button, theme } from 'antd';
import { MenuOutlined } from '@ant-design/icons';

import AppSider from './AppSider';
import AppHeader from './AppHeader';
import { useBreakpoint } from '@/hooks/useBreakpoint';

const { Content } = Layout;

export default function AppLayout() {
  const { isMobile } = useBreakpoint();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const { token } = theme.useToken();

  const contentStyle = {
    padding: isMobile ? 16 : 24,
    minHeight: 'calc(100vh - 64px)',
    background: token.colorBgLayout,
  };

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
            <Content style={contentStyle}>
              <Outlet />
            </Content>
          </Layout>
          <Drawer
            placement="left"
            open={drawerOpen}
            onClose={() => setDrawerOpen(false)}
            styles={{ body: { padding: 0, background: token.colorBgElevated } }}
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
            <Content style={contentStyle}>
              <Outlet />
            </Content>
          </Layout>
        </>
      )}
    </Layout>
  );
}
