package br.fatec.les.livraria.clientes.infraestrutura.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.AdicionarCartao;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.AlteracaoDeCartao;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.AlterarCartao;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ListarCartoes;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.MarcarCartaoPreferencial;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.RemoverCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.CartaoDaResposta;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.CartaoDoCadastro;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Cartões do cliente autenticado (RF-CLI-007, RN-CLI-004, RN-CLI-005, DEC-06, DEC-07, DEC-10). O número sai sempre
 * mascarado e o código de segurança não é recebido nem gravado. O cliente nunca informa o próprio id: ele vem da
 * sessão, então cartão de outro cliente responde 404.
 */
@RestController
@RequestMapping("/api/me/cartoes")
@RequiredArgsConstructor
public class CartaoController {

    private final ListarCartoes listarCartoes;
    private final AdicionarCartao adicionarCartao;
    private final AlterarCartao alterarCartao;
    private final MarcarCartaoPreferencial marcarCartaoPreferencial;
    private final RemoverCartao removerCartao;

    @GetMapping
    @Operation(summary = "Lista os cartões do cliente autenticado, com o número mascarado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cartões do cliente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CartaoDaResposta.class)))),
            @ApiResponse(responseCode = "401", description = "Sem sessão válida",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<CartaoDaResposta> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return listarCartoes.executar(usuario.usuarioId()).stream().map(CartaoDaResposta::de).toList();
    }

    @PostMapping
    @Operation(summary = "Adiciona um cartão ao cadastro do cliente autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cartão cadastrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartaoDaResposta.class))),
            @ApiResponse(responseCode = "401", description = "Sem sessão válida",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dados do cartão violam regras de negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CartaoDaResposta> adicionar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody CartaoDoCadastro entrada) {
        Cartao cartao = adicionarCartao.executar(usuario.usuarioId(), paraComando(entrada));
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/me/cartoes/" + cartao.id()))
                .body(CartaoDaResposta.de(cartao));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Altera um cartão do cliente autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cartão alterado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartaoDaResposta.class))),
            @ApiResponse(responseCode = "401", description = "Sem sessão válida",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Cartão inexistente ou de outro cliente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Dados do cartão violam regras de negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public CartaoDaResposta alterar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @RequestBody CartaoDoCadastro entrada) {
        return CartaoDaResposta.de(
                alterarCartao.executar(usuario.usuarioId(), new AlteracaoDeCartao(id, paraComando(entrada))));
    }

    @PostMapping("/{id}/preferencial")
    @Operation(summary = "Marca o cartão como o único preferencial")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cartão marcado como preferencial"),
            @ApiResponse(responseCode = "401", description = "Sem sessão válida",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Cartão inexistente ou de outro cliente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> marcarPreferencial(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        marcarCartaoPreferencial.executar(usuario.usuarioId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um cartão do cliente autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cartão excluído"),
            @ApiResponse(responseCode = "401", description = "Sem sessão válida",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Cartão inexistente ou de outro cliente",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> remover(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        removerCartao.executar(usuario.usuarioId(), id);
        return ResponseEntity.noContent().build();
    }

    private static NovoCartao paraComando(CartaoDoCadastro entrada) {
        if (entrada == null) {
            return null;
        }
        return new NovoCartao(entrada.numero(), entrada.nomeImpresso(), entrada.validade());
    }
}
