package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lista os cartões do cliente autenticado (RF-CLI-007, DEC-07). A API devolve o número mascarado (DEC-10). A resposta é
 * uma lista simples: a quantidade é limitada pelos cartões do próprio cliente.
 */
@Component
@RequiredArgsConstructor
public class ListarCartoes {

    private final ClienteDoUsuario clienteDoUsuario;

    @Transactional(readOnly = true)
    public List<Cartao> executar(Long usuarioId) {
        return clienteDoUsuario.buscar(usuarioId).getCartoes();
    }
}
