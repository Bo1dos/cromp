import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App as AntdApp } from 'antd';

import { router } from '@/router';
import ErrorBoundary from '@/components/common/ErrorBoundary';
import { ThemeProvider } from '@/context/ThemeContext';
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
// Mount
// ---------------------------------------------------------------------------
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider>
          <AntdApp>
            <RouterProvider router={router} />
          </AntdApp>
        </ThemeProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  </StrictMode>,
);
