package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.VISA;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/** Leitura e gravação dos cartões sem reinterpretar as colunas da bandeira (RN-CLI-004, RN-CLI-005, DEC-18). */
class ClienteMapperTest {

    private final ClienteMapper mapper = Mappers.getMapper(ClienteMapper.class);

    @Test
    void dec18_leituraDoCartaoNaoInterpretaOsPrefixosNemOsTamanhosDoSeed() {
        BandeiraJpa bandeira = new BandeiraJpa();
        bandeira.setId(1L);
        bandeira.setNome("Visa");
        bandeira.setPrefixos("valor malformado no seed");
        bandeira.setTamanhos("valor malformado no seed");

        CartaoJpa jpa = new CartaoJpa();
        jpa.setId(7L);
        jpa.setNumero("4111111111111111");
        jpa.setNomeImpresso("ANA SOUZA");
        jpa.setBandeira(bandeira);
        jpa.setValidade("2030-12");
        jpa.setPreferencial(true);

        Cartao cartao = mapper.paraDominio(jpa);

        assertThat(cartao.id()).isEqualTo(7L);
        assertThat(cartao.numero()).isEqualTo("4111111111111111");
        assertThat(cartao.bandeira().id()).isEqualTo(1L);
        assertThat(cartao.bandeira().nome()).isEqualTo("Visa");
        assertThat(cartao.bandeira().prefixos()).isEmpty();
        assertThat(cartao.bandeira().tamanhos()).isEmpty();
        assertThat(cartao.preferencial()).isTrue();
    }

    @Test
    void dec18_gravacaoDoCartaoUsaSoOIdDaBandeiraNaFk() {
        BandeiraJpa jpa = mapper.paraJpa(VISA);

        assertThat(jpa.getId()).isEqualTo(VISA.id());
        assertThat(jpa.getNome()).isNull();
        assertThat(jpa.getPrefixos()).isNull();
        assertThat(jpa.getTamanhos()).isNull();
    }
}
