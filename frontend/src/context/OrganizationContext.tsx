// ---------------------------------------------------------------------------
// OrganizationContext — active organisation & memberships state
// ---------------------------------------------------------------------------

import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { useQueryClient } from '@tanstack/react-query';

import * as authApi from '@/api/auth.api';
import { setAccessToken, setActiveOrgId } from '@/api/client';
import type {
  MembershipResponse,
  OrganizationResponse,
  OrganizationRole,
} from '@/types/organization';

// ---------------------------------------------------------------------------
// Storage keys
// ---------------------------------------------------------------------------
const ORG_ID_KEY = 'X-Organization-ID';

// ---------------------------------------------------------------------------
// Context shape
// ---------------------------------------------------------------------------
export interface OrganizationContextValue {
  activeOrganization: OrganizationResponse | null;
  memberships: MembershipResponse[];
  activeRole: OrganizationRole | null;
  setActiveOrganization: (org: OrganizationResponse) => Promise<void>;
  refreshMemberships: (memberships: MembershipResponse[]) => void;
}

export const OrganizationContext = createContext<OrganizationContextValue | undefined>(
  undefined,
);

// ---------------------------------------------------------------------------
// Provider
// ---------------------------------------------------------------------------
export function OrganizationProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();

  const [activeOrganization, setActiveOrganizationState] =
    useState<OrganizationResponse | null>(null);
  const [memberships, setMemberships] = useState<MembershipResponse[]>([]);

  // ---- Derived role -------------------------------------------------------
  const activeRole = useMemo<OrganizationRole | null>(() => {
    if (!activeOrganization) return null;
    const m = memberships.find(
      (mb) => mb.organization.orgUuid === activeOrganization.orgUuid,
    );
    return (m?.roleName as OrganizationRole) ?? null;
  }, [activeOrganization, memberships]);

  // ---- Persist active org id to localStorage + interceptor ----------------
  useEffect(() => {
    if (activeOrganization) {
      localStorage.setItem(ORG_ID_KEY, activeOrganization.orgUuid);
      setActiveOrgId(activeOrganization.orgUuid);
    } else {
      localStorage.removeItem(ORG_ID_KEY);
      setActiveOrgId(null);
    }
  }, [activeOrganization]);

  // ---- Public API ---------------------------------------------------------
  const setActiveOrganization = useCallback(
    async (org: OrganizationResponse) => {
      // Sync interceptor immediately (before useEffect fires)
      setActiveOrgId(org.orgUuid);

      // Inform backend about the chosen organisation (updates JWT claims)
      const response = await authApi.selectOrganization({ organizationId: String(org.id) });
      // Store the new token (now with org-scoped permissions)
      setAccessToken(response.accessToken);
      setActiveOrganizationState(org);
      // Invalidate cached data that depends on the organisation context
      queryClient.invalidateQueries({ queryKey: ['organizations'] });
    },
    [queryClient],
  );

  const refreshMemberships = useCallback((list: MembershipResponse[]) => {
    setMemberships(list);
  }, []);

  // ---- Memoised context value ---------------------------------------------
  const value = useMemo<OrganizationContextValue>(
    () => ({
      activeOrganization,
      memberships,
      activeRole,
      setActiveOrganization,
      refreshMemberships,
    }),
    [activeOrganization, memberships, activeRole, setActiveOrganization, refreshMemberships],
  );

  return (
    <OrganizationContext.Provider value={value}>
      {children}
    </OrganizationContext.Provider>
  );
}
