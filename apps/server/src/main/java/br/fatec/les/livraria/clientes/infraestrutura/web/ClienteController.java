package br.fatec.les.livraria.clientes.infraestrutura.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.CadastrarCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.CadastroDeCliente;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.CartaoDoCadastro;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.ClienteCriado;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.EnderecoDoCadastro;
import br.fatec.les.livraria.clientes.infraestrutura.web.dto.TelefoneDoCadastro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * Autocadastro de cliente (RF-CLI-001, DEC-07, DEC-10). Rota pública já liberada na configuração de segurança. A
 * resposta 201 traz o código do cliente e {@code Location: /api/me}, onde o cliente consulta os próprios dados.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final CadastrarCliente cadastrarCliente;

    @PostMapping
    @Operation(summary = "Cadastra um cliente e o usuário de login dele")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ClienteCriado.class))),
            @ApiResponse(responseCode = "422", description = "Dados do cadastro violam regras de negócio",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "400", description = "JSON malformado ou valor de enum inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ClienteCriado> cadastrar(@RequestBody CadastroDeCliente entrada) {
        Long id = cadastrarCliente.executar(paraComando(entrada));
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/me"))
                .body(new ClienteCriado(id));
    }

    private static NovoCliente paraComando(CadastroDeCliente entrada) {
        TelefoneDoCadastro telefone = entrada.telefone();
        return new NovoCliente(
                entrada.nome(),
                entrada.genero(),
                entrada.dataNascimento(),
                entrada.cpf(),
                entrada.email(),
                telefone == null ? null : telefone.tipo(),
                telefone == null ? null : telefone.ddd(),
                telefone == null ? null : telefone.numero(),
                entrada.senha(),
                entrada.confirmacaoSenha(),
                enderecos(entrada.enderecos()),
                cartoes(entrada.cartoes()));
    }

    private static List<NovoEndereco> enderecos(List<EnderecoDoCadastro> enderecos) {
        if (enderecos == null) {
            return null;
        }
        return enderecos.stream().map(ClienteController::paraEndereco).toList();
    }

    private static NovoEndereco paraEndereco(EnderecoDoCadastro endereco) {
        if (endereco == null) {
            return null;
        }
        return new NovoEndereco(endereco.nomeCurto(), endereco.tipoEndereco(), endereco.tipoLogradouro(),
                endereco.logradouro(), endereco.numero(), endereco.bairro(), endereco.cep(), endereco.cidade(),
                endereco.uf(), endereco.observacoes());
    }

    private static List<NovoCartao> cartoes(List<CartaoDoCadastro> cartoes) {
        if (cartoes == null) {
            return null;
        }
        return cartoes.stream().map(ClienteController::paraCartao).toList();
    }

    private static NovoCartao paraCartao(CartaoDoCadastro cartao) {
        if (cartao == null) {
            return null;
        }
        return new NovoCartao(cartao.numero(), cartao.nomeImpresso(), cartao.bandeiraId(), cartao.validade());
    }
}
