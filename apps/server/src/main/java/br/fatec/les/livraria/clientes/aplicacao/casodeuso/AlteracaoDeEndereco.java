package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;

/** Comando de {@code PUT /api/me/enderecos/{id}} (RF-CLI-006, RNF-CLI-004): o id do endereço e os dados novos. */
public record AlteracaoDeEndereco(Long id, NovoEndereco dados) {
}
