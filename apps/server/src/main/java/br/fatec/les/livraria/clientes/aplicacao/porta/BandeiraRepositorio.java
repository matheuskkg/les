package br.fatec.les.livraria.clientes.aplicacao.porta;

import java.util.List;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;

/** Bandeiras de cartão registradas no seed (RN-CLI-005, DEC-16, DEC-18). */
public interface BandeiraRepositorio {

    List<Bandeira> listar();
}
