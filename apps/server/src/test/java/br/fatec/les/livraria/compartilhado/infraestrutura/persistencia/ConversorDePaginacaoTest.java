package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/** Conversão entre a paginação da aplicação e os tipos do Spring Data (DEC-07). */
class ConversorDePaginacaoTest {

    @Test
    void acrescentaOIdComoUltimoDesempate() {
        Paginacao paginacao =
                new Paginacao(
                        1, 10, List.of(new Ordem("nome", Ordem.Direcao.ASC), new Ordem("email", Ordem.Direcao.DESC)));

        Pageable pageable = ConversorDePaginacao.paraPageable(paginacao);

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().toList())
                .extracting(Sort.Order::getProperty)
                .containsExactly("nome", "email", "id");
        assertThat(pageable.getSort().toList())
                .extracting(Sort.Order::getDirection)
                .containsExactly(Sort.Direction.ASC, Sort.Direction.DESC, Sort.Direction.ASC);
    }

    @Test
    void semOrdenacaoUsaApenasOId() {
        Pageable pageable = ConversorDePaginacao.paraPageable(Paginacao.padrao());

        assertThat(pageable.getSort().toList()).extracting(Sort.Order::getProperty).containsExactly("id");
    }

    @Test
    void naoRepeteOIdQuandoJaFoiInformado() {
        Paginacao paginacao = new Paginacao(0, 20, List.of(new Ordem("id", Ordem.Direcao.DESC)));

        Pageable pageable = ConversorDePaginacao.paraPageable(paginacao);

        assertThat(pageable.getSort().toList()).extracting(Sort.Order::getProperty).containsExactly("id");
        assertThat(pageable.getSort().toList())
                .extracting(Sort.Order::getDirection)
                .containsExactly(Sort.Direction.DESC);
    }

    @Test
    void converteOPageParaOEnvelopeDaApi() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        Pagina<String> pagina = ConversorDePaginacao.paraPagina(page);

        assertThat(pagina.itens()).containsExactly("a", "b");
        assertThat(pagina.pagina()).isEqualTo(1);
        assertThat(pagina.tamanho()).isEqualTo(2);
        assertThat(pagina.totalItens()).isEqualTo(5);
        assertThat(pagina.totalPaginas()).isEqualTo(3);
    }

    @Test
    void converteOsItensComOMapeador() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "bb"), PageRequest.of(0, 20), 2);

        Pagina<Integer> pagina = ConversorDePaginacao.paraPagina(page, String::length);

        assertThat(pagina.itens()).containsExactly(1, 2);
        assertThat(pagina.totalItens()).isEqualTo(2);
    }
}
