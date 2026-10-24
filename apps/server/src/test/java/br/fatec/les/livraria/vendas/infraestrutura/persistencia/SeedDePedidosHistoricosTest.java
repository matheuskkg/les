package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;

/** Seed 400_pedidos_historicos.sql do ANA-01 em H2 (RF-ANA-001, DEC-12, DEC-14, DEC-16). */
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
    void historicoVaiDeNovembroDe2024AOutubroDe2026SoComCarlaEElisa_DEC_14() {
        assertThat(jdbc.sql("select min(realizado_em) from pedido").query(LocalDateTime.class).single())
                .isAfterOrEqualTo(LocalDateTime.of(2024, 11, 1, 0, 0));
        assertThat(jdbc.sql("select max(realizado_em) from pedido").query(LocalDateTime.class).single())
                .isBefore(LocalDateTime.of(2026, 10, 21, 0, 0));
        assertThat(jdbc.sql("select distinct cliente_id from pedido order by cliente_id").query(Long.class).list())
                .containsExactly(3L, 5L);
        assertThat(jdbc.sql("select max(id) from pedido").query(Long.class).single()).isLessThan(10_000L);
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
