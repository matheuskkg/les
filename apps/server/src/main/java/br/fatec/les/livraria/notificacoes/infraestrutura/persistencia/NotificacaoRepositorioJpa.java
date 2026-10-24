package br.fatec.les.livraria.notificacoes.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ConversorDePaginacao;
import br.fatec.les.livraria.notificacoes.aplicacao.porta.NotificacaoRepositorio;
import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA da porta de notificações (DEC-13, DEC-14). A marcação de todas como lidas carrega as entidades e as
 * salva uma a uma para que o Envers registre a alteração (RNF-GER-002), em vez de um {@code update} em lote que
 * passaria sem revisão.
 */
@Component
@RequiredArgsConstructor
public class NotificacaoRepositorioJpa implements NotificacaoRepositorio {

    private final NotificacaoJpaRepository repositorio;
    private final NotificacaoMapper mapper;

    @Override
    public Notificacao salvar(Notificacao notificacao) {
        NotificacaoJpa salva = repositorio.save(mapper.paraJpa(notificacao));
        return mapper.paraDominio(salva);
    }

    @Override
    public Optional<Notificacao> buscarPorId(Long id) {
        return repositorio.findById(id).map(mapper::paraDominio);
    }

    @Override
    public Pagina<Notificacao> listar(Long usuarioId, Paginacao paginacao) {
        return ConversorDePaginacao.paraPagina(
                repositorio.findByUsuarioId(usuarioId, ConversorDePaginacao.paraPageable(paginacao)),
                mapper::paraDominio);
    }

    @Override
    public long contarNaoLidas(Long usuarioId) {
        return repositorio.countByUsuarioIdAndLidaEmIsNull(usuarioId);
    }

    @Override
    public int marcarTodasComoLidas(Long usuarioId, LocalDateTime agora) {
        List<NotificacaoJpa> naoLidas = repositorio.findByUsuarioIdAndLidaEmIsNull(usuarioId);
        naoLidas.forEach(notificacao -> notificacao.setLidaEm(agora));
        repositorio.saveAll(naoLidas);
        return naoLidas.size();
    }

    @Override
    public List<Notificacao> listarComIdMaiorQue(Long usuarioId, Long ultimoId) {
        return repositorio.findByUsuarioIdAndIdGreaterThanOrderByIdAsc(usuarioId, ultimoId).stream()
                .map(mapper::paraDominio)
                .toList();
    }
}
