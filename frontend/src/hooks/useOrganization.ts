// ---------------------------------------------------------------------------
// useOrganization — convenience hook for OrganizationContext consumption
// ---------------------------------------------------------------------------

import { useContext } from 'react';
import {
  OrganizationContext,
  type OrganizationContextValue,
} from '@/context/OrganizationContext';

export function useOrganization(): OrganizationContextValue {
  const ctx = useContext(OrganizationContext);
  if (ctx === undefined) {
    throw new Error(
      'useOrganization must be used within an <OrganizationProvider>',
    );
  }
  return ctx;
}
