package br.fatec.les.livraria.recomendacao.infraestrutura.web.dto;

import br.fatec.les.livraria.catalogo.infraestrutura.web.dto.LivroListadoDto;
import br.fatec.les.livraria.recomendacao.dominio.modelo.Avaliacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PapelDaMensagem;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mensagem da conversa com o assistente (RNF-REC-001, DEC-03, DEC-07). Os livros citados têm o formato da vitrine;
 * {@code tipo} e {@code avaliacao} são omitidos na mensagem do cliente e quando não existem.
 */
public record MensagemDto(
        Long id,
        PapelDaMensagem papel,
        String texto,
        @Nullable TipoDeResposta tipo,
        List<LivroListadoDto> livros,
        @Nullable Avaliacao avaliacao,
        LocalDateTime enviadaEm) {}
