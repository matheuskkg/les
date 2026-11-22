package br.fatec.les.livraria.vendas.dominio.regra;

import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.HOJE;
import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.promocional;
import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.reais;
import static br.fatec.les.livraria.vendas.dominio.pagamento.Cupons.troca;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomPromocional;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.CuponsDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.CuponsDeTrocaNaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.PromocionalNaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.SituacaoCupom;
import br.fatec.les.livraria.vendas.dominio.pagamento.TipoCupomPromocional;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** RN-VEN-003, RN-VEN-006, RN-VEN-007, DEC-11: regras dos cupons da compra. */
class RegrasDosCuponsTest {

    @Test
    void rnVen003_umPromocionalPorCompra() {
        UmCupomPromocionalPorCompra regra = new UmCupomPromocionalPorCompra();

        assertThat(violacoes(regra, List.of())).isEmpty();
        assertThat(violacoes(regra, List.of("A"))).isEmpty();
        assertThat(violacoes(regra, List.of("A", "FRETE"))).extracting(Violacao::codigo).containsExactly("RN-VEN-003");
    }

    @Test
    void rnVen006_exemploDoDrsComTresCuponsEhRecusado() {
        // compra de R$ 50,00 com cupons de R$ 20, R$ 40 e R$ 35: qualquer um sai e os outros cobrem
        CuponsDaCompra compra =
                new CuponsDaCompra(
                        reais("38.00"),
                        reais("12.00"),
                        null,
                        List.of(troca("T20", "20"), troca("T40", "40"), troca("T35", "35")));

        List<Violacao> encontradas = violacoes(new CuponsDesnecessarios(), compra);

        assertThat(encontradas).hasSize(3).allMatch(violacao -> violacao.codigo().equals("RN-VEN-006"));
    }

    @Test
    void rnVen006_exemploDoDrsComDoisCuponsEhAceito() {
        CuponsDaCompra compra =
                new CuponsDaCompra(
                        reais("38.00"), reais("12.00"), null, List.of(troca("T20", "20"), troca("T40", "40")));

        assertThat(violacoes(new CuponsDesnecessarios(), compra)).isEmpty();
    }

    @Test
    void rnVen006_promocionalDesnecessarioQuandoOsDeTrocaCobrem() {
        CuponsDaCompra compra =
                new CuponsDaCompra(
                        reais("38.00"),
                        reais("12.00"),
                        promocional(TipoCupomPromocional.VALOR_FIXO, "10"),
                        List.of(troca("T60", "60")));

        assertThat(violacoes(new CuponsDesnecessarios(), compra))
                .singleElement()
                .satisfies(violacao -> assertThat(violacao.mensagem()).contains("PROMO"));
    }

    @Test
    void rnVen006_percentualContaArredondadoNaComparacao() {
        // 50% de R$ 33,33 = R$ 16,665 → R$ 16,67; com o cupom de R$ 16,66 cobre exatamente R$ 33,33 sem frete
        CuponsDaCompra compra =
                new CuponsDaCompra(
                        reais("33.33"),
                        reais("0"),
                        promocional(TipoCupomPromocional.PERCENTUAL, "50"),
                        List.of(troca("T", "16.66")));

        assertThat(violacoes(new CuponsDesnecessarios(), compra)).isEmpty();
    }

    @Test
    void rnVen007_promocionalVencidoAbaixoDoMinimoOuJaUsado() {
        CupomPromocional cupom =
                new CupomPromocional(
                        1L, "MIN100", TipoCupomPromocional.PERCENTUAL, new BigDecimal("15"), HOJE, reais("100.00"));
        CupomPromocionalValido regra = new CupomPromocionalValido();

        assertThat(violacoes(regra, new PromocionalNaCompra(cupom, reais("100.00"), HOJE, false))).isEmpty();
        assertThat(violacoes(regra, new PromocionalNaCompra(cupom, reais("99.99"), HOJE.plusDays(1), true)))
                .hasSize(3)
                .allMatch(violacao -> violacao.codigo().equals("RN-VEN-007"))
                .anySatisfy(violacao -> assertThat(violacao.mensagem()).contains("R$ 100,00"));
    }

    @Test
    void rnVen007_cupomDeTrocaUsadoOuVencidoNaoEstaDisponivel() {
        CupomTroca usado =
                new CupomTroca(
                        2L, 1L, "USADO", reais("10"), null, SituacaoCupom.USADO, LocalDateTime.of(2026, 1, 1, 0, 0));
        CupomTroca vencido =
                new CupomTroca(
                        3L,
                        1L,
                        "VENCIDO",
                        reais("10"),
                        HOJE.minusDays(1),
                        SituacaoCupom.DISPONIVEL,
                        LocalDateTime.of(2026, 1, 1, 0, 0));
        CupomTroca valeHoje =
                new CupomTroca(
                        4L,
                        1L,
                        "HOJE",
                        reais("10"),
                        HOJE,
                        SituacaoCupom.DISPONIVEL,
                        LocalDateTime.of(2026, 1, 1, 0, 0));

        assertThat(
                        violacoes(
                                new CuponsDeTrocaDisponiveis(),
                                new CuponsDeTrocaNaCompra(List.of(usado, vencido, valeHoje), HOJE)))
                .extracting(Violacao::mensagem)
                .containsExactly("O cupom USADO não está disponível.", "O cupom VENCIDO não está disponível.");
    }

    @Test
    void rnVen007_cupomDeTrocaRepetidoEhRecusado() {
        CuponsDeTrocaSemRepeticao regra = new CuponsDeTrocaSemRepeticao();

        assertThat(violacoes(regra, List.of(1L, 2L))).isEmpty();
        assertThat(violacoes(regra, List.of(1L, 1L))).hasSize(1);
    }

    private static <T> List<Violacao> violacoes(Regra<T> regra, T alvo) {
        Violacoes violacoes = new Violacoes();
        regra.validar(alvo, violacoes);
        return violacoes.lista();
    }
}
