// ---------------------------------------------------------------------------
// useMembers — TanStack Query hooks for the Members domain
// ---------------------------------------------------------------------------

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notification } from 'antd';
import type { MembershipResponse } from '@/types/organization';
import type { InviteUserRequest } from '@/types/invitation';
import * as membershipsApi from '@/api/memberships.api';
import * as invitationsApi from '@/api/invitations.api';
import { QUERY_KEYS } from '@/utils/constants';

// ---------------------------------------------------------------------------
// List
// ---------------------------------------------------------------------------
export function useMembersList(orgUuid: string) {
  return useQuery<MembershipResponse[], Error>({
    queryKey: [QUERY_KEYS.MEMBERS, orgUuid],
    queryFn: () => membershipsApi.getMembersByOrg(orgUuid),
    staleTime: 120_000, // 2 min
    enabled: Boolean(orgUuid),
  });
}

// ---------------------------------------------------------------------------
// Change role
// ---------------------------------------------------------------------------
export function useChangeRole() {
  const queryClient = useQueryClient();

  return useMutation<MembershipResponse, Error, { membershipUuid: string; role: string }>({
    mutationFn: ({ membershipUuid, role }) =>
      membershipsApi.changeMemberRole(membershipUuid, role),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.MEMBERS] });
      notification.success({ message: 'Role updated' });
    },
    onError: (error) => {
      notification.error({
        message: 'Failed to change role',
        description: error.message,
      });
    },
  });
}

// ---------------------------------------------------------------------------
// Remove member
// ---------------------------------------------------------------------------
export function useRemoveMember() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (membershipUuid) => membershipsApi.removeMember(membershipUuid),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.MEMBERS] });
      notification.success({ message: 'Member removed' });
    },
    onError: (error) => {
      notification.error({
        message: 'Failed to remove member',
        description: error.message,
      });
    },
  });
}

// ---------------------------------------------------------------------------
// Invite member
// ---------------------------------------------------------------------------
export function useInviteMember() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: InviteUserRequest) => invitationsApi.inviteUser(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [QUERY_KEYS.MEMBERS] });
      notification.success({ message: 'Invitation sent' });
    },
    onError: (error) => {
      notification.error({
        message: 'Failed to send invitation',
        description: error.message,
      });
    },
  });
}
