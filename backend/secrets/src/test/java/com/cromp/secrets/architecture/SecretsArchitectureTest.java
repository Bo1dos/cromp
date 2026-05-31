package com.cromp.secrets.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.cromp.secrets", importOptions = com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class)
class SecretsArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("org.springframework..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("..infrastructure..");

    @ArchTest
    static final ArchRule application_should_not_depend_on_web =
            classes().that().resideInAPackage("..application..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("..infrastructure.web..");

    @ArchTest
    static final ArchRule api_should_not_depend_on_persistence =
            classes().that().resideInAPackage("..api..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("..infrastructure.persistence..");

    @ArchTest
    static final ArchRule facades_should_live_in_api_service =
            classes().that().resideInAPackage("..api.service..")
                    .should().haveSimpleNameEndingWith("Facade");

    @ArchTest
    static final ArchRule ports_should_live_in_application_or_domain =
            classes().that().resideInAnyPackage("..application.port..", "..domain.repository..")
                    .should().haveSimpleNameEndingWith("Port");

    @ArchTest
    static final ArchRule application_services_should_live_in_application_service =
            classes().that().resideInAPackage("..application.service..")
                    .should().haveSimpleNameEndingWith("ApplicationService");

    @ArchTest
    static final ArchRule repository_adapters_should_live_in_adapter_package =
            classes().that().resideInAPackage("..infrastructure.persistence.adapter..")
                    .should().haveSimpleNameEndingWith("RepositoryAdapter");

    @ArchTest
    static final ArchRule jpa_entities_should_live_in_entity_package =
            classes().that().resideInAPackage("..infrastructure.persistence.jpa.entity..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("JpaEntity");

    @ArchTest
    static final ArchRule infrastructure_should_not_be_used_directly_by_domain =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("..infrastructure..");

    @ArchTest
    static final ArchRule encryption_impl_should_stay_in_infrastructure =
            classes().that().resideInAnyPackage("..domain..", "..application..", "..api..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("..infrastructure.encryption..");

    @ArchTest
    static final ArchRule audit_adapter_should_stay_in_infrastructure =
            classes().that().resideInAnyPackage("..domain..", "..application..", "..api..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("..infrastructure.port..");

    @ArchTest
    static final ArchRule packages_should_be_free_of_cycles =
            slices().matching("com.cromp.secrets.(*)..").should().beFreeOfCycles();
}
