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
 * livros. Se, depois da remoção, não sobrar nenhum livro, o texto do modelo, que apresentava os livros removidos, vira
 * {@link #TEXTO_SO_COMPRADOS}.
 */
@Component
public class RemoverLivrosCompradosDaRecomendacao {

    public static final String TEXTO_SO_COMPRADOS =
            "Você já tem os livros que eu indicaria. Quer sugestões de outro gênero ou autor?";

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
        String texto = citados.isEmpty() ? TEXTO_SO_COMPRADOS : resposta.texto();
        return new RespostaDoAssistente(resposta.tipo(), texto, citados);
    }
}
