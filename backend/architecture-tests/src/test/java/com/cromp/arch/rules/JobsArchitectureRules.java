package com.cromp.arch.rules;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.cromp.jobs")
public class JobsArchitectureRules {

    private static final String DOMAIN = "com.cromp.jobs.domain..";
    private static final String APPLICATION = "com.cromp.jobs.application..";
    private static final String API = "com.cromp.jobs.api..";
    private static final String INFRASTRUCTURE = "com.cromp.jobs.infrastructure..";
    private static final String WEB = "com.cromp.jobs.infrastructure.web..";
    private static final String PERSISTENCE = "com.cromp.jobs.infrastructure.persistence..";

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring_or_infrastructure =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", INFRASTRUCTURE)
                    .because("domain must stay framework-free")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule application_should_not_depend_on_web =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat().resideInAPackage(WEB)
                    .because("application should be driven by ports, not controllers")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule api_should_not_depend_on_persistence =
            noClasses().that().resideInAPackage(API)
                    .should().dependOnClassesThat().resideInAPackage(PERSISTENCE)
                    .because("api should stay persistence-agnostic")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_directly =
            noClasses().that().resideInAPackage(WEB)
                    .should().dependOnClassesThat().resideInAPackage("com.cromp.jobs.infrastructure.persistence.jpa.repository..")
                    .because("controllers must go through facades/services")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule jobs_should_be_free_of_cycles =
            slices().matching("com.cromp.jobs.(*)..")
                    .should().beFreeOfCycles();
}
