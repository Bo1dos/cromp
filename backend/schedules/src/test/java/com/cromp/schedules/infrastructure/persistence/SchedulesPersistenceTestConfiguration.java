package com.cromp.schedules.infrastructure.persistence;

import com.cromp.schedules.infrastructure.persistence.jpa.entity.ScheduleJpaEntity;
import com.cromp.schedules.infrastructure.persistence.jpa.repository.ScheduleJpaRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan(basePackageClasses = ScheduleJpaEntity.class)
@EnableJpaRepositories(basePackageClasses = ScheduleJpaRepository.class)
public class SchedulesPersistenceTestConfiguration {
}
