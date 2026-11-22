package br.fatec.les.livraria.clientes.aplicacao.porta;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;

import java.util.List;

/** Bandeiras de cartão registradas no seed (RN-CLI-005, DEC-16, DEC-18). */
public interface BandeiraRepositorio {

    List<Bandeira> listar();
}
