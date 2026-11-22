package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.aplicacao.porta.PedidosConsultas;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;

import jakarta.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Detalhe do pedido no adapter JPA (RF-VEN-009, DEC-12): o pedido vem com os itens na mesma consulta e os cartões e
 * cupons nas suas, no máximo três idas ao banco contra as quatro do carregamento todo preguiçoso (RNF-GER-001). Os três
 * não cabem em uma consulta: são {@code List} sem coluna de ordem e o Hibernate recusa o fetch de dois bags na mesma
 * consulta.
 */
@SpringBootTest
@Transactional
class PedidosConsultasJpaTest {

    @Autowired private PedidosConsultas consultas;

    @Autowired private PedidoRepositorio repositorio;

    @Autowired private EntityManager entityManager;

    @Test
    void rfVen009_detalheTrazItensCartoesECuponsComOMesmoResultado() {
        Long id = repositorio.salvar(Pedidos.emProcessamento(null, 1L)).getId();
        entityManager.clear();

        Optional<Pedido> encontrado = consultas.buscar(id);

        assertThat(encontrado).isPresent();
        Pedido pedido = encontrado.orElseThrow();
        assertThat(pedido.getItens())
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.livro().titulo()).isEqualTo("Dom Casmurro");
                    assertThat(item.precoUnitario()).isEqualTo(Dinheiro.de("47.88"));
                    assertThat(item.quantidade()).isEqualTo(2);
                });
        assertThat(pedido.getCartoes())
                .singleElement()
                .satisfies(cartao -> {
                    assertThat(cartao.cartao().bandeira()).isEqualTo("Visa");
                    assertThat(cartao.cartao().ultimosDigitos()).isEqualTo("1111");
                    assertThat(cartao.valor()).isEqualTo(Dinheiro.de("57.76"));
                });
        assertThat(pedido.getCupons()).hasSize(2);
        assertThat(pedido.getSubtotal()).isEqualTo(Dinheiro.de("95.76"));
        assertThat(pedido.getFrete()).isEqualTo(Dinheiro.de("12.00"));
        assertThat(pedido.getDescontoPromocional()).isEqualTo(Dinheiro.de("10.00"));
        assertThat(pedido.getTotal()).isEqualTo(Dinheiro.de("97.76"));
    }

    @Test
    void rnfGer001_detalheFazNoMaximoTresConsultas() {
        Long id = repositorio.salvar(Pedidos.emProcessamento(null, 1L)).getId();
        Statistics estatisticas = entityManager.unwrap(Session.class).getSessionFactory().getStatistics();
        estatisticas.setStatisticsEnabled(true);
        entityManager.clear();
        estatisticas.clear();

        consultas.buscar(id).orElseThrow();

        // O carregamento todo preguiçoso faria quatro: o pedido e as três coleções. O detalhe traz os itens
        // na mesma consulta do pedido e paga só mais os cartões e os cupons (RNF-GER-001).
        assertThat(estatisticas.getPrepareStatementCount()).isLessThanOrEqualTo(3);
    }

    @Test
    void rfVen009_pedidoInexistenteDevolveVazio() {
        assertThat(consultas.buscar(999_999L)).isEmpty();
    }
}
