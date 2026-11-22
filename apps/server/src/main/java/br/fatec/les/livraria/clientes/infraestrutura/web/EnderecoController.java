package br.fatec.les.livraria.clientes.infraestrutura.web;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.AdicionarEndereco;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.AlteracaoDeEndereco;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.AlterarEndereco;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ListarEnderecos;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.RemoverEndereco;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.EnderecoDaResposta;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.EnderecoDoCadastro;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAutenticado;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import lombok.RequiredArgsConstructor;

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

import java.net.URI;
import java.util.List;

/**
 * Endereços do cliente autenticado (RF-CLI-006, RNF-CLI-004, RN-CLI-003, DEC-07, DEC-10). O cliente nunca informa o
 * próprio id: ele vem da sessão, então endereço de outro cliente responde 404. A lista é pequena e limitada pelos
 * endereços do cliente, por isso não é paginada.
 */
@RestController
@RequestMapping("/api/me/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final ListarEnderecos listarEnderecos;
    private final AdicionarEndereco adicionarEndereco;
    private final AlterarEndereco alterarEndereco;
    private final RemoverEndereco removerEndereco;

    @GetMapping
    @Operation(summary = "Lista os endereços do cliente autenticado")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Endereços do cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                array = @ArraySchema(schema = @Schema(implementation = EnderecoDaResposta.class)))),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<EnderecoDaResposta> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return listarEnderecos.executar(usuario.usuarioId()).stream().map(EnderecoDaResposta::de).toList();
    }

    @PostMapping
    @Operation(summary = "Adiciona um endereço ao cadastro do cliente autenticado")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "Endereço cadastrado",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = EnderecoDaResposta.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Dados do endereço violam regras de negócio",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EnderecoDaResposta> adicionar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody EnderecoDoCadastro entrada) {
        Endereco endereco = adicionarEndereco.executar(usuario.usuarioId(), paraComando(entrada));
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/me/enderecos/" + endereco.id()))
                .body(EnderecoDaResposta.de(endereco));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Altera um endereço do cliente autenticado")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Endereço alterado",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = EnderecoDaResposta.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Endereço inexistente ou de outro cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Dados do endereço violam regras de negócio",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EnderecoDaResposta alterar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @RequestBody EnderecoDoCadastro entrada) {
        return EnderecoDaResposta.de(
                alterarEndereco.executar(usuario.usuarioId(), new AlteracaoDeEndereco(id, paraComando(entrada))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um endereço do cliente autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Endereço excluído"),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Endereço inexistente ou de outro cliente",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Tentativa de excluir o único endereço",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> remover(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Long id) {
        removerEndereco.executar(usuario.usuarioId(), id);
        return ResponseEntity.noContent().build();
    }

    private static NovoEndereco paraComando(EnderecoDoCadastro entrada) {
        if (entrada == null) {
            return null;
        }
        return new NovoEndereco(
                entrada.nomeCurto(),
                entrada.tipoEndereco(),
                entrada.tipoLogradouro(),
                entrada.logradouro(),
                entrada.numero(),
                entrada.bairro(),
                entrada.cep(),
                entrada.cidade(),
                entrada.uf(),
                entrada.observacoes());
    }
}
