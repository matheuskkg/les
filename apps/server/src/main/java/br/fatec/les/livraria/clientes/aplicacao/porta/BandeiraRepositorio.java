package br.fatec.les.livraria.clientes.aplicacao.porta;

import java.util.Optional;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;

/** Bandeiras de cartão registradas no seed (RN-CLI-005, DEC-16). */
public interface BandeiraRepositorio {

    Optional<Bandeira> buscarPorId(Long id);
}
