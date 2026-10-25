package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;

/** Cache das bandeiras do seed (RN-CLI-005, DEC-18). */
@SpringBootTest
class BandeiraRepositorioJpaTest {

    @Autowired
    private BandeiraRepositorioJpa repositorio;

    @Test
    void dec18_carregaEInterpretaATabelaUmaUnicaVez() {
        List<Bandeira> primeira = repositorio.listar();
        List<Bandeira> segunda = repositorio.listar();

        assertThat(primeira).extracting(Bandeira::nome)
                .containsExactly("Visa", "Mastercard", "Elo", "American Express", "Hipercard");
        assertThat(primeira.getFirst().prefixos()).isNotEmpty();
        assertThat(primeira.getFirst().tamanhos()).containsExactly(13, 16, 19);
        assertThat(segunda).isSameAs(primeira);
    }
}
