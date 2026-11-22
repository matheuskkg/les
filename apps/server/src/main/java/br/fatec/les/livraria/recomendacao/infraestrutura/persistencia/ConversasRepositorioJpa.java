package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA das conversas com o assistente (RNF-REC-001, DEC-03, DEC-14). Cada operação tem a própria transação;
 * numa mensagem existente, só a avaliação muda. O texto é cortado no tamanho da coluna.
 */
@Component
@RequiredArgsConstructor
public class ConversasRepositorioJpa implements ConversasRepositorio {

    private final ConversaJpaRepository conversas;
    private final MensagemConversaJpaRepository mensagens;
    private final ChamadaIaJpaRepository chamadas;

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> conversaAtiva(Long clienteId) {
        return conversas.findFirstByClienteIdAndArquivadaEmIsNullOrderByIdDesc(clienteId).map(ConversaJpa::getId);
    }

    @Override
    @Transactional
    public Long abrir(Long clienteId, LocalDateTime em) {
        ConversaJpa conversa = new ConversaJpa();
        conversa.setClienteId(clienteId);
        conversa.setIniciadaEm(em);
        return conversas.save(conversa).getId();
    }

    @Override
    @Transactional
    public void arquivar(Long conversaId, LocalDateTime em) {
        ConversaJpa conversa = conversas.findById(conversaId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Conversa não encontrada."));
        conversa.setArquivadaEm(em);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensagemDaConversa> mensagens(Long conversaId) {
        return mensagens.findByConversaIdOrderByEnviadaEmAscIdAsc(conversaId).stream()
                .map(ConversasRepositorioJpa::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensagemDaConversa> ultimas(Long conversaId, int limite) {
        // Duas consultas em vez de uma por mensagem: os ids já limitados no banco e o resto com os livros em lote
        // (RNF-GER-001).
        List<Long> ids = mensagens.idsDasUltimas(conversaId, Limit.of(limite));
        if (ids.isEmpty()) {
            return List.of();
        }
        return mensagens.buscarPorIds(ids).stream()
                .sorted(Comparator.comparing(MensagemConversaJpa::getEnviadaEm)
                        .thenComparing(MensagemConversaJpa::getId))
                .map(ConversasRepositorioJpa::paraDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long enviadasPeloClienteDesde(Long clienteId, LocalDateTime desde) {
        return mensagens.contarDoCliente(clienteId, PapelDaMensagem.CLIENTE, desde);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MensagemDaConversa> buscarNaConversaAtiva(Long mensagemId, Long clienteId) {
        return mensagens.buscarNaConversaAtiva(mensagemId, clienteId).map(ConversasRepositorioJpa::paraDominio);
    }

    @Override
    @Transactional
    public MensagemDaConversa salvar(MensagemDaConversa mensagem) {
        MensagemConversaJpa entidade = mensagem.id() == null ? nova(mensagem) : avaliada(mensagem);
        return paraDominio(mensagens.save(entidade));
    }

    private MensagemConversaJpa nova(MensagemDaConversa mensagem) {
        MensagemConversaJpa entidade = new MensagemConversaJpa();
        entidade.setConversa(conversas.getReferenceById(mensagem.conversaId()));
        if (mensagem.chamadaIaId() != null) {
            entidade.setChamadaIa(chamadas.getReferenceById(mensagem.chamadaIaId()));
        }
        entidade.setPapel(mensagem.papel());
        entidade.setTexto(cortar(mensagem.texto()));
        entidade.setTipo(mensagem.tipo());
        entidade.setEnviadaEm(mensagem.enviadaEm());
        for (Long livroId : mensagem.livrosCitados()) {
            entidade.getLivros().add(new LivroCitadoJpa(livroId, entidade.getLivros().size() + 1));
        }
        return entidade;
    }

    private MensagemConversaJpa avaliada(MensagemDaConversa mensagem) {
        MensagemConversaJpa entidade = mensagens.findById(mensagem.id())
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Mensagem não encontrada."));
        entidade.setAvaliacao(mensagem.avaliacao());
        entidade.setAvaliadaEm(mensagem.avaliadaEm());
        return entidade;
    }

    private static String cortar(String texto) {
        int tamanho = MensagemConversaJpa.TAMANHO_DO_TEXTO;
        return texto.codePointCount(0, texto.length()) <= tamanho
                ? texto
                : texto.substring(0, texto.offsetByCodePoints(0, tamanho));
    }

    private static MensagemDaConversa paraDominio(MensagemConversaJpa entidade) {
        return new MensagemDaConversa(entidade.getId(), entidade.getConversa().getId(), entidade.getPapel(),
                entidade.getTexto(), entidade.getTipo(),
                entidade.getLivros().stream().sorted(Comparator.comparingInt(LivroCitadoJpa::getPosicao))
                        .map(LivroCitadoJpa::getLivroId).toList(),
                entidade.getChamadaIa() == null ? null : entidade.getChamadaIa().getId(), entidade.getEnviadaEm(),
                entidade.getAvaliacao(), entidade.getAvaliadaEm());
    }
}
