package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Descobre o cliente autenticado para as rotas {@code /api/me} (RF-VEN-001, DEC-07, DEC-15.4). O cliente nunca informa
 * o próprio id: ele vem do usuário da sessão, e o {@code vendas} usa este caso de uso público em vez de acessar a
 * persistência de {@code clientes}.
 */
@Component
@RequiredArgsConstructor
public class IdentificarCliente {

    private final ClienteRepositorio clientes;
    private final UsuarioAtual usuarioAtual;

    /**
     * @return id do cliente do usuário autenticado
     * @throws ExcecaoDeRecursoNaoEncontrado se não houver usuário na sessão ou ele não tiver cliente
     */
    @Transactional(readOnly = true)
    public Long executar() {
        Long usuarioId =
                usuarioAtual
                        .usuarioId()
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
        return clientes.buscarIdPorUsuarioId(usuarioId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
    }
}
