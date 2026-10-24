package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import lombok.RequiredArgsConstructor;

/** Adapter JPA do cadastro do cliente com endereços e cartões (RF-CLI-001, DEC-10, DEC-16). */
@Component
@RequiredArgsConstructor
public class ClienteRepositorioJpa implements ClienteRepositorio {

    private final ClienteJpaRepository repositorio;
    private final ClienteMapper mapper;

    @Override
    @Transactional
    public Cliente salvar(Cliente cliente) {
        ClienteJpa entidade = mapper.paraJpa(cliente);
        entidade.getEnderecos().forEach(endereco -> endereco.setCliente(entidade));
        entidade.getCartoes().forEach(cartao -> cartao.setCliente(entidade));
        return mapper.paraDominio(repositorio.save(entidade));
    }

    @Override
    public List<Long> buscarUsuarioIdsPorCpf(String cpf) {
        return repositorio.buscarUsuarioIdsPorCpf(cpf);
    }
}
