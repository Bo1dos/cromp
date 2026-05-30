package com.cromp.arch.rules;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.cromp")
public class ExecutionsArchitectureRules {

    private static final String DOMAIN = "com.cromp.executions.domain..";
    private static final String APPLICATION = "com.cromp.executions.application..";
    private static final String API = "com.cromp.executions.api..";
    private static final String INFRASTRUCTURE = "com.cromp.executions.infrastructure..";
    private static final String WEB = "com.cromp.executions.infrastructure.web..";
    private static final String PERSISTENCE = "com.cromp.executions.infrastructure.persistence..";

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring_or_infrastructure =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", INFRASTRUCTURE)
                    .because("executions domain must stay framework-free")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_should_not_depend_on_web =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAPackage(WEB)
                    .because("application must be driven by ports, not controllers")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule api_should_not_depend_on_persistence =
            noClasses().that().resideInAPackage(API)
                    .should().dependOnClassesThat().resideInAPackage(PERSISTENCE)
                    .because("api should remain persistence-agnostic")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_directly =
            noClasses().that().resideInAPackage(WEB)
                    .should().dependOnClassesThat().resideInAPackage("com.cromp.executions.infrastructure.persistence.jpa.repository")
                    .because("controllers must go through application services")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule repository_adapters_should_live_in_the_adapter_package =
            noClasses().that().haveSimpleNameEndingWith("RepositoryAdapter")
                    .should().resideOutsideOfPackage("com.cromp.executions.infrastructure.persistence.adapter..")
                    .because("repository adapters belong in persistence.adapter")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule jpa_entities_should_live_in_the_entity_package =
            noClasses().that().haveSimpleNameEndingWith("JpaEntity")
                    .should().resideOutsideOfPackage("com.cromp.executions.infrastructure.persistence.jpa.entity..")
                    .because("JPA entities belong in persistence.jpa.entity")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule custom_repositories_should_live_in_the_custom_package =
            noClasses().that().haveSimpleNameEndingWith("CustomRepository")
                    .should().resideOutsideOfPackage("com.cromp.executions.infrastructure.persistence.custom..")
                    .because("custom repository implementations belong in persistence.custom")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule port_impls_should_live_in_infrastructure =
            noClasses().that().haveSimpleNameEndingWith("PortImpl")
                    .should().resideOutsideOfPackage(INFRASTRUCTURE)
                    .because("port implementations belong in infrastructure")
                    .allowEmptyShould(true);
}
