package br.fatec.les.livraria;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import org.hibernate.envers.Audited;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import jakarta.persistence.Entity;

/**
 * Toda entidade JPA de escrita precisa de {@code @Audited} (RNF-GER-002, DEC-08, DEC-14): sem isso, uma escrita
 * passaria sem revisão e sem a linha na tabela {@code _aud}. As tabelas técnicas {@code seed_execucao} e
 * {@code revisao} são as únicas exceções.
 */
@AnalyzeClasses(packages = "br.fatec.les.livraria", importOptions = ImportOption.DoNotIncludeTests.class)
class EntidadesAuditadasTest {

    @ArchTest
    static final ArchRule todaEntidadeDeEscritaEhAuditada = classes()
            .that().areAnnotatedWith(Entity.class)
            .and().doNotHaveSimpleName("SeedExecucaoJpa")
            .and().doNotHaveSimpleName("RevisaoJpa")
            .should().beAnnotatedWith(Audited.class)
            .because("RNF-GER-002 exige registrar toda escrita nas tabelas _aud do Envers");
}
