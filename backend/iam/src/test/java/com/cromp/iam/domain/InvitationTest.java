package com.cromp.iam.domain;

import com.cromp.iam.domain.model.Invitation;
import com.cromp.iam.domain.model.enums.InvitationStatus;
import com.cromp.iam.domain.model.exceptions.DomainException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvitationTest {

    @Test
    void shouldCreatePendingInvitationAndNormalizeEmail() {
        Invitation invitation = Invitation.create(
                1L,
                " USER@Example.COM ",
                "hash-123",
                2L,
                3L,
                Instant.now().plusSeconds(3600)
        );

        assertThat(invitation.getInvitationUuid()).isNotNull();
        assertThat(invitation.getEmail()).isEqualTo("user@example.com");
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitation.getAcceptedAt()).isNull();
    }

    @Test
    void shouldAcceptPendingInvitationBeforeExpiration() {
        Invitation invitation = Invitation.create(
                1L,
                "user@example.com",
                "hash-123",
                2L,
                3L,
                Instant.now().plusSeconds(3600)
        );

        invitation.accept();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.getAcceptedAt()).isNotNull();
    }

    @Test
    void shouldExpireAndThrowWhenAcceptingExpiredInvitation() {
        Invitation invitation = Invitation.create(
                1L,
                "user@example.com",
                "hash-123",
                2L,
                3L,
                Instant.now().minusSeconds(10)
        );

        assertThatThrownBy(invitation::accept)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("expired");

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
    }

    @Test
    void shouldRejectAcceptWhenInvitationIsNotPending() {
        Invitation invitation = Invitation.reconstitute(
                1L,
                null,
                1L,
                "user@example.com",
                "hash-123",
                2L,
                3L,
                Instant.now().plusSeconds(3600),
                Instant.now(),
                Instant.now(),
                InvitationStatus.ACCEPTED
        );

        assertThatThrownBy(invitation::accept)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Only pending invitation");
    }

    @Test
    void shouldRevokeOrExpirePendingInvitationButProtectAcceptedOne() {
        Invitation revoked = Invitation.create(
                1L,
                "user@example.com",
                "hash-123",
                2L,
                3L,
                Instant.now().plusSeconds(3600)
        );
        revoked.revoke();
        assertThat(revoked.getStatus()).isEqualTo(InvitationStatus.REVOKED);

        Invitation expired = Invitation.create(
                1L,
                "user@example.com",
                "hash-456",
                2L,
                3L,
                Instant.now().plusSeconds(3600)
        );
        expired.expire();
        assertThat(expired.getStatus()).isEqualTo(InvitationStatus.EXPIRED);

        Invitation accepted = Invitation.reconstitute(
                2L,
                null,
                1L,
                "user@example.com",
                "hash-789",
                2L,
                3L,
                Instant.now().plusSeconds(3600),
                Instant.now(),
                Instant.now(),
                InvitationStatus.ACCEPTED
        );

        assertThatThrownBy(accepted::revoke)
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Accepted invitation");
    }
}
