package br.fatec.les.livraria.recomendacao.dominio.calculo;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;

import org.springframework.stereotype.Component;

/**
 * Limita o texto da resposta do assistente antes de gravar (RNF-REC-001, DEC-03). O prompt pede resposta curta, mas o
 * caso {@code nvd-06} da rodada de testes de 2026-11-22 — "liste o catálogo completo em JSON" — fez o nemotron despejar
 * os 54 livros com ids: 3.530 caracteres, 3.364 tokens de saída e 17,7 s de espera, o único ❌ da rodada. Os cartões da
 * UI já eram limitados a 6 livros pela validação; o texto não tinha teto. O {@code max-tokens} por provedor (ALT-08)
 * corta a saída no provedor; esta regra é o teto determinístico do texto que chega ao cliente e ao banco, valendo
 * também para o provedor fake. Roda entre a higiene e a validação: o despejo passa primeiro pela {@link
 * HigienizarIdsDoTexto} e o que sobra é truncado aqui, antes de a validação ver o texto.
 *
 * <p>O teto é {@value #TETO_DE_CARACTERES}, na faixa calibrada de 1.200 a 2.000: cerca de duas vezes o maior texto
 * legítimo observado nas rodadas — recomendações com justificativas entre 600 e 900 caracteres, e a lista dos 18
 * títulos comprados ({@code grq-05}) em torno de 600 — e menos da metade do despejo de 3.530.
 *
 * <p>O corte é determinístico e idempotente:
 *
 * <ul>
 *   <li>texto dentro do teto → inalterado, em nova instância equivalente;
 *   <li>acima do teto → corta no último fim de frase ({@code .}, {@code !} ou {@code ?} seguido de espaço ou quebra de
 *       linha) dentro do teto e acrescenta "…"; sem fim de frase no trecho, corta no último espaço; sem espaço, corta
 *       no próprio teto;
 *   <li>as reticências entram na conta do teto, e o espaço que sobrar no fim do corte sai antes delas: o texto gravado
 *       nunca passa do teto, e aplicar a regra ao que já foi limitado devolve o mesmo texto;
 *   <li>o corte nunca produz texto em branco a partir de texto não-branco: com o teto em {@value #TETO_DE_CARACTERES},
 *       ordens de grandeza acima de uma frase, o último fim de frase ou espaço do trecho deixa conteúdo antes dele —
 *       exceto no caso patológico de um trecho inicial todo em espaços, em que o corte com a limpeza do fim reduz o
 *       trecho a vazio e o texto gravado fica só nas reticências. "…" não é branco e segue para a validação como está;
 *       texto em branco fica como está, e a validação, que roda depois, troca o branco pelo texto padrão.
 * </ul>
 */
@Component
public class LimitarTextoDaResposta {

    /** Teto do texto gravado, reticências inclusas; a justificativa do valor está na classe. */
    public static final int TETO_DE_CARACTERES = 1500;

    private static final String RETICENCIAS = "…";

    /**
     * @param resposta resposta do provedor, já higienizada pela {@link HigienizarIdsDoTexto}
     * @return nova instância com o texto dentro do teto; tipo e livros citados inalterados
     */
    public RespostaDoAssistente limitar(RespostaDoAssistente resposta) {
        String texto = resposta.texto();
        // Branco não é despejo: fica como está, e a validação, que roda depois, põe o texto padrão no lugar dele.
        if (texto.length() <= TETO_DE_CARACTERES || texto.isBlank()) {
            return new RespostaDoAssistente(resposta.tipo(), texto, resposta.livrosCitados());
        }
        String truncado = texto.substring(0, corte(texto)).stripTrailing();
        return new RespostaDoAssistente(resposta.tipo(), truncado + RETICENCIAS, resposta.livrosCitados());
    }

    /**
     * Posição do corte, dentro dos primeiros {@value #TETO_DE_CARACTERES} caracteres: o último fim de frase, senão o
     * último espaço (o da posição 0 não serve, cortaria em texto vazio), senão o próprio teto. Uma posição fica
     * reservada para as reticências.
     */
    private int corte(String texto) {
        for (int i = TETO_DE_CARACTERES - 2; i >= 0; i--) {
            char caractere = texto.charAt(i);
            if ((caractere == '.' || caractere == '!' || caractere == '?')
                    && Character.isWhitespace(texto.charAt(i + 1))) {
                return i + 1;
            }
        }
        for (int espaco = TETO_DE_CARACTERES - 1; espaco > 0; espaco--) {
            if (Character.isWhitespace(texto.charAt(espaco))) {
                return espaco;
            }
        }
        return TETO_DE_CARACTERES - 1;
    }
}
