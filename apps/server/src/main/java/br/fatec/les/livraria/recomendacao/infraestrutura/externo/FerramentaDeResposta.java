package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

/**
 * Tool de resposta final do chat (RNF-REC-001, DEC-03). Com o formato {@code ferramenta}, o modelo entrega a
 * resposta chamando {@code responder} com o tipo, o texto e os livros citados; o adapter lê os argumentos e encerra
 * o loop, sem executar o método. {@code returnDirect} marca a tool como resposta final para o Spring AI.
 */
class FerramentaDeResposta {

    static final String NOME = "responder";

    @Tool(name = NOME, returnDirect = true,
            description = "Entrega a resposta final ao cliente. Use sempre que for responder, sem escrever a resposta "
                    + "no texto da mensagem.")
    String responder(
            @ToolParam(description = "Tipo da resposta: RECOMENDACAO, BUSCA, DUVIDA ou FORA_DO_ESCOPO")
            TipoDeResposta tipo,
            @ToolParam(description = "Texto da resposta em português, curto e cordial") String texto,
            @ToolParam(description = "Ids dos livros citados na resposta", required = false)
            @Nullable List<Long> livrosCitados) {
        return texto;
    }
}
