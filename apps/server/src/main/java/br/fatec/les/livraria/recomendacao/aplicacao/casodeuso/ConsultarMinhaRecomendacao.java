package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.aplicacao.AssistenteIndisponivel;
import br.fatec.les.livraria.recomendacao.aplicacao.LerCatalogo;
import br.fatec.les.livraria.recomendacao.aplicacao.MontarPerfilDoCliente;
import br.fatec.les.livraria.recomendacao.aplicacao.RecuperarCandidatos;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.Atendimento;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.ItemExibido;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RecomendacoesRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.regra.LivroAVenda;
import br.fatec.les.livraria.recomendacao.dominio.calculo.FiltrarItensRecomendados;
import br.fatec.les.livraria.recomendacao.dominio.calculo.OrdenarPelaPontuacao;
import br.fatec.les.livraria.recomendacao.dominio.calculo.RecomendacaoAindaValida;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemDaRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Recomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.ConsultarLivrosVendidos;
import br.fatec.les.livraria.vendas.aplicacao.consulta.LivroVendido;
import lombok.RequiredArgsConstructor;

/**
 * "Recomendados para você" do cliente logado (RNF-REC-001, DEC-03). A recomendação mais recente é reaproveitada até
 * a próxima compra ou por 24 horas (1 hora se gravada sem IA, {@link RecomendacaoAindaValida}); passado isso, outra é gerada: o server recupera os candidatos, a cadeia de
 * provedores os ordena e justifica, e só os ids que estavam entre os candidatos são gravados. Se toda a cadeia falhar
 * ou nenhum id da resposta for válido, a recomendação é gravada na ordem da pontuação, sem justificativa e sem
 * chamada associada, e conta como recomendação sem IA nas métricas.
 *
 * <p>Não há transação em volta da chamada à IA, que pode levar segundos: as leituras usam as transações dos casos
 * de uso de cada módulo, e a gravação a do repositório.</p>
 *
 * <p>Na exibição saem os itens avaliados negativamente, os livros inativos ou sem estoque e os comprados depois da
 * geração. Sem itens, a lista volta vazia e a vitrine esconde a seção.</p>
 */
@Component
@RequiredArgsConstructor
public class ConsultarMinhaRecomendacao {

    private final IdentificarCliente identificarCliente;
    private final ConsultarLivrosVendidos consultarLivrosVendidos;
    private final LerCatalogo lerCatalogo;
    private final RecomendacoesRepositorio recomendacoes;
    private final MontarPerfilDoCliente montarPerfil;
    private final RecuperarCandidatos recuperarCandidatos;
    private final CadeiaDeProvedores cadeia;
    private final FiltrarItensRecomendados filtrarItens;
    private final OrdenarPelaPontuacao ordenarPelaPontuacao;
    private final RecomendacaoAindaValida aindaValida;
    private final Relogio relogio;

    public List<ItemExibido> executar() {
        Long clienteId = identificarCliente.executar();
        List<LivroVendido> vendidos = consultarLivrosVendidos.executar();
        List<LivroListado> catalogo = lerCatalogo.ativos();
        List<LivroVendido> doCliente = vendidos.stream()
                .filter(vendido -> vendido.clienteId().equals(clienteId))
                .toList();

        int compras = (int) doCliente.stream().map(LivroVendido::pedidoId).distinct().count();

        Optional<Recomendacao> atual = recomendacoes.maisRecente(clienteId)
                .filter(recomendacao -> aindaValida.valida(recomendacao, relogio.agora(), compras));
        Recomendacao recomendacao = atual.isPresent() ? atual.get() : gerar(clienteId, compras, vendidos, catalogo);
        return exibir(recomendacao, catalogo, doCliente.stream().map(LivroVendido::livroId)
                .collect(Collectors.toSet()));
    }

    private Recomendacao gerar(Long clienteId, int compras, List<LivroVendido> vendidos, List<LivroListado> catalogo) {
        PerfilDoCliente perfil = montarPerfil.montar(clienteId, vendidos, recomendacoes.avaliacoesDoCliente(clienteId));
        List<LivroCandidato> candidatos = recuperarCandidatos.recuperar(clienteId, perfil, vendidos, catalogo);
        if (candidatos.isEmpty()) {
            return new Recomendacao(null, clienteId, null, relogio.agora(), compras, List.of());
        }

        Long chamadaIaId = null;
        List<ItemDaRecomendacao> itens = List.of();
        try {
            Atendimento<RespostaDeRecomendacao> atendimento = cadeia.recomendar(perfil, candidatos);
            itens = filtrarItens.filtrar(atendimento.resposta().itens(), candidatos);
            if (!itens.isEmpty()) {
                chamadaIaId = atendimento.chamadaIaId();
            }
        } catch (AssistenteIndisponivel indisponivel) {
            // Toda a cadeia falhou: a vitrine usa a ordem da pontuação (DEC-03).
        }
        if (itens.isEmpty()) {
            itens = ordenarPelaPontuacao.ordenar(candidatos);
        }
        return recomendacoes.salvar(new Recomendacao(null, clienteId, chamadaIaId, relogio.agora(), compras, itens));
    }

    private static List<ItemExibido> exibir(Recomendacao recomendacao, List<LivroListado> catalogo,
            Set<Long> comprados) {
        Map<Long, LivroListado> porId = catalogo.stream()
                .collect(Collectors.toMap(livro -> livro.livro().id(), Function.identity()));
        return recomendacao.itens().stream()
                .filter(item -> item.id() != null && !item.avaliadoNegativamente())
                .filter(item -> !comprados.contains(item.livroId()))
                .filter(item -> porId.containsKey(item.livroId()) && LivroAVenda.aVenda(porId.get(item.livroId())))
                .sorted(Comparator.comparingInt(ItemDaRecomendacao::posicao))
                .map(item -> new ItemExibido(item.id(), porId.get(item.livroId()), item.justificativa(),
                        item.avaliacao()))
                .toList();
    }
}
