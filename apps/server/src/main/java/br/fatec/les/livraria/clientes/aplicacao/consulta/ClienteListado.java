package br.fatec.les.livraria.clientes.aplicacao.consulta;

import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;

import java.util.Objects;

/**
 * Item da lista de clientes do admin (RF-CLI-004, DEC-07): os dados do cliente e os contatos do usuário correspondente,
 * carregados em lote, e o ranking, calculado só para a página (RN-CLI-007, DEC-04, DEC-15.4).
 *
 * @param cliente dados da tabela {@code cliente}
 * @param contato e-mail, telefone e situação do usuário
 * @param ranking ranking de 0 a 100
 */
public record ClienteListado(ClienteResumo cliente, ContatoDoUsuario contato, int ranking) {

    public ClienteListado {
        Objects.requireNonNull(cliente, "cliente");
        Objects.requireNonNull(contato, "contato");
    }
}
