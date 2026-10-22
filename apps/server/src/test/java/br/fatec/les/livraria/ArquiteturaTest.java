package br.fatec.les.livraria;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Direção das dependências entre camadas (docs/arquitetura.md). */
@AnalyzeClasses(packages = "br.fatec.les.livraria", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioNaoUsaJpa = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.hibernate..")
            .allowEmptyShould(true);

    /** O formato do JSON e do OpenAPI é decidido na borda (DEC-07). */
    @ArchTest
    static final ArchRule dominioNaoUsaJacksonNemSpringdoc = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.fasterxml.jackson..", "tools.jackson..", "io.swagger..", "org.springdoc..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dominioNaoDependeDeOutrasCamadas = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat().resideInAnyPackage("..aplicacao..", "..infraestrutura..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDeInfraestrutura = noClasses()
            .that().resideInAPackage("..aplicacao..")
            .should().dependOnClassesThat().resideInAPackage("..infraestrutura..")
            .allowEmptyShould(true);
}
