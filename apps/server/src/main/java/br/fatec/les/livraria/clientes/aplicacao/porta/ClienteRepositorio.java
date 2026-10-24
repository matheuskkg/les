package br.fatec.les.livraria.clientes.aplicacao.porta;

import java.util.List;

import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;

/** Persistência do cliente e dos seus endereços e cartões (RF-CLI-001, DEC-10, DEC-16). */
public interface ClienteRepositorio {

    Cliente salvar(Cliente cliente);

    /**
     * Ids dos usuários dos clientes com o CPF informado, em qualquer situação. A regra {@code CpfUnico} pergunta ao
     * {@code acesso} se algum deles ainda está em uso (DEC-10).
     */
    List<Long> buscarUsuarioIdsPorCpf(String cpf);
}
