package com.cromp.iam.infrastructure.persistence.jpa.repository;

import com.cromp.iam.infrastructure.persistence.jpa.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    @Query(value = "SELECT * FROM roles WHERE name = CAST(:name AS user_role)", nativeQuery = true)
    Optional<RoleJpaEntity> findByName(@Param("name") String name);
}
