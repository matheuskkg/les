package br.fatec.les.livraria.recomendacao.dominio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.recomendacao.dominio.modelo.CompraRegistrada;

import org.junit.jupiter.api.Test;

import java.util.List;

class ContarCoComprasTest {

    @Test
    void rnfRec001_contaCadaOutroClienteQueComprouUmLivroEmComum() {
        List<CompraRegistrada> compras =
                List.of(
                        new CompraRegistrada(1L, 10L, 1),
                        new CompraRegistrada(2L, 10L, 1),
                        new CompraRegistrada(2L, 20L, 3),
                        new CompraRegistrada(2L, 20L, 1),
                        new CompraRegistrada(3L, 10L, 1),
                        new CompraRegistrada(3L, 20L, 1),
                        new CompraRegistrada(3L, 30L, 1),
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
}
