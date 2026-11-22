package br.fatec.les.livraria;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import jakarta.persistence.Entity;

import org.hibernate.envers.Audited;

/**
 * Toda entidade JPA de escrita precisa de {@code @Audited} (RNF-GER-002, DEC-08, DEC-14): sem isso, uma escrita
 * passaria sem revisão e sem a linha na tabela {@code _aud}. As tabelas técnicas {@code seed_execucao} e {@code
 * revisao} e o log só de inserção {@code chamada_ia} (DEC-03) são as exceções.
 */
@AnalyzeClasses(packages = "br.fatec.les.livraria", importOptions = ImportOption.DoNotIncludeTests.class)
class EntidadesAuditadasTest {

    @ArchTest
    static final ArchRule todaEntidadeDeEscritaEhAuditada =
            classes()
                    .that()
                    .areAnnotatedWith(Entity.class)
                    .and()
                    .doNotHaveSimpleName("SeedExecucaoJpa")
                    .and()
                    .doNotHaveSimpleName("RevisaoJpa")
                    .and()
                    .doNotHaveSimpleName("ChamadaIaJpa")
                    .should()
                    .beAnnotatedWith(Audited.class)
                    .because("RNF-GER-002 exige registrar toda escrita nas tabelas _aud do Envers");
}
