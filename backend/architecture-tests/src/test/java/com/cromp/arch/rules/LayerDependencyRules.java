package com.cromp.arch.rules;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.cromp")
public class LayerDependencyRules {

    private static final String COMMON       = "com.cromp.common..";
    private static final String ORCHESTRATOR = "com.cromp.orchestrator..";

    private static final String[] DOMAINS = {
            "com.cromp.iam..",
            "com.cromp.jobs..",
            "com.cromp.schedules..",
            "com.cromp.executions..",
            "com.cromp.secrets..",
            "com.cromp.analytics.."
    };

    // === Common не зависит ни от чего ===
    @ArchTest
    static final ArchRule common_should_not_depend_on_domains =
            noClasses().that().resideInAPackage(COMMON)
                    .should().dependOnClassesThat().resideInAnyPackage(DOMAINS)
                    .because("common — утилиты, не должен знать о доменах (ADR-001)")
                    .allowEmptyShould(true);

    @ArchTest
    static final ArchRule common_should_not_depend_on_orchestrator =
            noClasses().that().resideInAPackage(COMMON)
                    .should().dependOnClassesThat().resideInAPackage(ORCHESTRATOR)
                    .because("common не должен зависеть от оркестратора")
                    .allowEmptyShould(true);

    // === Домены не зависят от Orchestrator (Clean Architecture) ===
    @ArchTest
    static final ArchRule domains_should_not_depend_on_orchestrator =
            noClasses().that().resideInAnyPackage(DOMAINS)
                    .should().dependOnClassesThat().resideInAPackage(ORCHESTRATOR)
                    .because("домены не должны зависеть от Scheduler/Executor (ADR-002)")
                    .allowEmptyShould(true);
}