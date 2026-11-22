package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.infraestrutura.seguranca.PropriedadesJwt;
import br.fatec.les.livraria.compartilhado.infraestrutura.tempo.RelogioComoClock;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

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
    private final FabricaDeCookieDeSessao cookies;
    private final Relogio relogio;

    public TokenDeSessao emitir(Usuario usuario) {
        Instant agora = RelogioComoClock.instante(relogio);
        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(EMISSOR)
                        .subject(usuario.getId().toString())
                        .claim("papel", usuario.getPapel().name())
                        .issuedAt(agora)
                        .expiresAt(agora.plus(propriedadesJwt.validade()))
                        .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
        return new TokenDeSessao(valor, cookies.criar(valor));
    }

    /** Cookie que apaga a sessão no navegador (DEC-08). */
    public ResponseCookie cookieExpirado() {
        return cookies.expirado();
    }
}
