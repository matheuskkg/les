package br.fatec.les.livraria.vendas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

/** RF-VEN-001, RF-VEN-002, DEC-16: regras do agregado do carrinho. */
class CarrinhoTest {

    @Test
    void rfVen001_adicionarOMesmoLivroSomaNaLinhaExistente() {
        Carrinho carrinho = Carrinho.vazio(1L);
        carrinho.adicionar(10L, 2);
        carrinho.adicionar(10L, 3);
        carrinho.adicionar(11L, 1);

        assertThat(carrinho.getItens()).hasSize(2);
        assertThat(carrinho.quantidadeDoLivro(10L)).isEqualTo(5);
        assertThat(carrinho.quantidadeDoLivro(99L)).isZero();
    }

    @Test
    void rfVen002_quantidadeNuncaFicaAbaixoDeUm() {
        Carrinho carrinho = Carrinho.vazio(1L);
        carrinho.adicionar(10L, 1);

        assertThatThrownBy(() -> carrinho.alterarQuantidade(10L, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> carrinho.adicionar(10L, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rfVen002_alterarOuRemoverLivroForaDoCarrinhoNaoEhEncontrado() {
        Carrinho carrinho = Carrinho.vazio(1L);

        assertThatThrownBy(() -> carrinho.alterarQuantidade(10L, 2))
                .isInstanceOf(ExcecaoDeRecursoNaoEncontrado.class);
        assertThatThrownBy(() -> carrinho.remover(10L)).isInstanceOf(ExcecaoDeRecursoNaoEncontrado.class);
    }

    @Test
    void rnVen014_renovarOPrazoZeraOAvisoEEsvaziarEncerraOBloqueio() {
        LocalDateTime prazo = LocalDateTime.of(2026, 10, 24, 12, 30);
        Carrinho carrinho = new Carrinho(1L, 1L, prazo.minusMinutes(10), prazo.minusMinutes(15), java.util.List.of());

        carrinho.renovarExpiracao(prazo);
        assertThat(carrinho.getExpiraEm()).isEqualTo(prazo);
        assertThat(carrinho.getAvisoEnviadoEm()).isNull();

        carrinho.encerrarExpiracao();
        assertThat(carrinho.getExpiraEm()).isNull();
    }
}
