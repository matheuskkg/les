package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.vendas.aplicacao.porta.PedidoRepositorio;
import br.fatec.les.livraria.vendas.dominio.pedido.Pedidos;

/** Uso do promocional por cliente (DEC-15.2) sobre o seed: BEMVINDO10 é o promocional 1 e Ana, a cliente 1. */
@SpringBootTest
@Transactional
class CuponsJpaTest {

    @Autowired
    private CuponsJpa cupons;

    @Autowired
    private PedidoRepositorio pedidos;

    @Test
    void dec15_2_segundoUsoDoMesmoClienteEhBarradoPeloUnique() {
        long primeiro = pedidos.salvar(Pedidos.emProcessamento(null, 1L)).getId();
        long segundo = pedidos.salvar(Pedidos.emProcessamento(null, 1L)).getId();
        cupons.registrarUso(1L, 1L, primeiro);

        assertThat(cupons.usadoPeloCliente(1L, 1L)).isTrue();
        assertThatThrownBy(() -> cupons.registrarUso(1L, 1L, segundo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void dec15_2_reprovacaoApagaOUsoEDevolveOPromocional() {
        long pedido = pedidos.salvar(Pedidos.emProcessamento(null, 1L)).getId();
        cupons.registrarUso(1L, 1L, pedido);

        cupons.removerUso(pedido);

        assertThat(cupons.usadoPeloCliente(1L, 1L)).isFalse();
    }
}
