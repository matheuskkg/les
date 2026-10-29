package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.Atendimento;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.OperacaoIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;
import br.fatec.les.livraria.recomendacao.infraestrutura.persistencia.ChamadaIaJpa;
import br.fatec.les.livraria.recomendacao.infraestrutura.persistencia.ChamadaIaJpaRepository;

/**
 * Cadeia sobre o seed em H2 (RNF-REC-001, DEC-03): a ordem padrão de {@code provedor_ia} e o atendimento pelo fake
 * quando nenhum provedor real tem chave, com a tentativa gravada em {@code chamada_ia}.
 */
// Sem rede nem cota, mesmo com IA_*_CHAVE exportada no terminal (DEC-03).
@SpringBootTest(properties = {
        "livraria.ia.provedores.gemini.chave=",
        "livraria.ia.provedores.groq.chave=",
        "livraria.ia.provedores.openrouter.chave="})
class CadeiaDeProvedoresIntegracaoTest {

    private static final PerfilDoCliente PERFIL = new PerfilDoCliente(List.of(), List.of(), List.of(), List.of());
    private static final List<LivroCandidato> CANDIDATOS = List.of(new LivroCandidato(
            1L, "Dom Casmurro", List.of("Machado de Assis"), List.of("Romance"), Dinheiro.de("29.90"),
            BigDecimal.ONE));

    @Autowired
    private CadeiaDeProvedores cadeia;

    @Autowired
    private ProvedoresIaRepositorio provedores;

    @Autowired
    private ChamadaIaJpaRepository chamadas;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void limparAntes() {
        limpar();
    }

    @AfterEach
    void limparDepois() {
        limpar();
    }

    private void limpar() {
        JdbcClient.create(dataSource).sql("delete from chamada_ia").update();
        cadeia.limpar();
    }

    @Test
    void rnfRec001_seedTemACadeiaPadraoNaOrdem() {
        assertThat(provedores.listarEmOrdem())
                .extracting(ProvedorIa::nome, ProvedorIa::ordem, ProvedorIa::ativo)
                .containsExactly(
                        tuple("gemini", 1, true),
                        tuple("groq", 2, true),
                        tuple("openrouter", 3, true),
                        tuple("fake", 4, true));
    }

    @Test
    void rnfRec001_semChavesOFakeAtendeERegistraSucesso() {
        Atendimento<RespostaDeRecomendacao> atendimento = cadeia.recomendar(PERFIL, CANDIDATOS);

        assertThat(atendimento.provedor()).isEqualTo("fake");
        assertThat(atendimento.resposta().itens()).extracting(item -> item.livroId()).containsExactly(1L);

        List<ChamadaIaJpa> registradas = chamadas.findAll();
        assertThat(registradas).hasSize(1);
        ChamadaIaJpa chamada = registradas.get(0);
        assertThat(chamada.getProvedor().getId()).isEqualTo(4L);
        assertThat(chamada.getModelo()).isEqualTo("fake");
        assertThat(chamada.getOperacao()).isEqualTo(OperacaoIa.RECOMENDAR);
        assertThat(chamada.getTentativa()).isEqualTo(1);
        assertThat(chamada.getResultado()).isEqualTo(ResultadoDaChamada.SUCESSO);
        assertThat(chamada.getStatusHttp()).isNull();
        assertThat(chamada.getErro()).isNull();
        assertThat(chamada.getTokensEntrada()).isNull();
        assertThat(chamada.getTokensSaida()).isNull();
        assertThat(chamada.getChamadasHttp()).isZero();
        assertThat(chamada.getTokensDasVoltas()).isNull();
    }
}
