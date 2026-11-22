package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Descobre o usuário do cookie {@code sessao} no logout, sem tornar a rota autenticada (DEC-08, DEC-13).
 *
 * <p>{@code DELETE /api/sessao} é público para que um token expirado ou de usuário inativo continue respondendo 204 e
 * apagando o cookie; por isso o filtro do token não o lê. Aqui o cookie é validado só para achar o dono dos canais SSE
 * a fechar: basta a assinatura e o {@code sub}, sem exigir conta ativa, para que os canais de quem foi inativado também
 * fechem. Token ausente ou inválido é simplesmente ignorado.
 */
@Component
@RequiredArgsConstructor
public class IdentidadeDoLogout {

    private final JwtDecoder jwtDecoder;

    /** Id do usuário do token válido do cookie, ou vazio se não há cookie ou ele não vale mais. */
    public Optional<Long> usuarioId(HttpServletRequest requisicao) {
        String token = ResolvedorDeTokenDaSessao.tokenDoCookie(requisicao);
        if (token == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(jwtDecoder.decode(token).getSubject()));
        } catch (JwtException | NumberFormatException excecao) {
            return Optional.empty();
        }
    }
}
