package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

/**
 * Seed 400_pedidos_historicos.sql do ANA-01 em H2 (RF-ANA-001, DEC-12, DEC-14, DEC-16, DEC-20, DEC-21). O banco guarda 36
 * meses, de nov/2023 a out/2026, com a Carla (3), a Elisa (5) e os cinco clientes novos do seed (6 a 10). A janela de
 * consulta da análise continua limitada a 24 meses (RN-ANA-002), mas isso não limita o histórico armazenado. Os
 * pedidos e as trocas da Carla e da Elisa, bem como os valores de março/2025 e outubro/2026, são preservados pelo
 * gerador porque testes de controller e roteiros E2E os conferem valor a valor.
 */
@SpringBootTest
@Transactional
class SeedDePedidosHistoricosTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PedidosConsultas pedidos;

    private JdbcClient jdbc;

    @BeforeEach
    void configurar() {
        jdbc = JdbcClient.create(dataSource);
    }

    @Test
    void historicoVaiDeNovembroDe2023AOutubroDe2026ComOsSeteClientesDoHistorico_DEC_20() {
        assertThat(jdbc.sql("select min(realizado_em) from pedido").query(LocalDateTime.class).single())
                .isEqualTo(LocalDateTime.of(2023, 11, 1, 20, 30));
        assertThat(jdbc.sql("select max(realizado_em) from pedido").query(LocalDateTime.class).single())
                .isEqualTo(LocalDateTime.of(2026, 10, 20, 10, 15));
        assertThat(jdbc.sql("select distinct cliente_id from pedido order by cliente_id").query(Long.class).list())
                .containsExactly(3L, 5L, 6L, 7L, 8L, 9L, 10L);
        assertThat(jdbc.sql("select max(id) from pedido").query(Long.class).single()).isLessThan(10_000L);
    }

    @Test
    void massaAmpliadaTem1180PedidosE147TrocasEm36Meses_RF_ANA_001() {
        assertThat(jdbc.sql("select count(*) from pedido").query(Long.class).single()).isEqualTo(1180L);
        assertThat(jdbc.sql("select count(*) from item_pedido").query(Long.class).single()).isEqualTo(2138L);
        assertThat(jdbc.sql("select count(*) from pagamento_cartao").query(Long.class).single()).isEqualTo(1180L);
        assertThat(jdbc.sql("select count(*) from troca").query(Long.class).single()).isEqualTo(147L);
        // Outros testes criam cupons TROCA-<n> no mesmo banco; os do gerador têm código TROCA-H<n>.
        assertThat(jdbc.sql("select count(*) from cupom where codigo like 'TROCA-H%'")
                .query(Long.class).single()).isEqualTo(83L);
        assertThat(jdbc.sql("""
                select count(distinct extract(year from realizado_em) * 100 + extract(month from realizado_em))
                from pedido
                """).query(Long.class).single())
                .as("meses com vendas")
                .isEqualTo(36L);
        assertThat(jdbc.sql("select count(distinct cliente_id) from pedido where cliente_id between 6 and 10")
                .query(Long.class).single())
                .as("clientes novos com histórico")
                .isEqualTo(5L);
    }

    @Test
    void alt02AcrescentaPedidosComStatusETrocasVariadosSobreOsLivrosNovos_RF_ANA_001() throws Exception {
        // Os 847 pedidos e todos os seus IDs, valores e datas permanecem; os novos começam em 848 (ALT-02).
        assertThat(jdbc.sql("select count(*) from pedido where id > 847").query(Long.class).single()).isEqualTo(333L);
        assertThat(jdbc.sql("select count(*) from item_pedido where pedido_id > 847").query(Long.class).single())
                .isEqualTo(614L);
        assertThat(jdbc.sql("select distinct status from pedido where id > 847 order by status")
                .query(String.class).list())
                .containsExactly("APROVADA", "EM_TRANSPORTE", "ENTREGUE", "REPROVADA");
        assertThat(jdbc.sql("""
                select distinct t.status from troca t join pedido p on p.id = t.pedido_id
                where p.id > 847 order by 1
                """).query(String.class).list())
                .containsExactly("TROCADO", "TROCA_AUTORIZADA", "TROCA_RECUSADA");

        // Os itens novos usam somente os 19 livros novos cujas categorias são todas novas. Esta é a invariável que
        // protege os valores de março/2025 e outubro/2026 conferidos pela análise e pelo E2E de apresentação.
        ObjectMapper json = new ObjectMapper();
        List<String> snapshots = jdbc.sql("select cast(livro as varchar) from item_pedido where pedido_id > 847")
                .query(String.class).list();
        assertThat(snapshots).hasSize(614);
        for (String snapshot : snapshots) {
            for (var categoria : json.readTree(snapshot).get("categorias")) {
                assertThat(categoria.get("id").asInt())
                        .as("categoria do livro em pedido novo (RF-ANA-001)")
                        .isGreaterThanOrEqualTo(6);
            }
        }
    }

    @Test
    void janelaEstendidaAntesDeNovembroDe2024TemDozeMesesDensos_DEC_20() {
        assertThat(jdbc.sql("select count(*) from pedido where realizado_em < TIMESTAMP '2024-11-01 00:00:00'")
                .query(Long.class).single()).isEqualTo(232L);
        assertThat(jdbc.sql("""
                select extract(year from realizado_em) * 100 + extract(month from realizado_em), count(*)
                from pedido
                where realizado_em < TIMESTAMP '2024-11-01 00:00:00'
                group by extract(year from realizado_em) * 100 + extract(month from realizado_em)
                order by 1
                """).query((linha, i) -> linha.getLong(1) + "=" + linha.getLong(2)).list())
                .containsExactly("202311=18", "202312=21", "202401=22", "202402=17", "202403=21", "202404=20",
                        "202405=18", "202406=20", "202407=17", "202408=16", "202409=22", "202410=20");
    }

    @Test
    void pagamentoCobreOTotalDeCadaPedido_RN_VEN_007() {
        Long divergentes = jdbc.sql("""
                select count(*) from pedido p
                where p.total <> (select sum(pc.valor) from pagamento_cartao pc where pc.pedido_id = p.id)
                   or p.subtotal_itens <> (select sum(i.preco_unitario * i.quantidade) from item_pedido i
                                           where i.pedido_id = p.id)
                   or p.total <> p.subtotal_itens + p.frete - p.desconto_promocional
                """).query(Long.class).single();
        assertThat(divergentes).isZero();
    }

    @Test
    void trocaConcluidaTemCupomNoValorDosItens_RN_VEN_012() {
        Long divergentes = jdbc.sql("""
                select count(*) from troca t
                left join cupom c on c.id = t.cupom_id
                where t.status = 'TROCADO' and (c.valor is null or c.valor <> t.valor_cupom or t.valor_cupom <>
                      (select sum(i.preco_unitario * it.quantidade) from item_troca it
                       join item_pedido i on i.id = it.item_pedido_id where it.troca_id = t.id))
                """).query(Long.class).single();
        assertThat(divergentes).isZero();
    }

    @Test
    void cadaPromocionalFoiUsadoNoMaximoUmaVezPorCliente_DEC_15_2() {
        Long duplicados = jdbc.sql("""
                select count(*) from (
                    select cupom_promocional_id, cliente_id
                    from uso_cupom_promocional
                    group by cupom_promocional_id, cliente_id
                    having count(*) > 1
                ) duplicados
                """).query(Long.class).single();
        assertThat(duplicados).isZero();
    }

    @Test
    void marcoDe2025TemOsPedidosDeReferenciaDaAnalise_RN_ANA_001() {
        var pedidosDoMes = jdbc.sql("""
                select status, count(*) from pedido
                where realizado_em >= TIMESTAMP '2025-03-01 00:00:00' and realizado_em < TIMESTAMP '2025-04-01 00:00:00'
                group by status order by status
                """).query((linha, i) -> linha.getString(1) + "=" + linha.getLong(2)).list();
        assertThat(pedidosDoMes).containsExactly("ENTREGUE=3", "REPROVADA=1");
        assertThat(jdbc.sql("""
                select t.status from troca t join pedido p on p.id = t.pedido_id
                where p.realizado_em >= TIMESTAMP '2025-03-01 00:00:00' and p.realizado_em < TIMESTAMP '2025-04-01 00:00:00'
                order by t.status
                """).query(String.class).list()).containsExactly("TROCADO", "TROCA_AUTORIZADA");
    }

    @Test
    void snapshotsEmJsonSaoLidosPeloPedido_DEC_16() {
        var pedido = pedidos.buscar(jdbc.sql("""
                select id from pedido where realizado_em = TIMESTAMP '2025-03-05 10:00:00'
                """).query(Long.class).single()).orElseThrow();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(pedido.getCliente().nome()).isEqualTo("Carla Mendes");
        var livro = pedido.getItens().getFirst().livro();
        assertThat(livro.titulo()).isEqualTo("Dom Casmurro");
        assertThat(livro.categorias()).hasSize(2);
        assertThat(pedido.getDescontoPromocional()).isEqualTo(Dinheiro.de("10.00"));
    }
}
