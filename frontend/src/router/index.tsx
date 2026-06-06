import { createBrowserRouter, Navigate } from 'react-router-dom';

import RootLayout from '@/components/layout/RootLayout';
import ProtectedRoute from '@/components/common/ProtectedRoute';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import SelectOrganizationPage from '@/pages/organization/SelectOrganizationPage';
import DashboardLayout from '@/pages/dashboard/DashboardLayout';
import DashboardPage from '@/pages/dashboard/DashboardPage';
import JobListPage from '@/pages/jobs/JobListPage';
import JobCreatePage from '@/pages/jobs/JobCreatePage';
import JobDetailPage from '@/pages/jobs/JobDetailPage';
import ExecutionListPage from '@/pages/executions/ExecutionListPage';
import ExecutionDetailPage from '@/pages/executions/ExecutionDetailPage';
import SecretListPage from '@/pages/secrets/SecretListPage';
import SecretDetailPage from '@/pages/secrets/SecretDetailPage';
import AnalyticsPage from '@/pages/analytics/AnalyticsPage';
import MembersPage from '@/pages/members/MembersPage';
import AuditLogPage from '@/pages/audit/AuditLogPage';
import NotFoundPage from '@/pages/errors/NotFoundPage';
import ProfilePage from '@/pages/profile/ProfilePage';
import RoleGuard from '@/components/auth/RoleGuard';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      // ---- Root redirect -----------------------------------------------
      { index: true, element: <Navigate to="/login" replace /> },

      // ---- Public routes -------------------------------------------------
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: 'register',
        element: <RegisterPage />,
      },
      {
        path: 'select-organization',
        element: <SelectOrganizationPage />,
      },

      // ---- Protected dashboard routes ------------------------------------
      {
        element: <ProtectedRoute />,
        children: [
          {
            path: 'dashboard',
            element: <DashboardLayout />,
            children: [
              { index: true, element: <DashboardPage /> },
              { path: 'jobs', element: <JobListPage /> },
              { path: 'jobs/new', element: <JobCreatePage /> },
              { path: 'jobs/:jobUuid', element: <JobDetailPage /> },
              { path: 'executions', element: <ExecutionListPage /> },
              { path: 'executions/:executionId', element: <ExecutionDetailPage /> },
              { path: 'secrets', element: <SecretListPage /> },
              { path: 'secrets/:secretUuid', element: <SecretDetailPage /> },
              { path: 'analytics', element: <AnalyticsPage /> },
              {
                path: 'members',
                element: (
                  <RoleGuard allowedRoles={['OWNER', 'ADMIN']}>
                    <MembersPage />
                  </RoleGuard>
                ),
              },
              {
                path: 'audit',
                element: (
                  <RoleGuard allowedRoles={['ADMIN']}>
                    <AuditLogPage />
                  </RoleGuard>
                ),
              },
              {
                path: 'profile',
                element: <ProfilePage />,
              },
            ],
          },
        ],
      },

      // ---- Catch-all -----------------------------------------------------
      {
        path: '*',
        element: <NotFoundPage />,
      },
    ],
  },
]);
