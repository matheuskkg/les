package br.fatec.les.livraria.acesso.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoDaConta;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
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
    public List<Usuario> buscarPorIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repositorio.findAllById(ids).stream().map(mapper::paraDominio).toList();
    }

    @Override
    public Set<Long> buscarIdsPorContato(String termo, String digitos) {
        if (termo == null && digitos == null) {
            return Set.of();
        }
        List<Long> encontrados;
        if (termo != null && digitos != null) {
            encontrados = repositorio.buscarIdsPorEmailETelefone(termo, digitos);
        } else if (termo != null) {
            encontrados = repositorio.buscarIdsPorEmail(termo);
        } else {
            encontrados = repositorio.buscarIdsPorTelefone(digitos);
        }
        return Set.copyOf(encontrados);
    }

    @Override
    public Set<Long> listarIdsPorSituacao(Collection<SituacaoConta> situacoes, LocalDateTime agora) {
        if (situacoes == null || situacoes.isEmpty()) {
            return Set.of();
        }
        LocalDateTime limite = agora.minus(SituacaoDaConta.PRAZO_DE_REATIVACAO);
        Set<Long> ids = new LinkedHashSet<>();
        if (situacoes.contains(SituacaoConta.ATIVA)) {
            ids.addAll(repositorio.listarIdsAtivos());
        }
        if (situacoes.contains(SituacaoConta.INATIVA)) {
            ids.addAll(repositorio.listarIdsInativos(limite));
        }
        if (situacoes.contains(SituacaoConta.ENCERRADA)) {
            ids.addAll(repositorio.listarIdsEncerrados(limite));
        }
        return Set.copyOf(ids);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }
        String identificador = login.strip();
        List<UsuarioJpa> encontrados = identificador.contains("@")
                ? repositorio.buscarPorEmailEmUso(identificador.toLowerCase(Locale.ROOT), limiteEmUso())
                : buscarPorTelefone(identificador);
        return encontrados.stream().findFirst().map(mapper::paraDominio);
    }

    @Override
    public boolean existeEmailEmUso(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return repositorio.contarPorEmailEmUso(email.strip().toLowerCase(Locale.ROOT), limiteEmUso()) > 0;
    }

    @Override
    public boolean existeEmailEmUsoExceto(String email, Long usuarioId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String normalizado = email.strip().toLowerCase(Locale.ROOT);
        if (usuarioId == null) {
            return existeEmailEmUso(normalizado);
        }
        return repositorio.contarPorEmailEmUsoExceto(normalizado, usuarioId, limiteEmUso()) > 0;
    }

    @Override
    public boolean existeTelefoneEmUso(String telefone) {
        String digitos = Telefone.digitosNacionais(telefone);
        if (!Telefone.digitosValidos(digitos)) {
            return false;
        }
        return repositorio.contarPorTelefoneEmUso(ddd(digitos), numero(digitos), limiteEmUso()) > 0;
    }

    @Override
    public boolean existeTelefoneEmUsoExceto(String telefone, Long usuarioId) {
        String digitos = Telefone.digitosNacionais(telefone);
        if (!Telefone.digitosValidos(digitos)) {
            return false;
        }
        if (usuarioId == null) {
            return existeTelefoneEmUso(digitos);
        }
        return repositorio.contarPorTelefoneEmUsoExceto(ddd(digitos), numero(digitos), usuarioId, limiteEmUso()) > 0;
    }

    @Override
    public boolean existeAlgumaEmUso(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return repositorio.contarEmUsoPorIds(ids, limiteEmUso()) > 0;
    }

    @Override
    public List<Long> listarAdministradores() {
        return repositorio.listarIdsPorPapel(Papel.ADMIN);
    }

    private List<UsuarioJpa> buscarPorTelefone(String telefone) {
        String digitos = Telefone.digitosNacionais(telefone);
        if (!Telefone.digitosValidos(digitos)) {
            return List.of();
        }
        return repositorio.buscarPorTelefoneEmUso(ddd(digitos), numero(digitos), limiteEmUso());
    }

    /** Limite de tempo das contas em uso: a inativação mais antiga que ainda pode ser reativada (DEC-10). */
    private LocalDateTime limiteEmUso() {
        return relogio.agora().minus(SituacaoDaConta.PRAZO_DE_REATIVACAO);
    }



    private static String ddd(String digitos) {
        return digitos.substring(0, 2);
    }

    private static String numero(String digitos) {
        return digitos.substring(2);
    }

}
