package br.fatec.les.livraria.recomendacao.dominio.calculo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

/**
 * Valida a resposta do assistente antes de gravar (RNF-REC-001, DEC-03): só ficam os livros citados que existem,
 * estão ativos e têm estoque, cada um uma vez e na ordem da resposta, até {@value #LIMITE_DE_LIVROS}. Com
 * {@link TipoDeResposta#FORA_DO_ESCOPO}, o texto do modelo é descartado e trocado por {@link #TEXTO_FORA_DO_ESCOPO},
 * sem livros. Texto em branco nos demais tipos vira um texto padrão.
 */
@Component
public class ValidarRespostaDoAssistente {

    public static final String TEXTO_FORA_DO_ESCOPO =
            "Posso ajudar só com livros e compras da livraria. Pergunte sobre um título, autor ou gênero.";
    public static final String TEXTO_SEM_RESPOSTA = "Não consegui responder. Tente perguntar de outro jeito.";
    public static final String TEXTO_SO_COM_LIVROS = "Veja estes livros.";
    public static final int LIMITE_DE_LIVROS = 6;

    /**
     * @param resposta resposta do provedor
     * @param aVenda   ids dos livros ativos e com estoque
     */
    public RespostaDoAssistente validar(RespostaDoAssistente resposta, Collection<Long> aVenda) {
        if (resposta.tipo() == TipoDeResposta.FORA_DO_ESCOPO) {
            return new RespostaDoAssistente(TipoDeResposta.FORA_DO_ESCOPO, TEXTO_FORA_DO_ESCOPO, List.of());
        }
        Set<Long> validos = Set.copyOf(aVenda);
        List<Long> citados = new LinkedHashSet<>(resposta.livrosCitados()).stream()
                .filter(id -> id != null && validos.contains(id))
                .limit(LIMITE_DE_LIVROS)
                .toList();
        String texto = resposta.texto().strip();
        if (texto.isEmpty()) {
            texto = citados.isEmpty() ? TEXTO_SEM_RESPOSTA : TEXTO_SO_COM_LIVROS;
        }
        return new RespostaDoAssistente(resposta.tipo(), texto, citados);
    }
}
