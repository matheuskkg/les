package br.fatec.les.livraria.recomendacao.infraestrutura.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.catalogo.infraestrutura.web.MapeadorDeLivros;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.AvaliarRespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.ConsultarMinhaConversa;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.EnviarMensagemAoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.casodeuso.LimparConversa;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.MensagemExibida;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.AvaliacaoDto;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.MensagemDto;
import br.fatec.les.livraria.recomendacao.infraestrutura.web.dto.NovaMensagemDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Chat do cliente logado com o assistente (RNF-REC-001, DEC-03): a conversa ativa, o envio de mensagens, "Limpar
 * conversa" e a avaliação das respostas. O cliente vem da sessão, e o histórico enviado à IA vem do banco.
 */
@RestController
@RequestMapping("/api/me/assistente")
@RequiredArgsConstructor
public class AssistenteController {

    private final ConsultarMinhaConversa consultarMinhaConversa;
    private final EnviarMensagemAoAssistente enviarMensagem;
    private final LimparConversa limparConversa;
    private final AvaliarRespostaDoAssistente avaliarResposta;
    private final MapeadorDeLivros mapeadorDeLivros;

    @GetMapping("/conversa")
    @Operation(summary = "Mensagens da conversa ativa, da mais antiga à mais recente; vazia sem conversa")
    public List<MensagemDto> conversa() {
        return consultarMinhaConversa.executar().stream().map(this::paraDto).toList();
    }

    @DeleteMapping("/conversa")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Limpa a conversa: a atual é arquivada e a próxima mensagem abre outra")
    public void limpar() {
        limparConversa.executar();
    }

    @PostMapping("/mensagens")
    @Operation(summary = "Envia a mensagem ao assistente e devolve a resposta gravada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resposta do assistente"),
            @ApiResponse(responseCode = "422", description = "Mensagem vazia, longa demais ou acima do limite por "
                    + "minuto", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Nenhum provedor de IA atendeu; a mensagem do "
                    + "cliente fica gravada", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public MensagemDto enviar(@RequestBody NovaMensagemDto mensagem) {
        return paraDto(enviarMensagem.executar(mensagem.texto()));
    }

    @PutMapping("/mensagens/{id}/avaliacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Avalia uma resposta do assistente; a última avaliação vale")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Avaliação gravada"),
            @ApiResponse(responseCode = "400", description = "Avaliação ausente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Resposta inexistente, de outro cliente, de conversa "
                    + "arquivada ou mensagem do próprio cliente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void avaliar(@PathVariable Long id, @RequestBody AvaliacaoDto avaliacao) {
        if (avaliacao.avaliacao() == null) {
            throw new ParametroInvalido("avaliacao", "Informe a avaliação: POSITIVA ou NEGATIVA.");
        }
        avaliarResposta.executar(id, avaliacao.avaliacao());
    }

    private MensagemDto paraDto(MensagemExibida exibida) {
        MensagemDaConversa mensagem = exibida.mensagem();
        return new MensagemDto(mensagem.id(), mensagem.papel(), mensagem.texto(), mensagem.tipo(),
                exibida.livros().stream().map(mapeadorDeLivros::paraDto).toList(), mensagem.avaliacao(),
                mensagem.enviadaEm());
    }
}
