import { createBrowserRouter, Navigate } from 'react-router-dom';

import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import SelectOrganizationPage from '@/pages/organization/SelectOrganizationPage';
import DashboardLayout from '@/pages/dashboard/DashboardLayout';
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

export const router = createBrowserRouter([
  // ---- Public routes -------------------------------------------------------
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    path: '/select-organization',
    element: <SelectOrganizationPage />,
  },

  // ---- Protected dashboard routes ------------------------------------------
  {
    path: '/dashboard',
    element: <DashboardLayout />,
    children: [
      { index: true, element: <Navigate to="jobs" replace /> },
      { path: 'jobs', element: <JobListPage /> },
      { path: 'jobs/new', element: <JobCreatePage /> },
      { path: 'jobs/:jobUuid', element: <JobDetailPage /> },
      { path: 'executions', element: <ExecutionListPage /> },
      { path: 'executions/:executionId', element: <ExecutionDetailPage /> },
      { path: 'secrets', element: <SecretListPage /> },
      { path: 'secrets/:secretUuid', element: <SecretDetailPage /> },
      { path: 'analytics', element: <AnalyticsPage /> },
      { path: 'members', element: <MembersPage /> },
      { path: 'audit', element: <AuditLogPage /> },
    ],
  },

  // ---- Catch-all -----------------------------------------------------------
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);
