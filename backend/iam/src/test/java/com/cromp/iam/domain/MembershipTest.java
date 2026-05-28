package com.cromp.iam.domain;

import com.cromp.iam.domain.model.Membership;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MembershipTest {

    @Test
    void shouldJoinWithGeneratedUuidWhenValidIdsProvided() {
        Membership membership = Membership.join(10L, 20L, 30L);

        assertThat(membership.getMembershipUuid()).isNotNull();
        assertThat(membership.getUserId()).isEqualTo(10L);
        assertThat(membership.getOrganizationId()).isEqualTo(20L);
        assertThat(membership.getRoleId()).isEqualTo(30L);
    }

    @Test
    void shouldChangeRoleAndTouchUpdatedAtWhenCalled() {
        Instant before = Instant.parse("2024-01-01T00:00:00Z");
        Membership membership = Membership.reconstitute(
                1L,
                null,
                10L,
                20L,
                30L,
                before,
                before,
                null
        );

        membership.changeRole(40L);

        assertThat(membership.getRoleId()).isEqualTo(40L);
        assertThat(membership.getUpdatedAt()).isAfter(before);
    }

    @Test
    void shouldRejectNullIdentifiers() {
        assertThatThrownBy(() -> Membership.join(null, 20L, 30L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("userId");

        Membership membership = Membership.join(10L, 20L, 30L);

        assertThatThrownBy(() -> membership.changeRole(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("roleId");
    }
}
