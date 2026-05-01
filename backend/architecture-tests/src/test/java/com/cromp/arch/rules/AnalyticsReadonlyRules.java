package com.cromp.arch.rules;

import com.cromp.arch.util.ArchUnitImporter;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.cromp")
public class AnalyticsReadonlyRules {

    private static final String ANALYTICS = "com.cromp.analytics..";
    private static final String[] WRITABLE_DOMAINS = {
            "com.cromp.jobs..",
            "com.cromp.schedules..",
            "com.cromp.executions..",
            "com.cromp.secrets.."
    };

    // === Analytics не модифицирует другие домены (ADR-001) ===
    @ArchTest
    static final ArchRule analytics_should_not_modify_domains =
            noClasses().that().resideInAPackage(ANALYTICS)
                    .should().dependOnClassesThat().resideInAnyPackage(WRITABLE_DOMAINS)
                    .because("Analytics — read-only, не должен влиять на домены исполнения (ADR-001)")
                    .allowEmptyShould(true);
}