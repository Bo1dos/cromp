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
      (mb) => mb.organization.uuid === activeOrganization.uuid,
    );
    return m?.role ?? null;
  }, [activeOrganization, memberships]);

  // ---- Persist active org id to localStorage ------------------------------
  useEffect(() => {
    if (activeOrganization) {
      localStorage.setItem(ORG_ID_KEY, activeOrganization.uuid);
    } else {
      localStorage.removeItem(ORG_ID_KEY);
    }
  }, [activeOrganization]);

  // ---- Public API ---------------------------------------------------------
  const setActiveOrganization = useCallback(
    async (org: OrganizationResponse) => {
      // Inform backend about the chosen organisation (updates JWT claims)
      await authApi.selectOrganization({ organizationId: org.uuid });
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
