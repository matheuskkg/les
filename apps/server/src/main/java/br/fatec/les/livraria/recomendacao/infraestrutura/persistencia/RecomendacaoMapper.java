package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;

import org.mapstruct.Mapper;

/** Mapeamento domínio ↔ tabela {@code provedor_ia} (DEC-03, DEC-14). */
@Mapper
public interface RecomendacaoMapper {

    ProvedorIaJpa paraJpa(ProvedorIa provedor);

    ProvedorIa paraDominio(ProvedorIaJpa provedor);
}
