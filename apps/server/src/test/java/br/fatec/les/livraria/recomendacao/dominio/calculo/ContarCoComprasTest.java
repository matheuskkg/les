package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.recomendacao.dominio.modelo.CompraRegistrada;

class ContarCoComprasTest {

    @Test
    void rnfRec001_contaCadaOutroClienteQueComprouUmLivroEmComum() {
        List<CompraRegistrada> compras = List.of(
                new CompraRegistrada(1L, 10L, 1),
                new CompraRegistrada(2L, 10L, 1), new CompraRegistrada(2L, 20L, 3), new CompraRegistrada(2L, 20L, 1),
                new CompraRegistrada(3L, 10L, 1), new CompraRegistrada(3L, 20L, 1), new CompraRegistrada(3L, 30L, 1),
                new CompraRegistrada(4L, 40L, 1));

        assertThat(new ContarCoCompras().contar(1L, compras))
                .containsEntry(20L, 2)
                .containsEntry(30L, 1)
                .doesNotContainKey(40L);
    }

    @Test
    void rnfRec001_clienteSemComprasNaoTemCoCompra() {
        assertThat(new ContarCoCompras().contar(9L, List.of(new CompraRegistrada(1L, 10L, 1)))).isEmpty();
    }

    @Test
    void rnfRec001_vendidosSomaAsQuantidades() {
        assertThat(new ContarVendidos().contar(List.of(new CompraRegistrada(1L, 10L, 2),
                new CompraRegistrada(2L, 10L, 3)))).containsEntry(10L, 5);
    }
}
