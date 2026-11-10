package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;

/**
 * Seed padrão de {@code provedor_ia} (RNF-REC-001, DEC-03): o fake fica inativo, para a apresentação não mascarar
 * uma falha dos provedores reais. O profile dev e os testes o ativam por um seed próprio.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:seed-provedores;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "livraria.seed.local=classpath:seed/*.sql" })
class SeedDosProvedoresIntegracaoTest {

    @Autowired
    private ProvedoresIaRepositorio provedores;

    @Test
    void rnfRec001_fakeFicaInativoPorPadrao() {
        assertThat(provedores.listarEmOrdem())
                .extracting(ProvedorIa::nome, ProvedorIa::ativo)
                .contains(
                        tuple("gemini", true),
                        tuple("groq", true),
                        tuple("openrouter", true),
                        tuple("nvidia", true),
                        tuple("fake", false));
    }
}
