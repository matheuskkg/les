package br.fatec.les.livraria.recomendacao.dominio.calculo;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Higieniza o texto da resposta do assistente antes de gravar (RNF-REC-001, DEC-03). Modelos às vezes escrevem ids
 * internos no texto que o cliente lê — "recomendo "O Mapa das Estrelas" (id 32)" — ou despejam o catálogo em JSON com
 * pares como "id": 12. O prompt do chat manda deixar os ids só no campo próprio da resposta, mas o comportamento do
 * modelo é probabilístico: esta regra é a garantia server-side do caso mecânico. Roda antes da validação: um texto que
 * a higiene reduz a vazio, porque era só anotação, ganha na validação o texto padrão.
 *
 * <ul>
 *   <li>anotações de id entre parênteses ou colchetes, com só o id dentro — "(id 32)", "(id: 8)", "[ID 52]" — saem, com
 *       qualquer caixa e espaçamento;
 *   <li>pares de JSON, como "id": 12, saem com a vírgula que sobrar, esteja ela antes ou depois;
 *   <li>espaços dobrados, espaço antes de pontuação e espaço no fim da linha, que a remoção deixa, são normalizados.
 * </ul>
 *
 * <p><b>Limite documentado:</b> só as anotações mecânicas saem. O eco do que o próprio cliente digitou — "não
 * encontramos nenhum livro com o ID 999" — e a prosa do modelo — "o id interno de "O Hobbit" é 5" — ficam como estão,
 * porque não são anotações entre parênteses/colchetes nem pares de JSON; esses casos são inibidos pela instrução do
 * prompt, não por esta regra. A aplicação é idempotente: higienizar duas vezes dá o mesmo texto de higienizar uma.
 */
@Component
public class HigienizarIdsDoTexto {

    /** "(id 32)", "(id: 8)", "[ID 52]": delimitações com só o id dentro, em qualquer caixa e espaçamento. */
    private static final Pattern ANOTACAO_DE_ID =
            Pattern.compile("[\\(\\[]\\s*[iI][dD]\\s*[:\\-]?\\s*\\d+\\s*[\\)\\]]");

    /** Par de JSON com a vírgula depois, como "id": 12, com os espaços que vierem em seguida. */
    private static final Pattern PAR_DE_JSON_COM_VIRGULA_DEPOIS =
            Pattern.compile("[\"'][iI][dD][\"']\\s*:\\s*\\d+\\s*,\\s*");

    /** Par de JSON no fim da lista, com a vírgula de antes: {"titulo": "O Hobbit", "id": 5}. */
    private static final Pattern PAR_DE_JSON_COM_VIRGULA_ANTES =
            Pattern.compile("\\s*,\\s*[\"'][iI][dD][\"']\\s*:\\s*\\d+");

    /** Par de JSON sem vírgula, como o único campo do objeto. */
    private static final Pattern PAR_DE_JSON_SOZINHO = Pattern.compile("[\"'][iI][dD][\"']\\s*:\\s*\\d+");

    /** Espaços dobrados que a remoção deixa no meio da linha; a indentação original da linha fica como está. */
    private static final Pattern ESPACOS_DOBRADOS = Pattern.compile("(?<=\\S) {2,}");

    private static final Pattern ESPACO_ANTES_DA_PONTUACAO = Pattern.compile(" +([.,;:!?])");
    private static final Pattern ESPACO_NO_FIM_DA_LINHA = Pattern.compile(" +(?=\r|\n)");

    /**
     * @param resposta resposta crua do provedor, antes da validação
     * @return nova instância com o texto higienizado; tipo e livros citados inalterados
     */
    public RespostaDoAssistente higienizar(RespostaDoAssistente resposta) {
        String texto = resposta.texto();
        for (Pattern padrao :
                List.of(
                        ANOTACAO_DE_ID,
                        PAR_DE_JSON_COM_VIRGULA_DEPOIS,
                        PAR_DE_JSON_COM_VIRGULA_ANTES,
                        PAR_DE_JSON_SOZINHO)) {
            texto = padrao.matcher(texto).replaceAll("");
        }
        texto = ESPACO_NO_FIM_DA_LINHA.matcher(texto).replaceAll("");
        texto = ESPACOS_DOBRADOS.matcher(texto).replaceAll(" ");
        texto = ESPACO_ANTES_DA_PONTUACAO.matcher(texto).replaceAll("$1");
        return new RespostaDoAssistente(resposta.tipo(), texto.strip(), resposta.livrosCitados());
    }
}
