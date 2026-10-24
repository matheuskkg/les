package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteResumo;
import br.fatec.les.livraria.clientes.aplicacao.consulta.FiltroCliente;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClientesConsultas;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.infraestrutura.persistencia.ConversorDePaginacao;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA da consulta de clientes do admin (RF-CLI-004, RNF-CLI-005, DEC-07). A listagem usa as
 * {@link EspecificacoesDeCliente} e devolve só os dados da tabela {@code cliente}; endereços e cartões são
 * carregados apenas no detalhe.
 */
@Component
@RequiredArgsConstructor
public class ClientesConsultasJpa implements ClientesConsultas {

    private final ClienteJpaRepository clientes;
    private final ClienteMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Pagina<ClienteResumo> listar(FiltroCliente filtro, Paginacao paginacao) {
        Page<ClienteJpa> pagina = clientes.findAll(EspecificacoesDeCliente.de(filtro),
                ConversorDePaginacao.paraPageable(paginacao));
        return ConversorDePaginacao.paraPagina(pagina, ClientesConsultasJpa::paraResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> buscarIds(String nome, String cpf) {
        return clientes.findAll(EspecificacoesDeCliente.porNomeECpf(nome, cpf)).stream()
                .map(ClienteJpa::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cliente> buscar(Long id) {
        return clientes.findById(id).map(mapper::paraDominio);
    }

    private static ClienteResumo paraResumo(ClienteJpa cliente) {
        return new ClienteResumo(cliente.getId(), cliente.getUsuarioId(), cliente.getNome(), cliente.getCpf(),
                cliente.getGenero(), cliente.getDataNascimento());
    }
}
