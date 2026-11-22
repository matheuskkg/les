package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import br.fatec.les.livraria.estoque.dominio.modelo.Reserva;

import org.mapstruct.Mapper;

/** Mapeamento entre a reserva de domínio e a tabela {@code reserva} (DEC-16). */
@Mapper
public interface ReservaMapper {

    ReservaJpa paraJpa(Reserva reserva);

    Reserva paraDominio(ReservaJpa reserva);
}
