package br.fatec.les.livraria.recomendacao.dominio.calculo;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Completa a recomendação do chat que perdeu todos os livros citados, porque o modelo citou só livros que o cliente já
 * comprou (RNF-REC-001, DEC-03): em vez de dizer que o cliente já tem o que seria indicado, indica os livros que ele
 * ainda não tem, com texto fixo curto e sem ids — os livros chegam ao cliente como cartões pelo campo de citados. Os
 * candidatos são os mesmos da vitrine e da tool {@code recomendarParaMim}, já sem os comprados e sem os avaliados
 * negativamente, e a resposta indica até {@value ValidarRespostaDoAssistente#LIMITE_DE_LIVROS} deles. Sem candidato
 * nenhum — o cliente já tem ou avaliou tudo à venda —, a resposta fica sem livros, com texto fixo que volta a poder
 * afirmar que ele já tem o que seria indicado (caso {@code gem-07a}, achado 7 da rodada de testes do chat de 22/11;
 * antes, em ALT-10, o texto era fixo também com livros à venda que o cliente não tinha).
 */
@Component
public class IndicarLivrosQueOClienteNaoTem {

    public static final String TEXTO_COM_LIVROS_NOVOS =
            "Separei estes livros, que você ainda não tem. Se preferir outro gênero ou autor, é só pedir.";
    public static final String TEXTO_SEM_LIVROS_NOVOS =
            "Você já tem ou já avaliou tudo o que eu indicaria. Quando chegarem novidades, volto a sugerir.";

    /**
     * @param resposta recomendação já sem os livros comprados; outros tipos e recomendações que ainda citam livros
     *     ficam como estão
     * @param candidatos ids dos livros que o cliente ainda não tem, na ordem da pontuação da vitrine
     */
    public RespostaDoAssistente indicar(RespostaDoAssistente resposta, Collection<Long> candidatos) {
        if (resposta.tipo() != TipoDeResposta.RECOMENDACAO || !resposta.livrosCitados().isEmpty()) {
            return resposta;
        }
        List<Long> novos = candidatos.stream().limit(ValidarRespostaDoAssistente.LIMITE_DE_LIVROS).toList();
        return new RespostaDoAssistente(
                TipoDeResposta.RECOMENDACAO, novos.isEmpty() ? TEXTO_SEM_LIVROS_NOVOS : TEXTO_COM_LIVROS_NOVOS, novos);
    }
}
