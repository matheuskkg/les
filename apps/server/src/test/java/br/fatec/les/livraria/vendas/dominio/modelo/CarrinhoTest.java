package br.fatec.les.livraria.vendas.dominio.modelo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

/** RF-VEN-001, RF-VEN-002, RN-VEN-014, RN-VEN-015, RNF-VEN-001, DEC-16: regras do agregado do carrinho. */
class CarrinhoTest {

    private static final LocalDateTime PRAZO = LocalDateTime.of(2026, 10, 24, 12, 30);
    private static final Duration AVISO = Duration.ofMinutes(5);

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
        Carrinho carrinho = new Carrinho(1L, 1L, prazo.minusMinutes(10), prazo.minusMinutes(15), List.of(),
                List.of());

        carrinho.renovarExpiracao(prazo);
        assertThat(carrinho.getExpiraEm()).isEqualTo(prazo);
        assertThat(carrinho.getAvisoEnviadoEm()).isNull();

        carrinho.encerrarExpiracao();
        assertThat(carrinho.getExpiraEm()).isNull();
    }

    @Test
    void rnVen014_avisoSaiUmaVezDentroDaAntecedencia() {
        Carrinho carrinho = comLivro(PRAZO);

        assertThat(carrinho.deveAvisarEm(PRAZO.minusMinutes(6), AVISO)).isFalse();
        assertThat(carrinho.deveAvisarEm(PRAZO.minusMinutes(5), AVISO)).isTrue();
        assertThat(carrinho.deveAvisarEm(PRAZO.minusSeconds(1), AVISO)).isTrue();
        assertThat(carrinho.deveAvisarEm(PRAZO, AVISO)).isFalse();

        carrinho.registrarAviso(PRAZO.minusMinutes(4));
        assertThat(carrinho.deveAvisarEm(PRAZO.minusMinutes(3), AVISO)).isFalse();
    }

    @Test
    void rnVen014_carrinhoVazioNaoAvisaNemVence() {
        Carrinho carrinho = new Carrinho(1L, 1L, PRAZO, null, List.of(), List.of());

        assertThat(carrinho.deveAvisarEm(PRAZO.minusMinutes(1), AVISO)).isFalse();
        assertThat(carrinho.venceuEm(PRAZO.plusMinutes(1))).isFalse();
    }

    @Test
    void rnVen014_venceNoInstanteDoPrazo() {
        Carrinho carrinho = comLivro(PRAZO);

        assertThat(carrinho.venceuEm(PRAZO.minusSeconds(1))).isFalse();
        assertThat(carrinho.venceuEm(PRAZO)).isTrue();
    }

    @Test
    void rnVen015_expirarRetiraTodasAsUnidadesDeCadaLivroEGuardaNosRemovidos() {
        Carrinho carrinho = comLivro(PRAZO);
        carrinho.adicionar(11L, 1);

        List<ItemRemovidoCarrinho> retirados = carrinho.expirar(PRAZO);

        assertThat(carrinho.getItens()).isEmpty();
        assertThat(carrinho.getExpiraEm()).isNull();
        assertThat(carrinho.getRemovidos()).containsExactlyElementsOf(retirados)
                .containsExactly(new ItemRemovidoCarrinho(10L, 3, PRAZO), new ItemRemovidoCarrinho(11L, 1, PRAZO));
    }

    @Test
    void rnfVen001_novaRetiradaDoMesmoLivroSubstituiAAnterior() {
        Carrinho carrinho = comLivro(PRAZO);
        carrinho.expirar(PRAZO);

        carrinho.registrarRemovidos(List.of(new ItemRemovidoCarrinho(10L, 1, PRAZO.plusHours(1))));

        assertThat(carrinho.getRemovidos()).containsExactly(new ItemRemovidoCarrinho(10L, 1, PRAZO.plusHours(1)));
    }

    @Test
    void rnfVen001_readicionarOLivroApagaOAvisoDeRemovido() {
        Carrinho carrinho = comLivro(PRAZO);
        carrinho.adicionar(11L, 1);
        carrinho.expirar(PRAZO);

        carrinho.adicionar(10L, 1);

        assertThat(carrinho.getRemovidos()).extracting(ItemRemovidoCarrinho::livroId).containsExactly(11L);
    }

    @Test
    void rnfVen001_dispensarApagaOAvisoESoValeParaLivroRemovido() {
        Carrinho carrinho = comLivro(PRAZO);
        carrinho.expirar(PRAZO);

        carrinho.dispensarRemovido(10L);

        assertThat(carrinho.getRemovidos()).isEmpty();
        assertThatThrownBy(() -> carrinho.dispensarRemovido(10L))
                .isInstanceOf(ExcecaoDeRecursoNaoEncontrado.class);
    }

    @Test
    void dec10_esvaziarApagaItensRemovidosEPrazo() {
        Carrinho carrinho = comLivro(PRAZO);
        carrinho.registrarRemovidos(List.of(new ItemRemovidoCarrinho(11L, 1, PRAZO)));

        carrinho.esvaziar();

        assertThat(carrinho.vazio()).isTrue();
        assertThat(carrinho.getRemovidos()).isEmpty();
        assertThat(carrinho.getExpiraEm()).isNull();
    }

    private static Carrinho comLivro(LocalDateTime prazo) {
        Carrinho carrinho = new Carrinho(1L, 1L, null, null, List.of(), List.of());
        carrinho.adicionar(10L, 3);
        carrinho.renovarExpiracao(prazo);
        return carrinho;
    }
}
