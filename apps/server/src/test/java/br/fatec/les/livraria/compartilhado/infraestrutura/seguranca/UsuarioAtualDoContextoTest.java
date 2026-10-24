package br.fatec.les.livraria.compartilhado.infraestrutura.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.fatec.les.livraria.acesso.infraestrutura.seguranca.PrincipalDaSessao;

/** Autor das escritas (RNF-GER-002, DEC-08) nos três contextos da tabela. */
class UsuarioAtualDoContextoTest {

    private final UsuarioAtualDoContexto usuarioAtual = new UsuarioAtualDoContexto();

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void requisicaoAutenticadaDevolveIdEEmail() {
        dentroDeRequisicao();
        autenticar(new PrincipalDaSessao(7L, "cliente@livraria.test"));

        assertThat(usuarioAtual.usuarioId()).contains(7L);
        assertThat(usuarioAtual.usuario()).isEqualTo("cliente@livraria.test");
    }

    @Test
    void requisicaoSemLoginDevolveAnonimo() {
        dentroDeRequisicao();

        assertThat(usuarioAtual.usuarioId()).isEmpty();
        assertThat(usuarioAtual.usuario()).isEqualTo("anonimo");
    }

    @Test
    void foraDeRequisicaoHttpDevolveSistema() {
        autenticar(new PrincipalDaSessao(7L, "cliente@livraria.test"));

        assertThat(usuarioAtual.usuarioId()).isEmpty();
        assertThat(usuarioAtual.usuario()).isEqualTo("sistema");
    }

    private static void dentroDeRequisicao() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    private static void autenticar(PrincipalDaSessao principal) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
    }
}
