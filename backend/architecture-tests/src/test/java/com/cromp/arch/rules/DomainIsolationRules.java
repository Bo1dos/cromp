package com.cromp.arch.rules;

import com.cromp.arch.util.ArchUnitImporter;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.cromp")
public class DomainIsolationRules {

    private static final JavaClasses ALL_CLASSES = ArchUnitImporter.importAllProductionClasses();

    private static final String JOBS = "com.cromp.jobs..";
    private static final String SCHEDULES = "com.cromp.schedules..";
    private static final String EXECUTIONS = "com.cromp.executions..";
    private static final String SECRETS = "com.cromp.secrets..";
    private static final String ANALYTICS = "com.cromp.analytics..";
    private static final String IAM = "com.cromp.iam..";
    private static final String API = "com.cromp.api.."; 

    // === Jobs не зависит от Executions (ADR-002: Job — декларация, не исполнитель) ===
    @ArchTest
    static final ArchRule jobs_should_not_depend_on_executions =
            noClasses().that().resideInAPackage(JOBS)
                    .should().dependOnClassesThat().resideInAPackage(EXECUTIONS)
                    .because("Jobs — декларация намерений, не должен знать о фактах выполнения (ADR-002)")
                    .allowEmptyShould(true);

    // === Schedules не создаёт Executions (ADR-002: Schedule — календарь, не триггер) ===
    @ArchTest
    static final ArchRule schedules_should_not_depend_on_executions =
            noClasses().that().resideInAPackage(SCHEDULES)
                    .should().dependOnClassesThat().resideInAPackage(EXECUTIONS)
                    .because("Schedules — декларативный источник временных правил, не инициирует выполнение (ADR-002)")
                    .allowEmptyShould(true);

    
    // === Только Executor может зависеть от execution_attempts (ADR-004) ===
    @ArchTest
    static final ArchRule only_executor_can_depend_on_execution_attempts =
        noClasses().that().resideInAnyPackage(JOBS, SCHEDULES, SECRETS, ANALYTICS)
            .should().dependOnClassesThat().resideInAPackage("com.cromp.orchestrator.executor..")
            .because("Только Executor работает с ExecutionAttempts (ADR-004)")
            .allowEmptyShould(true);

    // === Secrets не зависит от бизнес-доменов (ADR-007) ===
    @ArchTest
    static final ArchRule secrets_should_not_depend_on_domains =
            noClasses().that().resideInAPackage(SECRETS)
                    .should().dependOnClassesThat().resideInAnyPackage(JOBS, SCHEDULES, EXECUTIONS)
                    .because("Secrets — изолированный домен безопасности (ADR-007)")
                    .allowEmptyShould(true);

    // === Secrets не возвращается через API (ADR-007) ===
    @ArchTest
    static final ArchRule secrets_value_never_exposed_in_api =
        noClasses().that().resideInAPackage(API)
            .should().dependOnClassesThat().resideInAPackage("com.cromp.secrets.internal..")
            .because("Secrets values никогда не возвращаются через API (ADR-007)")
            .allowEmptyShould(true);


    // === IAM: домены не тянут таблицы IAM напрямую (ADR-006) ===
    @ArchTest
    static final ArchRule domains_should_not_depend_on_iam_internal =
            noClasses().that().resideInAnyPackage(JOBS, SCHEDULES, EXECUTIONS, SECRETS)
                    .should().dependOnClassesThat().resideInAPackage("com.cromp.iam.internal..")
                    .because("домены не должны обращаться к внутренним классам IAM напрямую (ADR-006)")
                    .allowEmptyShould(true);
}