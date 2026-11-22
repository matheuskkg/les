package br.fatec.les.livraria.acesso.infraestrutura.web;

import br.fatec.les.livraria.acesso.aplicacao.CredenciaisInvalidas;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.Autenticar;
import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ConsultarNomeDeExibicao;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.EmissorDeToken;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.IdentidadeDoLogout;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.PrincipalDaSessao;
import br.fatec.les.livraria.acesso.infraestrutura.seguranca.TokenDeSessao;
import br.fatec.les.livraria.acesso.infraestrutura.web.dto.EntradaDeLogin;
import br.fatec.les.livraria.acesso.infraestrutura.web.dto.Sessao;
import br.fatec.les.livraria.notificacoes.aplicacao.casodeuso.EncerrarCanais;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rotas de sessão (DEC-08, DEC-10): login por e-mail ou telefone, consulta do usuário logado e logout. O token vai no
 * cookie {@code sessao}; as rotas são públicas, mas o login e o logout exigem o token CSRF como toda escrita.
 */
@RestController
@RequestMapping("/api/sessao")
@RequiredArgsConstructor
public class SessaoController {

    private static final String PREFIXO_DE_AUTORIDADE = "ROLE_";

    private final Autenticar autenticar;
    private final EmissorDeToken emissor;
    private final CsrfTokenRepository csrf;
    private final EncerrarCanais encerrarCanais;
    private final IdentidadeDoLogout identidadeDoLogout;
    private final ConsultarNomeDeExibicao consultarNomeDeExibicao;

    @PostMapping
    @Operation(summary = "Entra no sistema e grava o cookie de sessão")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Sessão aberta",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = Sessao.class))),
        @ApiResponse(
                responseCode = "400",
                description = "Login ou senha em branco",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Login ou senha inválidos",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Sessao> entrar(
            @Valid @RequestBody EntradaDeLogin entrada, HttpServletRequest requisicao, HttpServletResponse resposta) {
        Usuario usuario = autenticar.executar(new Autenticar.Credenciais(entrada.login(), entrada.senha()));
        TokenDeSessao token = emissor.emitir(usuario);
        rotacionarCsrf(requisicao, resposta);
        resposta.addHeader(HttpHeaders.SET_COOKIE, token.cookie().toString());
        return ResponseEntity.ok().body(sessao(usuario.getId(), usuario.getEmail(), usuario.getPapel()));
    }

    @GetMapping
    @Operation(summary = "Consulta o usuário logado e emite o cookie CSRF")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Usuário logado",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = Sessao.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Sem sessão válida",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                schema = @Schema(implementation = ProblemDetail.class)))
    })
    public Sessao obter(Authentication autenticacao) {
        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof PrincipalDaSessao principal)) {
            throw new SessaoAusente();
        }
        return sessao(principal.usuarioId(), principal.email(), papel(autenticacao));
    }

    @DeleteMapping
    @Operation(summary = "Encerra a sessão, fecha os canais SSE e apaga o cookie")
    @ApiResponses(@ApiResponse(responseCode = "204", description = "Sessão encerrada"))
    public ResponseEntity<Void> sair(HttpServletRequest requisicao, HttpServletResponse resposta) {
        identidadeDoLogout.usuarioId(requisicao).ifPresent(encerrarCanais::executar);
        rotacionarCsrf(requisicao, resposta);
        resposta.addHeader(HttpHeaders.SET_COOKIE, emissor.cookieExpirado().toString());
        return ResponseEntity.noContent().build();
    }

    private Sessao sessao(Long usuarioId, String email, Papel papel) {
        return new Sessao(usuarioId, email, papel, consultarNomeDeExibicao.executar(usuarioId, email, papel));
    }

    /**
     * Invalida o token CSRF da sessão anterior e grava um novo no cookie {@code XSRF-TOKEN} (DEC-08), para que ele não
     * sobreviva ao login/logout. O {@code saveToken(null)} manda o navegador apagar o valor antigo; o novo valor é
     * gravado em seguida.
     */
    private void rotacionarCsrf(HttpServletRequest requisicao, HttpServletResponse resposta) {
        csrf.saveToken(null, requisicao, resposta);
        csrf.saveToken(csrf.generateToken(requisicao), requisicao, resposta);
    }

    /** O papel vem das autoridades carregadas do banco pelo conversor do JWT (DEC-08), não da claim do token. */
    private static Papel papel(Authentication autenticacao) {
        return autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(autoridade -> autoridade.startsWith(PREFIXO_DE_AUTORIDADE))
                .map(autoridade -> autoridade.substring(PREFIXO_DE_AUTORIDADE.length()))
                .map(Papel::valueOf)
                .findFirst()
                .orElseThrow(SessaoAusente::new);
    }

    /** Requisição sem sessão em {@code GET /api/sessao}, respondida em 401 pelo handler abaixo. */
    private static final class SessaoAusente extends RuntimeException {}

    @ExceptionHandler(CredenciaisInvalidas.class)
    ProblemDetail tratarCredenciaisInvalidas(CredenciaisInvalidas excecao) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, excecao.getMessage());
    }

    @ExceptionHandler(SessaoAusente.class)
    ProblemDetail tratarSessaoAusente(SessaoAusente excecao) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Autenticação necessária.");
    }
}
