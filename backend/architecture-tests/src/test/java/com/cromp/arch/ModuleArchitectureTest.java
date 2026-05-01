package com.cromp.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Архитектурные тесты: проверка границ модулей.
 * Запускается из common, но сканирует весь проект.
 */
class ModuleArchitectureTest {

    private static final String ROOT_PACKAGE = "com.cromp";
    private static final String COMMON_PACKAGE = ROOT_PACKAGE + ".common..";
    private static final String API_PACKAGE = ROOT_PACKAGE + ".api..";
    private static final String ORCHESTRATOR_PACKAGE = ROOT_PACKAGE + ".orchestrator..";
    
    private static final String[] DOMAIN_MODULES = {
            ROOT_PACKAGE + ".iam..",
            ROOT_PACKAGE + ".jobs..",
            ROOT_PACKAGE + ".schedules..",
            ROOT_PACKAGE + ".executions..",
            ROOT_PACKAGE + ".secrets..",
            ROOT_PACKAGE + ".analytics.."
    };

    private JavaClasses importAllClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT_PACKAGE);
    }

    @Test
    void commonShouldNotDependOnDomainModules() {
        JavaClasses classes = importAllClasses();
        noClasses()
            .that().resideInAPackage(COMMON_PACKAGE)
            .should().dependOnClassesThat().resideInAnyPackage(DOMAIN_MODULES)
            .allowEmptyShould(true)  // разрешаем пустой результат (модуль может быть пуст)
            .check(classes);
    }

    @Test
    void commonShouldNotDependOnApi() {
        JavaClasses classes = importAllClasses();
        noClasses()
            .that().resideInAPackage(COMMON_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(API_PACKAGE)
            .allowEmptyShould(true)
            .check(classes);
    }

    @Test
    void commonShouldNotDependOnOrchestrator() {
        JavaClasses classes = importAllClasses();
        noClasses()
            .that().resideInAPackage(COMMON_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(ORCHESTRATOR_PACKAGE)
            .allowEmptyShould(true)
            .check(classes);
    }

    @Test
    void domainModulesShouldNotDependOnApi() {
        JavaClasses classes = importAllClasses();
        for (String domainModule : DOMAIN_MODULES) {
            noClasses()
                .that().resideInAPackage(domainModule)
                .should().dependOnClassesThat().resideInAPackage(API_PACKAGE)
                .allowEmptyShould(true)
                .check(classes);
        }
    }

    @Test
    void domainModulesShouldNotDependOnOrchestrator() {
        JavaClasses classes = importAllClasses();
        for (String domainModule : DOMAIN_MODULES) {
            noClasses()
                .that().resideInAPackage(domainModule)
                .should().dependOnClassesThat().resideInAPackage(ORCHESTRATOR_PACKAGE)
                .allowEmptyShould(true)
                .check(classes);
        }
    }
}