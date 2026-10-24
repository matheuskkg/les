package br.fatec.les.livraria.acesso.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import lombok.RequiredArgsConstructor;

/**
 * Adapter da porta de usuários. O prazo de reativação aparece só aqui, ao calcular o limite das contas em uso
 * (DEC-10): uma conta encerrada não é encontrada pelo login nem entra na unicidade de e-mail e telefone.
 */
@Component
@RequiredArgsConstructor
public class UsuarioRepositorioJpa implements UsuarioRepositorio {

    private final UsuarioJpaRepository repositorio;
    private final UsuarioMapper mapper;
    private final Relogio relogio;

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpa salvo = repositorio.save(mapper.paraJpa(usuario));
        return mapper.paraDominio(salvo);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return repositorio.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }
        String identificador = login.strip();
        Optional<UsuarioJpa> encontrado = identificador.contains("@")
                ? repositorio.buscarPorEmailEmUso(identificador.toLowerCase(Locale.ROOT), limiteEmUso())
                : buscarPorTelefone(identificador);
        return encontrado.map(mapper::paraDominio);
    }

    @Override
    public boolean existeEmailEmUso(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return repositorio.contarPorEmailEmUso(email.strip().toLowerCase(Locale.ROOT), limiteEmUso()) > 0;
    }

    @Override
    public boolean existeTelefoneEmUso(String telefone) {
        String digitos = somenteDigitos(telefone);
        if (!telefoneValido(digitos)) {
            return false;
        }
        return repositorio.contarPorTelefoneEmUso(ddd(digitos), numero(digitos), limiteEmUso()) > 0;
    }

    @Override
    public List<Long> listarAdministradores() {
        return repositorio.listarIdsPorPapel(Papel.ADMIN);
    }

    private Optional<UsuarioJpa> buscarPorTelefone(String telefone) {
        String digitos = somenteDigitos(telefone);
        if (!telefoneValido(digitos)) {
            return Optional.empty();
        }
        return repositorio.buscarPorTelefoneEmUso(ddd(digitos), numero(digitos), limiteEmUso());
    }

    /** Limite de tempo das contas em uso: a inativação mais antiga que ainda pode ser reativada (DEC-10). */
    private LocalDateTime limiteEmUso() {
        return relogio.agora().minus(SituacaoDaConta.PRAZO_DE_REATIVACAO);
    }

    private static boolean telefoneValido(String digitos) {
        return digitos.length() == 10 || digitos.length() == 11;
    }

    private static String ddd(String digitos) {
        return digitos.substring(0, 2);
    }

    private static String numero(String digitos) {
        return digitos.substring(2);
    }

    private static String somenteDigitos(String telefone) {
        return telefone == null ? "" : telefone.replaceAll("\\D", "");
    }
}
