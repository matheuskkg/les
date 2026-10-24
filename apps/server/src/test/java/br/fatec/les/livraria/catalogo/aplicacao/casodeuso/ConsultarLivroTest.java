package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.catalogo.aplicacao.EnriquecerComPrecoEDisponibilidade;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroConsultado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.catalogo.aplicacao.porta.MargemPorLivro;
import br.fatec.les.livraria.catalogo.dominio.calculo.CalcularPrecoVenda;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarDisponibilidade;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarMaiorCusto;
import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDeEstoque;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDoLivro;
import br.fatec.les.livraria.estoque.dominio.calculo.CalcularDisponivel;

/** Detalhe da vitrine enriquecido com preço e disponível (RF-LIV-005, DEC-15.3). */
class ConsultarLivroTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @Test
    void rfLiv005_devolveODetalheComPrecoEDisponivel() {
        ConsultarLivro consultar = consulta(detalhe(), Map.of(1L, Dinheiro.de("47.88")),
                Map.of(1L, new SaldoDoLivro(20, 5)));

        LivroConsultado livro = consultar.executar(1L);

        assertThat(livro.precoVenda()).isEqualTo(Dinheiro.de("47.88"));
        assertThat(livro.disponivel()).isEqualTo(15);
        assertThat(livro.livro().isbn()).isEqualTo("9788500010019");
        assertThat(livro.livro().codigoBarras()).isEqualTo("7891000000014");
        assertThat(livro.livro().livro().titulo()).isEqualTo("Dom Casmurro");
    }

    @Test
    void livroSemPrecoFicaSemPrecoEDisponivelZero() {
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(1L, new SaldoDoLivro(20, 0)));
        ConsultarLivro consultar = consulta(detalhe(), Map.of(), saldo);

        LivroConsultado livro = consultar.executar(1L);

        assertThat(livro.precoVenda()).isNull();
        assertThat(livro.disponivel()).isZero();
        assertThat(saldo.chamadas).isZero();
    }

    @Test
    void livroInexistenteOuInativoRespondeNaoEncontrado() {
        ConsultarLivro consultar = consulta(Optional.empty(), Map.of(),
                Map.of(1L, new SaldoDoLivro(20, 0)));

        assertThatThrownBy(() -> consultar.executar(19L))
                .isInstanceOf(ExcecaoDeRecursoNaoEncontrado.class)
                .hasMessage("Livro não encontrado.");
    }

    private static Optional<LivroDetalhe> detalhe() {
        LivroResumo resumo = new LivroResumo(1L, "Dom Casmurro", List.of("Machado de Assis"),
                List.of(new CategoriaDoLivro(1L, "Romance")), "Companhia das Letras", 1899);
        return Optional.of(new LivroDetalhe(resumo, "1ª edição", "9788500010019", 256,
                "Romance clássico.", new BigDecimal("21.00"), new BigDecimal("14.00"),
                new BigDecimal("1.60"), new BigDecimal("0.320"), "7891000000014"));
    }

    private static ConsultarLivro consulta(Optional<LivroDetalhe> detalhe, Map<Long, Dinheiro> valores,
            Map<Long, SaldoDoLivro> saldos) {
        return consulta(detalhe, valores, new SaldoDeEstoqueFake(saldos));
    }

    private static ConsultarLivro consulta(Optional<LivroDetalhe> detalhe, Map<Long, Dinheiro> valores,
            SaldoDeEstoque saldo) {
        CatalogoConsultas catalogo = new CatalogoConsultasFake(detalhe);
        MargemPorLivro margens = livroIds -> filtrar(valores, livroIds, BigDecimal.ZERO);
        MaiorCustoPorLivro custos = livroIds -> filtrar(valores, livroIds, null);
        ConsultarPrecosDeVenda precos = new ConsultarPrecosDeVenda(margens,
                new ConsultarMaiorCusto(custos), new CalcularPrecoVenda());
        ConsultarDisponibilidade disponibilidade = new ConsultarDisponibilidade(saldo,
                new CalcularDisponivel(), () -> AGORA);
        return new ConsultarLivro(catalogo,
                new EnriquecerComPrecoEDisponibilidade(precos, disponibilidade));
    }

    private static Map<Long, BigDecimal> filtrar(Map<Long, Dinheiro> valores, Collection<Long> livroIds,
            BigDecimal padrao) {
        return valores.entrySet().stream()
                .filter(entrada -> livroIds.contains(entrada.getKey()))
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entrada -> padrao != null ? padrao : entrada.getValue().valor()));
    }

    private static final class CatalogoConsultasFake implements CatalogoConsultas {

        private final Optional<LivroDetalhe> detalhe;

        private CatalogoConsultasFake(Optional<LivroDetalhe> detalhe) {
            this.detalhe = detalhe;
        }

        @Override
        public Pagina<LivroResumo> listar(FiltroLivro filtro, Paginacao paginacao) {
            return new Pagina<>(List.of(), 0, 20, 0, 0);
        }

        @Override
        public Optional<LivroDetalhe> buscar(Long id) {
            return detalhe;
        }

        @Override
        public Pagina<CategoriaDoLivro> listarCategorias(Paginacao paginacao) {
            return new Pagina<>(List.of(), 0, 20, 0, 0);
        }
    }

    private static final class SaldoDeEstoqueFake implements SaldoDeEstoque {

        private final Map<Long, SaldoDoLivro> saldos;
        private int chamadas;

        private SaldoDeEstoqueFake(Map<Long, SaldoDoLivro> saldos) {
            this.saldos = saldos;
        }

        @Override
        public Map<Long, SaldoDoLivro> saldos(Collection<Long> livroIds, LocalDateTime agora) {
            chamadas++;
            return saldos.entrySet().stream()
                    .filter(entrada -> livroIds.contains(entrada.getKey()))
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
