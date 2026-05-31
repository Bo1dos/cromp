package com.cromp.iam.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "com.cromp.iam",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class IamArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat()
                    .resideOutsideOfPackage("org.springframework..");

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
    static final ArchRule facade_classes_should_have_proper_suffix =
            classes().that().resideInAPackage("..api.service..")
                    .should().haveSimpleNameEndingWith("Facade");

    @ArchTest
    static final ArchRule port_classes_should_have_proper_suffix =
            classes().that().resideInAnyPackage("..application.port..", "..domain.repository..")
                    .should().haveSimpleNameEndingWith("Port");

    @ArchTest
    static final ArchRule application_service_classes_should_have_proper_suffix =
            classes().that().resideInAPackage("..application.service..")
                    .should().haveSimpleNameEndingWith("ApplicationService");

    @ArchTest
    static final ArchRule packages_should_be_free_of_cycles =
            slices().matching("com.cromp.iam.(*)..").should().beFreeOfCycles();
}
