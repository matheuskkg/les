package br.fatec.les.livraria.clientes.infraestrutura.web.dto;

import br.fatec.les.livraria.acesso.dominio.modelo.TipoTelefone;

/** Telefone do cadastro (RN-CLI-006, DEC-10): tipo, DDD e número, guardados no {@code Usuario}. */
public record TelefoneDoCadastro(TipoTelefone tipo, String ddd, String numero) {
}
