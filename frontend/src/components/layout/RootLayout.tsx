// ---------------------------------------------------------------------------
// RootLayout — top-level provider shell for auth + organisation contexts
// ---------------------------------------------------------------------------

import { Outlet } from 'react-router-dom';

import { AuthProvider } from '@/context/AuthContext';
import { OrganizationProvider } from '@/context/OrganizationContext';

export default function RootLayout() {
  return (
    <AuthProvider>
      <OrganizationProvider>
        <Outlet />
      </OrganizationProvider>
    </AuthProvider>
  );
}
