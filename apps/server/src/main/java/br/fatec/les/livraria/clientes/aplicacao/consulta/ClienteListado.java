package br.fatec.les.livraria.clientes.aplicacao.consulta;

import java.util.Objects;

import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;

/**
 * Item da lista de clientes do admin (RF-CLI-004, DEC-07): os dados do cliente e os contatos do usuário
 * correspondente, carregados em lote. Sem ranking, que não faz parte desta listagem (DEC-15.4).
 *
 * @param cliente dados da tabela {@code cliente}
 * @param contato e-mail, telefone e situação do usuário
 */
public record ClienteListado(ClienteResumo cliente, ContatoDoUsuario contato) {

    public ClienteListado {
        Objects.requireNonNull(cliente, "cliente");
        Objects.requireNonNull(contato, "contato");
    }
}
