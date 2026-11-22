package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.ClienteDoUsuario;
import br.fatec.les.livraria.clientes.dominio.modelo.Endereco;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lista os endereços do cliente autenticado (RF-CLI-006, RNF-CLI-004, DEC-07). A quantidade é limitada pelos próprios
 * endereços do cliente, então a resposta é uma lista simples, sem paginação.
 */
@Component
@RequiredArgsConstructor
public class ListarEnderecos {

    private final ClienteDoUsuario clienteDoUsuario;

    @Transactional(readOnly = true)
    public List<Endereco> executar(Long usuarioId) {
        return clienteDoUsuario.buscar(usuarioId).getEnderecos();
    }
}
