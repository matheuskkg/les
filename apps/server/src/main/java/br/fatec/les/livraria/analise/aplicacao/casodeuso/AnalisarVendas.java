package br.fatec.les.livraria.analise.aplicacao.casodeuso;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.analise.dominio.modelo.AnaliseDeVendas;
import br.fatec.les.livraria.analise.dominio.modelo.CategoriaAnalisada;
import br.fatec.les.livraria.analise.dominio.modelo.PeriodoDeAnalise;
import br.fatec.les.livraria.analise.dominio.modelo.SolicitacaoDeAnalise;
import br.fatec.les.livraria.analise.dominio.modelo.VendaNoMes;
import br.fatec.les.livraria.analise.dominio.regra.CategoriaSelecionada;
import br.fatec.les.livraria.analise.dominio.regra.FimNaoAnteriorAoInicio;
import br.fatec.les.livraria.analise.dominio.regra.PeriodoDeAte24Meses;
import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ListarCategorias;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.CategoriaDoLivro;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarItensVendidos;
import br.fatec.les.livraria.vendas.aplicacao.consulta.ItemVendido;
import br.fatec.les.livraria.vendas.dominio.pedido.CategoriaNoPedido;
import lombok.RequiredArgsConstructor;

/**
 * Histórico de vendas por categoria e mês para o gráfico e a exportação (RF-ANA-001 a RF-ANA-004, RN-ANA-001 a
 * RN-ANA-004). Os itens vendidos vêm do módulo de vendas e os nomes das categorias, do catálogo.
 */
@Component
@RequiredArgsConstructor
public class AnalisarVendas {

    private final FimNaoAnteriorAoInicio fimNaoAnteriorAoInicio;
    private final PeriodoDeAte24Meses periodoDeAte24Meses;
    private final CategoriaSelecionada categoriaSelecionada;
    private final ListarCategorias listarCategorias;
    private final ConsultarItensVendidos consultarItensVendidos;

    /**
     * @param solicitacao meses e categorias pedidos
     * @return uma linha por categoria, na ordem informada, com um valor por mês
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se o período ou a seleção violarem as
     *         regras (HTTP 422)
     * @throws ParametroInvalido se alguma categoria não existir (HTTP 400)
     */
    @Transactional(readOnly = true)
    public AnaliseDeVendas executar(SolicitacaoDeAnalise solicitacao) {
        Objects.requireNonNull(solicitacao, "solicitacao");
        Violacoes.verificar(solicitacao, fimNaoAnteriorAoInicio, periodoDeAte24Meses, categoriaSelecionada);
        List<CategoriaAnalisada> categorias = categoriasSelecionadas(solicitacao.categorias());
        PeriodoDeAnalise periodo = new PeriodoDeAnalise(solicitacao.inicio(), solicitacao.fim());
        List<VendaNoMes> vendas = consultarItensVendidos
                .executar(periodo.primeiroInstante(), periodo.fimExclusivo()).stream()
                .map(AnalisarVendas::vendaNoMes)
                .toList();
        return AnaliseDeVendas.calcular(periodo, categorias, vendas);
    }

    private List<CategoriaAnalisada> categoriasSelecionadas(List<Long> ids) {
        Map<Long, String> catalogo = new LinkedHashMap<>();
        Pagina<CategoriaDoLivro> pagina;
        int numero = 0;
        do {
            pagina = listarCategorias.executar(new Paginacao(numero++, Paginacao.TAMANHO_MAXIMO, List.of()));
            pagina.itens().forEach(categoria -> catalogo.put(categoria.id(), categoria.nome()));
        } while (numero < pagina.totalPaginas());
        List<CategoriaAnalisada> categorias = new ArrayList<>();
        for (Long id : ids) {
            String nome = catalogo.get(id);
            if (nome == null) {
                throw new ParametroInvalido("categoria", "A categoria %d não existe.".formatted(id));
            }
            categorias.add(new CategoriaAnalisada(id, nome));
        }
        return categorias;
    }

    private static VendaNoMes vendaNoMes(ItemVendido item) {
        return new VendaNoMes(YearMonth.from(item.realizadoEm()),
                item.categorias().stream().map(CategoriaNoPedido::id).collect(Collectors.toSet()), item.valor());
    }
}
