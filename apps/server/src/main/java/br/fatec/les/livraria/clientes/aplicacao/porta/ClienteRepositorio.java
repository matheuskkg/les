package br.fatec.les.livraria.clientes.aplicacao.porta;

import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistência do cliente e dos seus endereços e cartões (RF-CLI-001, RF-CLI-002, RF-CLI-006, RF-CLI-007, DEC-10,
 * DEC-16).
 */
public interface ClienteRepositorio {

    Cliente salvar(Cliente cliente);

    /** Busca o cliente do usuário autenticado, sem que a API receba um id de cliente na URL (DEC-07). */
    Optional<Cliente> buscarPorUsuarioId(Long usuarioId);

    /**
     * Como {@link #buscarPorUsuarioId(Long)}, travando o cliente até o fim da transação: duas alterações simultâneas de
     * endereços ou cartões não se sobrepõem (DEC-10: um só cartão preferencial).
     */
    Optional<Cliente> buscarPorUsuarioIdParaAlterar(Long usuarioId);

    /**
     * Altera os dados cadastrais que ficam no {@code Cliente} (RF-CLI-002). E-mail e telefone são alterados no {@code
     * acesso}, pelo caso de uso público {@code AlterarContato} (DEC-08, DEC-10).
     */
    Cliente alterarDados(Long clienteId, String nome, Genero genero, LocalDate dataNascimento, Cpf cpf);

    /**
     * Id do cliente do usuário de login, para as rotas {@code /api/me} (RF-VEN-001, DEC-07, DEC-15.4). O cliente nunca
     * informa o próprio id: ele vem do usuário autenticado.
     */
    Optional<Long> buscarIdPorUsuarioId(Long usuarioId);

    /** Usuário de login do cliente, para notificar fora de uma requisição, como na expiração do carrinho (DEC-13). */
    Optional<Long> buscarUsuarioIdPorClienteId(Long clienteId);

    /**
     * Ids dos usuários dos clientes com o CPF informado, em qualquer situação. A regra {@code CpfUnico} pergunta ao
     * {@code acesso} se algum deles ainda está em uso (DEC-10).
     */
    List<Long> buscarUsuarioIdsPorCpf(String cpf);
}
