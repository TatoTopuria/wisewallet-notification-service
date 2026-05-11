package com.wisewallet.notification.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.wisewallet.notification", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domainMustNotDependOnApplication =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..infrastructure..", "..presentation..");

    @ArchTest
    static final ArchRule domainMustNotUseSpring =
            noClasses().that().resideInAPackage("..domain..")
                    .and().areNotAnnotatedWith(jakarta.persistence.Entity.class)
                    .and().areNotAnnotatedWith(jakarta.persistence.Embeddable.class)
                    .and().areNotAnnotatedWith(jakarta.persistence.MappedSuperclass.class)
                    .should().dependOnClassesThat()
                    .resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule applicationMustNotDependOnInfrastructure =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule applicationMustNotDependOnPresentation =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..presentation..");

    @ArchTest
    static final ArchRule presentationMustNotDependOnInfrastructure =
            noClasses().that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");
}
