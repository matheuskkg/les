package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

/**
 * Remove da resposta do chat os livros que o cliente já comprou quando o tipo é
 * {@link TipoDeResposta#RECOMENDACAO} (RNF-REC-001, DEC-03). Com {@link TipoDeResposta#BUSCA} e
 * {@link TipoDeResposta#DUVIDA} os comprados ficam, porque o cliente pode perguntar de um livro que já tem; com
 * {@link TipoDeResposta#FORA_DO_ESCOPO} a resposta já vem sem livros. Se, depois da remoção, não sobrar nenhum livro e
 * o texto estiver em branco ou for o padrão de "só com livros", ele vira
 * {@link ValidarRespostaDoAssistente#TEXTO_SEM_RESPOSTA}.
 */
@Component
public class RemoverLivrosCompradosDaRecomendacao {

    /**
     * @param resposta  resposta já validada pelo {@link ValidarRespostaDoAssistente}
     * @param comprados ids dos livros que o cliente comprou
     */
    public RespostaDoAssistente remover(RespostaDoAssistente resposta, Collection<Long> comprados) {
        if (resposta.tipo() != TipoDeResposta.RECOMENDACAO) {
            return resposta;
        }
        Set<Long> compradosSet = Set.copyOf(comprados);
        List<Long> citados = new LinkedHashSet<>(resposta.livrosCitados()).stream()
                .filter(id -> !compradosSet.contains(id))
                .toList();
        if (citados.size() == resposta.livrosCitados().size()) {
            return resposta;
        }
        String texto = resposta.texto();
        if (citados.isEmpty() && (texto.isBlank()
                || texto.strip().equals(ValidarRespostaDoAssistente.TEXTO_SO_COM_LIVROS))) {
            texto = ValidarRespostaDoAssistente.TEXTO_SEM_RESPOSTA;
        }
        return new RespostaDoAssistente(resposta.tipo(), texto, citados);
    }
}
