package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ConsultarUsuarios;
import br.fatec.les.livraria.acesso.aplicacao.consulta.ContatoDoUsuario;
import br.fatec.les.livraria.clientes.aplicacao.CalcularRanking;
import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteDetalhado;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClientesConsultas;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import lombok.RequiredArgsConstructor;

/**
 * Detalhe de um cliente para o administrador (RF-CLI-004, DEC-07, DEC-10), com dados pessoais, endereços, cartões
 * mascarados e o contato do usuário. Cliente inexistente responde HTTP 404.
 */
@Component
@RequiredArgsConstructor
public class ConsultarCliente {

    private final ClientesConsultas clientes;
    private final ConsultarUsuarios consultarUsuarios;
    private final Relogio relogio;
    private final CalcularRanking calcularRanking;

    /**
     * @param id código do cliente
     * @return o cliente com endereços, cartões e contato
     * @throws ExcecaoDeRecursoNaoEncontrado se o cliente não existir
     */
    @Transactional(readOnly = true)
    public ClienteDetalhado executar(Long id) {
        Objects.requireNonNull(id, "id");
        Cliente cliente = clientes.buscar(id)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
        Map<Long, ContatoDoUsuario> contatos = consultarUsuarios.executar(List.of(cliente.getUsuarioId()),
                relogio.agora());
        ContatoDoUsuario contato = Objects.requireNonNull(contatos.get(cliente.getUsuarioId()),
                "Contato do usuário " + cliente.getUsuarioId() + " não encontrado.");
        return new ClienteDetalhado(cliente, contato, calcularRanking.executar(List.of(cliente.getId())).get(cliente.getId()));
    }
}
