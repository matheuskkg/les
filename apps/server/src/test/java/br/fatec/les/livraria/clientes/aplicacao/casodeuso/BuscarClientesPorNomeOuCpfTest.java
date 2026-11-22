package br.fatec.les.livraria.clientes.aplicacao.casodeuso;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.aplicacao.consulta.ClienteResumo;
import br.fatec.les.livraria.clientes.aplicacao.consulta.FiltroCliente;
import br.fatec.les.livraria.clientes.aplicacao.porta.ClientesConsultas;
import br.fatec.les.livraria.clientes.dominio.modelo.Cliente;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

class BuscarClientesPorNomeOuCpfTest {

    private final ClientesComIds clientes = new ClientesComIds();
    private final BuscarClientesPorNomeOuCpf buscar = new BuscarClientesPorNomeOuCpf(clientes);

    @Test
    void rfVen009_semNomeNemCpfNaoFiltra() {
        assertThat(buscar.executar(null, "  ")).isEmpty();
        assertThat(clientes.consultas).isZero();
    }

    @Test
    void rfVen009_nomeOuCpfDevolveOsIdsEncontrados() {
        assertThat(buscar.executar("ana", null)).contains(Set.of(1L));
        assertThat(buscar.executar(null, "529.982.247-25")).contains(Set.of(1L));
    }

    @Test
    void rfVen009_nenhumClienteEncontradoDevolveConjuntoVazio() {
        clientes.ids = Set.of();

        assertThat(buscar.executar("zzz", null)).isEqualTo(Optional.of(Set.of()));
    }

    private static final class ClientesComIds implements ClientesConsultas {

        private Set<Long> ids = Set.of(1L);
        private int consultas;

        @Override
        public Set<Long> buscarIds(String nome, String cpf) {
            consultas++;
            return ids;
        }

        @Override
        public Pagina<ClienteResumo> listar(FiltroCliente filtro, Paginacao paginacao) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Cliente> buscar(Long id) {
            throw new UnsupportedOperationException();
        }
    }
}
