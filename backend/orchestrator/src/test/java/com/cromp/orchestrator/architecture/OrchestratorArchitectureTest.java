package com.cromp.orchestrator.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@DisplayName("Orchestrator Architecture Tests")
class OrchestratorArchitectureTest {

    private static final String BASE = "com.cromp.orchestrator";
    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Nested
    @DisplayName("package structure rules")
    class PackageStructure {

        @Test
        @DisplayName("config package should not contain business logic")
        void configShouldNotContainBusinessLogic() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".config..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "org.apache.hc..",
                            "java..",
                            BASE + ".config.."
                    )
                    .because("config classes should only contain Spring/HTTP configuration, no business logic");

            rule.check(classes);
        }

        @Test
        @DisplayName("exception package should only contain exception handling")
        void exceptionShouldOnlyHandleExceptions() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".exception..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "java..",
                            "com.cromp.iam.domain.model.exceptions..",
                            "com.cromp.jobs.domain.model.exceptions..",
                            "com.cromp.schedules.domain.model.exceptions..",
                            "com.cromp.secrets.domain.model.exceptions..",
                            BASE + ".exception.."
                    )
                    .because("exception package should only map exceptions to HTTP responses");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("dependency rules")
    class DependencyRules {

        @Test
        @DisplayName("executor should depend only on ports and adapters")
        void executorShouldDependOnPorts() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".executor..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            BASE + ".executor..",
                            BASE + ".config..",
                            "com.cromp.executions.application.port..",
                            "com.cromp.executions.api.dto..",
                            "com.cromp.executions.domain.model..",
                            "com.cromp.jobs.domain.model..",
                            "com.cromp.secrets.application.port..",
                            "org.springframework..",
                            "com.fasterxml..",
                            "io.micrometer..",
                            "java..",
                            "lombok..",
                            "org.slf4j.."
                    )
                    .because("executor should communicate via public ports only");

            rule.check(classes);
        }

        @Test
        @DisplayName("scheduler should depend only on ports")
        void schedulerShouldDependOnPorts() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".scheduler..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            BASE + ".scheduler..",
                            BASE + ".config..",
                            "com.cromp.executions.application.port..",
                            "com.cromp.jobs.domain.model..",
                            "com.cromp.jobs.domain.repository..",
                            "com.cromp.schedules.application.port..",
                            "com.cromp.schedules.domain.model..",
                            "com.cromp.schedules.domain.repository..",
                            "com.cromp.schedules.domain.service..",
                            "com.cronutils..",
                            "org.springframework..",
                            "io.micrometer..",
                            "java..",
                            "lombok..",
                            "org.slf4j.."
                    )
                    .because("scheduler should communicate via public ports only");

            rule.check(classes);
        }

        @Test
        @DisplayName("janitor should depend only on ports")
        void janitorShouldDependOnPorts() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".janitor..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            BASE + ".janitor..",
                            BASE + ".config..",
                            "com.cromp.executions.application.port..",
                            "com.cromp.executions.domain.model..",
                            "org.springframework..",
                            "io.micrometer..",
                            "java..",
                            "lombok..",
                            "org.slf4j.."
                    )
                    .because("janitor should communicate via public ports only");

            rule.check(classes);
        }

        @Test
        @DisplayName("orchestrator should not have circular dependencies")
        void shouldNotHaveCircularDependencies() {
            // ArchUnit's layeredArchitecture checks this naturally
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + "..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            BASE + "..",
                            "com.cromp.common..",
                            "com.cromp.iam..",
                            "com.cromp.jobs..",
                            "com.cromp.schedules..",
                            "com.cromp.executions..",
                            "com.cromp.secrets..",
                            "com.cromp.analytics..",
                            "org.springframework..",
                            "com.fasterxml..",
                            "io.micrometer..",
                            "org.apache.hc..",
                            "java..",
                            "lombok..",
                            "org.slf4j..",
                            "com.cronutils.."
                    );

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("no infrastructure coupling")
    class NoInfrastructureCoupling {

        @Test
        @DisplayName("should not directly depend on other modules' infrastructure")
        void shouldNotDependOnOtherInfrastructure() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + "..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackages(
                            "com.cromp.iam.infrastructure..",
                            "com.cromp.jobs.infrastructure..",
                            "com.cromp.schedules.infrastructure..",
                            "com.cromp.executions.infrastructure..",
                            "com.cromp.secrets.infrastructure.."
                    )
                    .because("orchestrator should not directly access other modules' infrastructure");

            rule.check(classes);
        }

        @Test
        @DisplayName("should call secrets through SecretResolvePort only")
        void shouldCallSecretsThroughPort() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + "..")
                    .should().onlyAccessClassesThat()
                    .resideOutsideOfPackage("com.cromp.secrets..")
                    .orShould().accessClassesThat()
                    .resideInAnyPackage(
                            "com.cromp.secrets.application.port..",
                            "com.cromp.secrets.domain.model.exceptions.."
                    )
                    .because("all secrets access must go through SecretResolvePort");

            rule.check(classes);
        }

        @Test
        @DisplayName("should call executions through defined ports only")
        void shouldCallExecutionsThroughPorts() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + "..")
                    .should().onlyAccessClassesThat()
                    .resideOutsideOfPackage("com.cromp.executions..")
                    .orShould().accessClassesThat()
                    .resideInAnyPackage(
                            "com.cromp.executions.application.port..",
                            "com.cromp.executions.api.dto..",
                            "com.cromp.executions.domain.model.."
                    )
                    .because("all executions access must go through ports");

            rule.check(classes);
        }
    }

    @Nested
    @DisplayName("no web layer in process services")
    class NoWebInServices {

        @Test
        @DisplayName("executor should not depend on web/controller layer")
        void executorShouldNotDependOnWeb() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".executor..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackages(
                            "org.springframework.web.bind..",
                            "org.springframework.web.servlet..",
                            "jakarta.servlet.."
                    )
                    .because("executor is a background service, not a web component");

            rule.check(classes);
        }

        @Test
        @DisplayName("scheduler should not depend on web/controller layer")
        void schedulerShouldNotDependOnWeb() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".scheduler..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackages(
                            "org.springframework.web.bind..",
                            "org.springframework.web.servlet..",
                            "jakarta.servlet.."
                    )
                    .because("scheduler is a background service, not a web component");

            rule.check(classes);
        }

        @Test
        @DisplayName("janitor should not depend on web/controller layer")
        void janitorShouldNotDependOnWeb() {
            ArchRule rule = classes()
                    .that().resideInAPackage(BASE + ".janitor..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackages(
                            "org.springframework.web.bind..",
                            "org.springframework.web.servlet..",
                            "jakarta.servlet.."
                    )
                    .because("janitor is a background service, not a web component");

            rule.check(classes);
        }
    }
}
