package com.cromp.analytics.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class AnalyticsArchitectureTest {

    private static final String PACKAGE = "com.cromp.analytics";

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(PACKAGE);
    }

    // ── Package rules ─────────────────────────────────────────────────────

    @Test
    void apiPackageShouldContainOnlyDtoAndMapper() {
        classes().that()
                .resideInAPackage("..api..")
                .should().resideInAnyPackage("..api.dto..", "..api.mapper..")
                .check(classes);
    }

    @Test
    void applicationPackageShouldContainOnlyServices() {
        classes().that()
                .resideInAPackage("..application..")
                .should().resideInAnyPackage("..application.service..")
                .check(classes);
    }

    @Test
    void infrastructurePersistenceShouldContainOnlyRepository() {
        classes().that()
                .resideInAPackage("..infrastructure.persistence..")
                .should().resideInAnyPackage("..infrastructure.persistence.repository..")
                .check(classes);
    }

    @Test
    void infrastructureMlClientShouldContainOnlyClient() {
        classes().that()
                .resideInAPackage("..infrastructure.mlclient..")
                .should().resideInAnyPackage(
                        "..infrastructure.mlclient.client..",
                        "..infrastructure.mlclient.contract..")
                .check(classes);
    }

    @Test
    void infrastructureWebShouldContainOnlyControllers() {
        classes().that()
                .resideInAPackage("..infrastructure.web..")
                .should().resideInAnyPackage("..infrastructure.web.controller..")
                .check(classes);
    }

    @Test
    void configPackageShouldContainOnlyConfigurationClasses() {
        classes().that()
                .resideInAPackage("..config..")
                .should().resideInAnyPackage("..config..")
                .check(classes);
    }

    // ── Dependency rules ──────────────────────────────────────────────────

    @Test
    void domainShouldNotDependOnSpring() {
        classes().that()
                .resideInAPackage("..domain..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackages(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate.."
                )
                .check(classes);
    }

    @Test
    void domainShouldNotDependOnInfrastructure() {
        noClasses().that()
                .resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..")
                .check(classes);
    }

    @Test
    void applicationShouldNotDependOnWeb() {
        noClasses().that()
                .resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.web..")
                .check(classes);
    }

    @Test
    void mapperShouldNotDependOnPersistence() {
        noClasses().that()
                .resideInAPackage("..api.mapper..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.persistence..")
                .check(classes);
    }

    @Test
    void repositoryImplShouldNotDependOnWeb() {
        noClasses().that()
                .resideInAPackage("..infrastructure.persistence..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.web..")
                .check(classes);
    }

    @Test
    void controllerShouldNotDirectlyAccessRepository() {
        noClasses().that()
                .resideInAPackage("..infrastructure.web..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.persistence..")
                .check(classes);
    }

    @Test
    void controllerShouldCallOnlyApplicationServices() {
        classes().that()
                .resideInAPackage("..infrastructure.web.controller..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..application.service..",
                        "..api..",
                        "java..",
                        "org.springframework..",
                        "lombok.."
                )
                .check(classes);
    }

    // ── Special checks ────────────────────────────────────────────────────

    @Test
    void mlClientShouldStayOnlyInInfrastructure() {
        classes().that()
                .haveSimpleNameContaining("MlClient")
                .should().resideInAPackage("..infrastructure.mlclient..")
                .check(classes);
    }

    @Test
    void repositoryImplShouldStayOnlyInPersistencePackage() {
        classes().that()
                .haveSimpleNameContaining("RepositoryImpl")
                .should().resideInAPackage("..infrastructure.persistence..")
                .check(classes);
    }

    @Test
    void noCircularDependencies() {
        com.tngtech.archunit.library.dependencies.SliceRule rule =
                com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
                        .matching(PACKAGE + ".(*)..")
                        .should().beFreeOfCycles();

        rule.check(classes);
    }

    @Test
    void noCaffeineCacheDirectlyInApplicationLayer() {
        noClasses().that()
                .resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.github.benmanes.caffeine..")
                .check(classes);
    }
}
