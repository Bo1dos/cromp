import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider, App as AntdApp } from 'antd';

import { router } from '@/router';
import ErrorBoundary from '@/components/common/ErrorBoundary';
import '@/styles/global.css';

// ---------------------------------------------------------------------------
// TanStack Query client
// ---------------------------------------------------------------------------
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,          // 30 s
      retry: 2,
      refetchOnWindowFocus: true,
    },
  },
});

// ---------------------------------------------------------------------------
// Ant Design theme token overrides
// ---------------------------------------------------------------------------
const themeConfig = {
  token: {
    colorPrimary: '#1677ff',
    borderRadius: 6,
  },
};

// ---------------------------------------------------------------------------
// Mount
// ---------------------------------------------------------------------------
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ConfigProvider theme={themeConfig}>
          <AntdApp>
            <RouterProvider router={router} />
          </AntdApp>
        </ConfigProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  </StrictMode>,
);
