// ---------------------------------------------------------------------------
// RoleGuard — conditionally renders children based on the active role
// ---------------------------------------------------------------------------

import type { ReactNode } from 'react';
import { Result } from 'antd';

import { useOrganization } from '@/hooks/useOrganization';
import { hasRole } from '@/utils/permissions';
import type { OrganizationRole } from '@/types/organization';

interface RoleGuardProps {
  children: ReactNode;
  allowedRoles: OrganizationRole[];
}

export default function RoleGuard({ children, allowedRoles }: RoleGuardProps) {
  const { activeRole } = useOrganization();

  if (!hasRole(activeRole, allowedRoles)) {
    return (
      <Result
        status="403"
        title="Доступ запрещён"
        subTitle="У вас недостаточно прав для просмотра этой страницы."
      />
    );
  }

  return <>{children}</>;
}
