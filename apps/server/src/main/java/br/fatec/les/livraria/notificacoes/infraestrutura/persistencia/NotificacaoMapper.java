package br.fatec.les.livraria.notificacoes.infraestrutura.persistencia;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.fatec.les.livraria.notificacoes.dominio.modelo.Notificacao;

/** Mapeamento domínio ↔ tabela {@code notificacao} (DEC-14, DEC-16). */
@Mapper
public interface NotificacaoMapper {

    @Mapping(target = "lida", ignore = true)
    NotificacaoJpa paraJpa(Notificacao notificacao);

    Notificacao paraDominio(NotificacaoJpa notificacao);
}
