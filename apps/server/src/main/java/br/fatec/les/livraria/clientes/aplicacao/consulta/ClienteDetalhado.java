package br.fatec.les.livraria.clientes.aplicacao.consulta;

import java.util.Objects;

import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;

/**
 * Detalhe de um cliente para o admin (RF-CLI-004, DEC-07, DEC-10): os dados pessoais com endereços e cartões do
 * cadastro, mais o e-mail, o telefone e a situação do usuário.
 *
 * @param cliente cliente com endereços e cartões
 * @param contato e-mail, telefone e situação do usuário
 * @param ranking ranking de 0 a 100 (RN-CLI-007, DEC-04)
 */
public record ClienteDetalhado(Cliente cliente, ContatoDoUsuario contato, int ranking) {

    public ClienteDetalhado {
        Objects.requireNonNull(cliente, "cliente");
        Objects.requireNonNull(contato, "contato");
    }
}
