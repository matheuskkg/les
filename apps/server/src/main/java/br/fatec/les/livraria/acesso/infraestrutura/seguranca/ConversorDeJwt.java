package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

import lombok.RequiredArgsConstructor;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converte o JWT em autenticação carregando o usuário pelo {@code sub} (DEC-08). Usuário inexistente ou fora da
 * situação {@code ATIVA} derruba a autenticação (401), e as autoridades vêm do papel do banco, nunca da claim.
 */
@Component
@RequiredArgsConstructor
public class ConversorDeJwt implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UsuarioRepositorio usuarios;
    private final SituacaoDaConta situacaoDaConta;
    private final Relogio relogio;

    @Override
    public AbstractAuthenticationToken convert(Jwt token) {
        Usuario usuario =
                usuarios.buscarPorId(idDoToken(token))
                        .orElseThrow(() -> new InvalidBearerTokenException("Usuário da sessão não encontrado."));
        if (situacaoDaConta.calcular(usuario, relogio.agora()) != SituacaoConta.ATIVA) {
            throw new InvalidBearerTokenException("Usuário da sessão não está ativo.");
        }
        List<GrantedAuthority> autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPapel().name()));
        return new JwtAuthenticationToken(
                token, new PrincipalDaSessao(usuario.getId(), usuario.getEmail()), autoridades);
    }

    private static Long idDoToken(Jwt token) {
        try {
            return Long.valueOf(token.getSubject());
        } catch (NumberFormatException excecao) {
            throw new InvalidBearerTokenException("Token sem usuário válido.", excecao);
        }
    }
}
