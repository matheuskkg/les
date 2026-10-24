package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.dominio.pedido.CategoriaCupom;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedido;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;
import br.fatec.les.livraria.vendas.dominio.pedido.StatusPedido;
import jakarta.persistence.EntityManager;

/** Gravação e leitura do pedido com as cópias em JSON (DEC-12, DEC-16). */
@SpringBootTest
@Transactional
class PedidoRepositorioJpaTest {

    @Autowired
    private PedidoRepositorioJpa pedidos;

    @Autowired
    private EntityManager entityManager;

    @Test
    void rfVen008_gravaELeDeVoltaAsCopiasDoPedido() {
        Pedido gravado = pedidos.salvar(Pedidos.emProcessamento(null, 1L));
        entityManager.clear();

        Pedido lido = pedidos.buscarParaAlterar(gravado.getId()).orElseThrow();

        assertThat(lido.getStatus()).isEqualTo(StatusPedido.EM_PROCESSAMENTO);
        assertThat(lido.getRealizadoEm()).isEqualTo(Pedidos.REALIZADO_EM);
        assertThat(lido.getCliente().cpf()).isEqualTo("52998224725");
        assertThat(lido.getEnderecoEntrega().cep()).isEqualTo("01415000");
        assertThat(lido.getItens()).singleElement().satisfies(item -> {
            assertThat(item.livro().categorias()).extracting("nome").containsExactly("Romance", "Clássicos");
            assertThat(item.livro().autores()).containsExactly("Machado de Assis");
            assertThat(item.precoUnitario().valor()).isEqualByComparingTo("47.88");
            assertThat(item.quantidade()).isEqualTo(2);
        });
        assertThat(lido.getCartoes()).singleElement()
                .satisfies(cartao -> assertThat(cartao.cartao().ultimosDigitos()).isEqualTo("1111"));
        assertThat(lido.getCupons()).extracting(cupom -> cupom.cupom().categoria())
                .containsExactly(CategoriaCupom.PROMOCIONAL, CategoriaCupom.TROCA);
        assertThat(lido.cuponsDeTrocaUsados()).containsExactly(2L);
        assertThat(lido.getSubtotal().valor()).isEqualByComparingTo("95.76");
        assertThat(lido.getTotal().valor()).isEqualByComparingTo("97.76");
    }
}
