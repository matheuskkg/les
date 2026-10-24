package br.fatec.les.livraria.clientes.aplicacao.porta;

import java.util.Optional;

import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteResumo;
import br.fatec.les.livraria.clientes.aplicacao.consulta.FiltroCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

/**
 * Porta de saída das consultas de leitura de clientes do admin (RF-CLI-004, DEC-07). A implementação traduz o
 * filtro para especificações JPA; endereços e cartões só são carregados no detalhe, e a listagem não faz uma
 * consulta por item.
 */
public interface ClientesConsultas {

    /**
     * @param filtro campos combinados com E; gênero, UF e situação repetidos combinam com OU
     * @param paginacao página, tamanho e ordenação já validados
     * @return a página de clientes que atende ao filtro
     */
    Pagina<ClienteResumo> listar(FiltroCliente filtro, Paginacao paginacao);

    /**
     * @param id código do cliente
     * @return o cliente com endereços e cartões, ou vazio se não existir
     */
    Optional<Cliente> buscar(Long id);
}
