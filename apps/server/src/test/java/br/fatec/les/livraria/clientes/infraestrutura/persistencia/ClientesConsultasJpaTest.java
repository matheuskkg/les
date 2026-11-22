package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.porta.ClientesConsultas;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

/**
 * Busca de ids por nome e CPF no seed em H2 (RF-VEN-009, DEC-07, RNF-GER-001), com os ids vindo de uma
 * consulta projetada só na coluna {@code id}. Os ids conferidos são os dos dez clientes do
 * {@code 201_clientes.sql}; as regras são as mesmas do filtro da listagem: nome "contém", sem diferenciar
 * maiúsculas nem acentos, e CPF comparado só pelos dígitos.
 */
@SpringBootTest
@Transactional
class ClientesConsultasJpaTest {

    @Autowired
    private ClientesConsultas clientes;

    @Test
    void rfVen009_idsPeloNomeSemDiferenciarMaiusculas() {
        assertThat(clientes.buscarIds("BRUNO lima", null)).containsExactly(2L);
    }

    @Test
    void rfVen009_trechoQueApareceEmVariosClientesTrazTodos() {
        assertThat(clientes.buscarIds("ana", null)).containsExactlyInAnyOrder(1L, 10L);
    }

    @Test
    void rfVen009_idsPeloCpfComOuSemMascara() {
        assertThat(clientes.buscarIds(null, "111.444.777-35")).containsExactly(2L);
        assertThat(clientes.buscarIds(null, "11144477735")).containsExactly(2L);
    }

    @Test
    void rfVen009_nomeECpfCombinamComE() {
        assertThat(clientes.buscarIds("ana souza", "52998224725")).containsExactly(1L);
        assertThat(clientes.buscarIds("ana souza", "11144477735")).isEmpty();
    }

    @Test
    void dec07_parametroEmBrancoNaoFiltra() {
        assertThat(clientes.buscarIds("   ", "11144477735")).containsExactly(2L);
    }

    @Test
    void rfVen009_semNenhumFiltroDevolveOsIdsDeTodos() {
        assertThat(clientes.buscarIds(null, null))
                .contains(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }

    @Test
    void dec07_cpfSemNenhumDigitoRespondeParametroInvalido() {
        assertThatThrownBy(() -> clientes.buscarIds("ana", "abc"))
                .isInstanceOfSatisfying(ParametroInvalido.class, erro ->
                        assertThat(erro.getParametro()).isEqualTo("cpf"));
    }
}
