package br.fatec.les.livraria.recomendacao.dominio.calculo;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Remove da resposta do chat os livros que o cliente já comprou quando o tipo é {@link TipoDeResposta#RECOMENDACAO}
 * (RNF-REC-001, DEC-03). Com {@link TipoDeResposta#BUSCA} e {@link TipoDeResposta#DUVIDA} os comprados ficam, porque o
 * cliente pode perguntar de um livro que já tem; com {@link TipoDeResposta#FORA_DO_ESCOPO} a resposta já vem sem
 * livros. Quando a remoção esvazia os citados, o texto do modelo, que apresentava os livros removidos, já não serve:
 * quem completa a resposta com os livros que o cliente ainda não tem é {@link IndicarLivrosQueOClienteNaoTem}, que o
 * caso de uso chama nesse caso (caso {@code gem-07a}, achado 7 da rodada de testes do chat de 22/11; antes de
 * ALT-01-rec08-so-novos o texto era trocado aqui por um texto fixo sem livros, mesmo quando o catálogo tinha livros à
 * venda que o cliente não tinha).
 */
@Component
public class RemoverLivrosCompradosDaRecomendacao {

    /**
     * @param resposta resposta já validada pelo {@link ValidarRespostaDoAssistente}
     * @param comprados ids dos livros que o cliente comprou
     */
    public RespostaDoAssistente remover(RespostaDoAssistente resposta, Collection<Long> comprados) {
        if (resposta.tipo() != TipoDeResposta.RECOMENDACAO) {
            return resposta;
        }
        Set<Long> compradosSet = Set.copyOf(comprados);
        List<Long> citados =
                new LinkedHashSet<>(resposta.livrosCitados())
                        .stream().filter(id -> !compradosSet.contains(id)).toList();
        if (citados.size() == resposta.livrosCitados().size()) {
            return resposta;
        }
        return new RespostaDoAssistente(resposta.tipo(), resposta.texto(), citados);
    }
}
