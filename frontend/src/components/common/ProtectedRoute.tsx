// ---------------------------------------------------------------------------
// ProtectedRoute — guard for authenticated + organisation-selected routes
// ---------------------------------------------------------------------------

import { Navigate, Outlet } from 'react-router-dom';
import { Spin } from 'antd';

import { useAuth } from '@/hooks/useAuth';
import { useOrganization } from '@/hooks/useOrganization';

export default function ProtectedRoute() {
  const { isAuthenticated, isLoading } = useAuth();
  const { activeOrganization } = useOrganization();

  // ---- Still resolving session --------------------------------------------
  if (isLoading) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
        }}
      >
        <Spin size="large" />
      </div>
    );
  }

  // ---- Not authenticated --------------------------------------------------
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // ---- Authenticated but no organisation selected -------------------------
  if (!activeOrganization) {
    return <Navigate to="/select-organization" replace />;
  }

  // ---- All good — render child routes -------------------------------------
  return <Outlet />;
}
