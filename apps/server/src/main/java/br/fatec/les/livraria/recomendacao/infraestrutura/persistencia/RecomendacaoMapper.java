package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import org.mapstruct.Mapper;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;

/** Mapeamento domínio ↔ tabela {@code provedor_ia} (DEC-03, DEC-14). */
@Mapper
public interface RecomendacaoMapper {

    ProvedorIaJpa paraJpa(ProvedorIa provedor);

    ProvedorIa paraDominio(ProvedorIaJpa provedor);
}
