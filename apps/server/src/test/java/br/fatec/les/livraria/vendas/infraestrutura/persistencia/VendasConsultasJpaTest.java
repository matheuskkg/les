package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.vendas.aplicacao.consulta.CompraDeLivro;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;
import br.fatec.les.livraria.vendas.aplicacao.porta.VendasConsultas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

/**
 * Consultas de vendas da recomendação sobre o seed (RNF-REC-001, DEC-12, DEC-16, DEC-21). O histórico do cliente
 * filtrado no banco e as somas por cliente e livro são conferidos item a item com uma referência calculada em SQL puro,
 * que replica o filtro antigo em Java: item com quantidade líquida positiva (RNF-GER-001).
 */
@SpringBootTest
@Transactional
class VendasConsultasJpaTest {

    private static final List<String> CONTAM_COMO_VENDA = List.of("APROVADA", "EM_TRANSPORTE", "ENTREGUE");

    @Autowired private VendasConsultas vendas;

    @Autowired private DataSource dataSource;

    private final ObjectMapper json = new ObjectMapper();
    private JdbcClient jdbc;

    @BeforeEach
    void configurar() {
        jdbc = JdbcClient.create(dataSource);
    }

    /**
     * O invariante de troca — nenhum item trocado mais do que foi comprado — garante que somar o líquido por livro ou
     * por item dá o mesmo total, o que as agregações da recomendação assumem ao agrupar no banco. Quem o garante em
     * tempo de escrita é a regra {@code QuantidadeDisponivelParaTroca} (RF-VEN-011, DEC-12), que limita cada
     * solicitação de troca à quantidade comprada menos o que já está em qualquer troca, inclusive recusada, e tem
     * testes próprios em {@code RegrasDaSolicitacaoDeTrocaTest}; este teste protege esse invariante sobre o seed, em
     * vez de apenas fotografá-lo.
     */
    @Test
    void rnfGer001_nenhumItemFoiTrocadoMaisDoQueFoiComprado() {
        Long negativos =
                jdbc.sql(
                                """
                                select count(*) from item_pedido i
                                where i.quantidade < coalesce((
                                    select sum(it.quantidade) from item_troca it join troca t on t.id = it.troca_id
                                    where it.item_pedido_id = i.id and t.status = 'TROCADO'), 0)
                                """)
                        .query(Long.class)
                        .single();
        assertThat(negativos).isZero();
    }

    @Test
    void rnfRec001_comprasDoClienteDevolveSoOsItensLiquidosDoCliente() throws Exception {
        List<ItemLiquido> doCliente = itensLiquidos().stream().filter(item -> item.clienteId() == 3L).toList();

        assertThat(vendas.comprasDoCliente(3L))
                .containsExactlyInAnyOrderElementsOf(doCliente.stream().map(ItemLiquido::comoVendido).toList());
        assertThat(vendas.comprasDoCliente(1L)).isEmpty();
        assertThat(vendas.comprasDoCliente(999L)).isEmpty();
    }

    @Test
    void rnfRec001_unidadesVendidasSomamOLiquidoDeCadaLivro() throws Exception {
        // A mesma fórmula do cálculo anterior, item a item: soma por livro dos líquidos positivos.
        Map<Long, Integer> esperado =
                itensLiquidos().stream()
                        .collect(
                                Collectors.groupingBy(
                                        ItemLiquido::livroId, Collectors.summingInt(ItemLiquido::liquido)));

        assertThat(vendas.unidadesVendidasPorLivro()).isEqualTo(esperado);
    }

    @Test
    void rnfRec001_comprasRegistradasSomamPorClienteELivro() throws Exception {
        Map<Long, Map<Long, Integer>> porClienteELivro =
                itensLiquidos().stream()
                        .collect(
                                Collectors.groupingBy(
                                        ItemLiquido::clienteId,
                                        Collectors.groupingBy(
                                                ItemLiquido::livroId, Collectors.summingInt(ItemLiquido::liquido))));
        List<CompraDeLivro> esperado = new ArrayList<>();
        porClienteELivro.forEach((clienteId, livros) ->
                livros.forEach(
                        (livroId, quantidade) -> esperado.add(new CompraDeLivro(clienteId, livroId, quantidade))));

        assertThat(vendas.comprasRegistradas()).containsExactlyInAnyOrderElementsOf(esperado);
    }

    /** Itens dos pedidos que contam como venda, com a quantidade menos a trocada em trocas concluídas (DEC-16). */
    private List<ItemLiquido> itensLiquidos() throws Exception {
        Map<Long, Long> trocadas = new HashMap<>();
        jdbc.sql(
                        """
                        select it.item_pedido_id as itemId, sum(it.quantidade) as quantidade
                        from item_troca it join troca t on t.id = it.troca_id
                        join pedido p on p.id = t.pedido_id
                        where t.status = 'TROCADO' and p.status in ('APROVADA', 'EM_TRANSPORTE', 'ENTREGUE')
                        group by it.item_pedido_id
                        """)
                .query((linha, i) -> trocadas.put(linha.getLong("itemId"), linha.getLong("quantidade")))
                .list();

        return jdbc
                .sql(
                        """
                        select i.id as itemId, i.pedido_id as pedidoId, p.cliente_id as clienteId,
                               cast(i.livro as varchar) as livro, i.quantidade as quantidade,
                               p.realizado_em as realizadoEm
                        from item_pedido i join pedido p on p.id = i.pedido_id
                        where p.status in ('APROVADA', 'EM_TRANSPORTE', 'ENTREGUE')
                        """)
                .query((linha, i) -> item(linha, trocadas))
                .stream()
                .filter(item -> item.liquido() > 0)
                .toList();
    }

    private ItemLiquido item(java.sql.ResultSet linha, Map<Long, Long> trocadas) {
        try {
            JsonNode snapshot = json.readTree(linha.getString("livro"));
            List<String> autores = new ArrayList<>();
            snapshot.get("autores").forEach(autor -> autores.add(autor.asText()));
            List<String> categorias = new ArrayList<>();
            snapshot.get("categorias").forEach(categoria -> categorias.add(categoria.get("nome").asText()));
            int liquido = linha.getInt("quantidade") - trocadas.getOrDefault(linha.getLong("itemId"), 0L).intValue();
            return new ItemLiquido(
                    linha.getLong("itemId"),
                    linha.getLong("pedidoId"),
                    linha.getLong("clienteId"),
                    snapshot.get("id").asLong(),
                    snapshot.get("titulo").asText(),
                    autores,
                    categorias,
                    liquido,
                    linha.getTimestamp("realizadoEm").toLocalDateTime());
        } catch (java.sql.SQLException | com.fasterxml.jackson.core.JsonProcessingException excecao) {
            throw new IllegalStateException(excecao);
        }
    }

    /** Item do seed com a quantidade líquida, que sabe se devolver como {@link LivroVendido}. */
    private record ItemLiquido(
            Long itemId,
            Long pedidoId,
            Long clienteId,
            Long livroId,
            String titulo,
            List<String> autores,
            List<String> categorias,
            int liquido,
            LocalDateTime realizadoEm) {

        private LivroVendido comoVendido() {
            return new LivroVendido(pedidoId, clienteId, livroId, titulo, autores, categorias, liquido, realizadoEm);
        }
    }
}
