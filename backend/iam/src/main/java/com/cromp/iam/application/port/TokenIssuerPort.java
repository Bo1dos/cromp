package com.cromp.iam.application.port;

import com.cromp.iam.domain.model.User;
import com.cromp.iam.domain.model.Organization;

import java.util.Collection;

public interface TokenIssuerPort {
    String issueToken(User user, Organization activeOrganization, Collection<String> permissions);
}