package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.PropriedadesJwt;
import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.PropriedadesSessao;
import lombok.RequiredArgsConstructor;

/**
 * Emite o JWT de sessão e monta o cookie {@code sessao} (DEC-08). O token leva o id do usuário em {@code sub}, o papel
 * em {@code papel}, o emissor, a emissão e a expiração; a autorização usa o papel do banco, não esta claim.
 */
@Component
@RequiredArgsConstructor
public class EmissorDeToken {

    /** Emissor exigido na validação do token (DEC-08). */
    public static final String EMISSOR = "livraria";

    /** Nome do cookie de sessão. */
    public static final String COOKIE = "sessao";

    private final JwtEncoder jwtEncoder;
    private final PropriedadesJwt propriedadesJwt;
    private final PropriedadesSessao propriedadesSessao;

    public TokenDeSessao emitir(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.getId().toString())
                .claim("papel", usuario.getPapel().name())
                .issuedAt(agora)
                .expiresAt(agora.plus(propriedadesJwt.validade()))
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenDeSessao(valor, cookie(valor).build());
    }

    /** Cookie que apaga a sessão no navegador (DEC-08). */
    public ResponseCookie cookieExpirado() {
        return cookie("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder cookie(String valor) {
        return ResponseCookie.from(COOKIE, valor)
                .httpOnly(true)
                .secure(propriedadesSessao.cookieSeguro())
                .sameSite("Strict")
                .path("/api")
                .maxAge(propriedadesJwt.validade());
    }

    /** Validade configurada, usada pelo controller da sessão em respostas informativas. */
    public Duration validade() {
        return propriedadesJwt.validade();
    }
}
