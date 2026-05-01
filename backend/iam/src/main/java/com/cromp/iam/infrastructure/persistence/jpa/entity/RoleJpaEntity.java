package com.cromp.iam.infrastructure.persistence.jpa.entity;

import com.cromp.iam.domain.model.enums.UserRole;
import com.cromp.iam.domain.model.support.SchemaLimits;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, columnDefinition = "user_role")
    private UserRole name;


    @Column(name = "description", length = SchemaLimits.ROLE_DESCRIPTION_MAX_LENGTH)
    private String description;
}