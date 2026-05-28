package com.cromp.secrets.infrastructure.persistence;

import com.cromp.iam.infrastructure.persistence.jpa.entity.OrganizationJpaEntity;
import com.cromp.iam.infrastructure.persistence.jpa.entity.UserJpaEntity;
import com.cromp.iam.infrastructure.persistence.jpa.repository.OrganizationJpaRepository;
import com.cromp.iam.infrastructure.persistence.jpa.repository.UserJpaRepository;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretJpaEntity;
import com.cromp.secrets.infrastructure.persistence.jpa.entity.SecretVersionJpaEntity;
import com.cromp.secrets.infrastructure.persistence.jpa.repository.SecretJpaRepository;
import com.cromp.secrets.infrastructure.persistence.jpa.repository.SecretVersionJpaRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan(basePackageClasses = {
        OrganizationJpaEntity.class,
        UserJpaEntity.class,
        SecretJpaEntity.class,
        SecretVersionJpaEntity.class
})
@EnableJpaRepositories(basePackageClasses = {
        OrganizationJpaRepository.class,
        UserJpaRepository.class,
        SecretJpaRepository.class,
        SecretVersionJpaRepository.class
})
public class SecretsPersistenceTestConfiguration {
}
