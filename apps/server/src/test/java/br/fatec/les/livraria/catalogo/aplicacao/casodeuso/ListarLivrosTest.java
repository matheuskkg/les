package br.fatec.les.livraria.catalogo.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.FiltroLivro;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroDetalhe;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroResumo;
import br.fatec.les.livraria.catalogo.aplicacao.porta.CatalogoConsultas;
import br.fatec.les.livraria.catalogo.aplicacao.porta.MargemPorLivro;
import br.fatec.les.livraria.catalogo.dominio.calculo.CalcularPrecoVenda;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarDisponibilidade;
import br.fatec.les.livraria.estoque.aplicacao.casodeuso.ConsultarMaiorCusto;
import br.fatec.les.livraria.estoque.aplicacao.porta.MaiorCustoPorLivro;
import br.fatec.les.livraria.estoque.aplicacao.porta.SaldoDeEstoque;
import br.fatec.les.livraria.estoque.dominio.calculo.CalcularDisponivel;

/** Listagem da vitrine enriquecida com preço e disponível em lote (RF-LIV-005, DEC-15.3). */
class ListarLivrosTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @Test
    void rfLiv005_enriqueceAPaginaComPrecoEDisponivelEmLote() {
        CatalogoConsultasFake catalogo = new CatalogoConsultasFake(paginaComDoisLivros());
        ConsultarPrecosDeVenda precos = precos(Map.of(1L, Dinheiro.de("47.88")));
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(1L, 20, 2L, 7), Map.of(1L, 5, 2L, 2));
        ListarLivros listar = new ListarLivros(catalogo, precos, disponibilidade(saldo));

        Pagina<LivroListado> pagina = listar.executar(new FiltroLivro(null, null, null, null, null, null,
                null, null, null, null, null, null), Paginacao.padrao());

        assertThat(pagina.itens()).hasSize(2);
        assertThat(pagina.itens().get(0).precoVenda()).isEqualTo(Dinheiro.de("47.88"));
        assertThat(pagina.itens().get(0).disponivel()).isEqualTo(15);
        assertThat(pagina.itens().get(1).precoVenda()).isNull();
        assertThat(pagina.itens().get(1).disponivel()).isZero();
        assertThat(saldo.idsRecebidos).containsExactly(1L, 2L);
    }

    @Test
    void paginaVaziaNaoConsultaPrecoNemDisponibilidade() {
        CatalogoConsultasFake catalogo = new CatalogoConsultasFake(
                new Pagina<>(List.of(), 0, 20, 0, 0));
        SaldoDeEstoqueFake saldo = new SaldoDeEstoqueFake(Map.of(), Map.of());
        ListarLivros listar = new ListarLivros(catalogo, precos(Map.of()), disponibilidade(saldo));

        Pagina<LivroListado> pagina = listar.executar(new FiltroLivro(null, null, null, null, null, null,
                null, null, null, null, null, null), Paginacao.padrao());

        assertThat(pagina.itens()).isEmpty();
        assertThat(saldo.chamadas).isZero();
    }

    private static Pagina<LivroResumo> paginaComDoisLivros() {
        LivroResumo primeiro = new LivroResumo(1L, "Dom Casmurro", List.of("Machado de Assis"),
                List.of(new CategoriaDoLivro(1L, "Romance")), "Companhia das Letras", 1899);
        LivroResumo segundo = new LivroResumo(2L, "Contos Brasileiros", List.of("Machado de Assis"),
                List.of(new CategoriaDoLivro(1L, "Romance")), "Companhia das Letras", 2020);
        return new Pagina<>(List.of(primeiro, segundo), 0, 20, 2, 1);
    }

    private static ConsultarPrecosDeVenda precos(Map<Long, Dinheiro> valores) {
        MargemPorLivro margens = livroIds -> filtrar(valores, livroIds, BigDecimal.ZERO);
        MaiorCustoPorLivro custos = livroIds -> filtrar(valores, livroIds, null);
        return new ConsultarPrecosDeVenda(margens, new ConsultarMaiorCusto(custos),
                new CalcularPrecoVenda());
    }

    private static Map<Long, BigDecimal> filtrar(Map<Long, Dinheiro> valores, Collection<Long> livroIds,
            BigDecimal padrao) {
        return valores.entrySet().stream()
                .filter(entrada -> livroIds.contains(entrada.getKey()))
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entrada -> padrao != null ? padrao : entrada.getValue().valor()));
    }

    private static ConsultarDisponibilidade disponibilidade(SaldoDeEstoque saldo) {
        return new ConsultarDisponibilidade(saldo, new CalcularDisponivel(), () -> AGORA);
    }

    private static final class CatalogoConsultasFake implements CatalogoConsultas {

        private final Pagina<LivroResumo> pagina;

        private CatalogoConsultasFake(Pagina<LivroResumo> pagina) {
            this.pagina = pagina;
        }

        @Override
        public Pagina<LivroResumo> listar(FiltroLivro filtro, Paginacao paginacao) {
            return pagina;
        }

        @Override
        public Optional<LivroDetalhe> buscar(Long id) {
            return Optional.empty();
        }

        @Override
        public List<CategoriaDoLivro> listarCategorias() {
            return List.of();
        }
    }

    private static final class SaldoDeEstoqueFake implements SaldoDeEstoque {

        private final Map<Long, Integer> quantidades;
        private final Map<Long, Integer> reservas;
        private final List<Long> idsRecebidos = new ArrayList<>();
        private int chamadas;

        private SaldoDeEstoqueFake(Map<Long, Integer> quantidades, Map<Long, Integer> reservas) {
            this.quantidades = quantidades;
            this.reservas = reservas;
        }

        @Override
        public Map<Long, Integer> quantidadesEmEstoque(Collection<Long> livroIds) {
            chamadas++;
            idsRecebidos.addAll(livroIds);
            return filtrar(quantidades, livroIds);
        }

        @Override
        public Map<Long, Integer> reservasAtivas(Collection<Long> livroIds, LocalDateTime agora) {
            chamadas++;
            return filtrar(reservas, livroIds);
        }

        private static Map<Long, Integer> filtrar(Map<Long, Integer> valores, Collection<Long> livroIds) {
            return valores.entrySet().stream()
                    .filter(entrada -> livroIds.contains(entrada.getKey()))
                    .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }
}
