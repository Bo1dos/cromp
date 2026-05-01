package com.cromp.arch.util;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

public final class ArchUnitImporter {

    private static final String ROOT_PACKAGE = "com.cromp";

    private ArchUnitImporter() {}

    public static JavaClasses importAllProductionClasses() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
                .importPackages(ROOT_PACKAGE);
    }

    public static JavaClasses importPackages(String... packages) {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(packages);
    }
}