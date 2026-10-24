package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClienteRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Genero;
import br.fatec.les.livraria.compartilhado.dominio.Cpf;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA do cadastro do cliente com endereços e cartões (RF-CLI-001, RF-CLI-002, RF-CLI-006, RF-CLI-007, DEC-10,
 * DEC-16). A leitura acontece dentro da transação do caso de uso, com as coleções carregadas antes de virarem o
 * agregado de domínio ({@code spring.jpa.open-in-view=false}).
 */
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
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorUsuarioId(Long usuarioId) {
        return repositorio.findByUsuarioId(usuarioId).map(mapper::paraDominio);
    }

    @Override
    @Transactional
    public Optional<Cliente> buscarPorUsuarioIdParaAlterar(Long usuarioId) {
        return repositorio.buscarPorUsuarioIdParaAlterar(usuarioId).map(mapper::paraDominio);
    }

    @Override
    @Transactional
    public Cliente alterarDados(Long clienteId, String nome, Genero genero, LocalDate dataNascimento, Cpf cpf) {
        ClienteJpa entidade = repositorio.findById(clienteId)
                .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cliente não encontrado."));
        entidade.setNome(nome);
        entidade.setGenero(genero);
        entidade.setDataNascimento(dataNascimento);
        entidade.setCpf(cpf.numero());
        return mapper.paraDominio(repositorio.save(entidade));
    }

    @Override
    public List<Long> buscarUsuarioIdsPorCpf(String cpf) {
        return repositorio.buscarUsuarioIdsPorCpf(cpf);
    }

    @Override
    public Optional<Long> buscarIdPorUsuarioId(Long usuarioId) {
        return repositorio.buscarIdPorUsuarioId(usuarioId);
    }
}
