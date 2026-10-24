package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

/**
 * Listagens filtradas em H2: envelope, ordenação com desempate, filtros combinados (RF-CLI-004, RF-LIV-005,
 * RNF-GER-001). Usa uma entidade só de teste, que não entra no contrato OpenAPI.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:listagem;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
        + "DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1")
@ActiveProfiles("h2")
@Transactional
class ListagemJpaTest {

    @Autowired
    private ExemploListagemRepositorio exemplos;

    @Autowired
    private ExemploTagRepositorio tags;

    @BeforeEach
    void inserirDados() {
        ExemploTagJpa poesia = tags.save(new ExemploTagJpa("poesia"));
        ExemploTagJpa romance = tags.save(new ExemploTagJpa("romance"));

        ExemploListagemJpa ana = salvar("Ana Ávila", "A-1", "12345678900", "SP", "10.00",
                LocalDate.of(2020, 1, 10));
        ana.adicionarTag(poesia);
        ana.adicionarTag(romance);
        exemplos.saveAndFlush(ana);

        ExemploListagemJpa anaSemAcento = salvar("ana avila", "A-2", "12345678900", "SP", "20.00",
                LocalDate.of(2021, 2, 10));
        anaSemAcento.adicionarTag(romance);
        exemplos.saveAndFlush(anaSemAcento);

        salvar("João Çedilha", "B-1", "98765432100", "RJ", "30.00", LocalDate.of(2022, 3, 10));
        salvar("100% puro", "C-1", "11122233344", "MG", "40.00", LocalDate.of(2023, 4, 10));
        salvar("a_b", "C-2", "55566677788", "MG", "50.00", LocalDate.of(2024, 5, 10));
        salvar("aXb", "C-3", "99988877766", "MG", "60.00", LocalDate.of(2025, 6, 10));
    }

    @Test
    void devolveOEnvelopeDePaginacao() {
        Paginacao paginacao = new Paginacao(0, 2, List.of(new Ordem("codigo", Ordem.Direcao.ASC)));

        Page<ExemploListagemJpa> page = exemplos.findAll(Specification.unrestricted(),
                ConversorDePaginacao.paraPageable(paginacao));
        Pagina<String> pagina = ConversorDePaginacao.paraPagina(page, ExemploListagemJpa::getCodigo);

        assertThat(pagina.itens()).containsExactly("A-1", "A-2");
        assertThat(pagina.pagina()).isZero();
        assertThat(pagina.tamanho()).isEqualTo(2);
        assertThat(pagina.totalItens()).isEqualTo(6);
        assertThat(pagina.totalPaginas()).isEqualTo(3);
    }

    @Test
    void ordenaComOIdComoDesempateEntreAsPaginas() {
        salvar("Mesmo Nome", "D-1", "10000000001", "SP", "10.00", LocalDate.of(2026, 1, 1));
        salvar("Mesmo Nome", "D-2", "10000000002", "SP", "10.00", LocalDate.of(2026, 1, 2));
        salvar("Mesmo Nome", "D-3", "10000000003", "SP", "10.00", LocalDate.of(2026, 1, 3));

        Specification<ExemploListagemJpa> mesmoNome = Especificacoes.contem("nome", "Mesmo Nome");
        Pageable primeiraPagina = ConversorDePaginacao
                .paraPageable(new Paginacao(0, 2, List.of(new Ordem("nome", Ordem.Direcao.ASC))));
        Pageable segundaPagina = ConversorDePaginacao
                .paraPageable(new Paginacao(1, 2, List.of(new Ordem("nome", Ordem.Direcao.ASC))));

        assertThat(primeiraPagina.getSort().toList()).extracting(Sort.Order::getProperty)
                .containsExactly("nome", "id");

        List<ExemploListagemJpa> itens = new ArrayList<>();
        itens.addAll(exemplos.findAll(mesmoNome, primeiraPagina).getContent());
        itens.addAll(exemplos.findAll(mesmoNome, segundaPagina).getContent());

        assertThat(itens).extracting(ExemploListagemJpa::getCodigo).containsExactly("D-1", "D-2", "D-3");
        assertThat(itens).extracting(ExemploListagemJpa::getId).doesNotHaveDuplicates().isSorted();
    }

    @Test
    void contemIgnoraCaixaEAcentos() {
        assertThat(buscar(Especificacoes.contem("nome", "ÁVILA")))
                .extracting(ExemploListagemJpa::getCodigo)
                .containsExactlyInAnyOrder("A-1", "A-2");
        assertThat(buscar(Especificacoes.contem("nome", "ÇEDILHA")))
                .extracting(ExemploListagemJpa::getCodigo)
                .containsExactly("B-1");
    }

    @Test
    void escapaOsCuringasDoLikeNoTermoDoUsuario() {
        assertThat(buscar(Especificacoes.contem("nome", "%")))
                .extracting(ExemploListagemJpa::getCodigo)
                .containsExactly("C-1");
        assertThat(buscar(Especificacoes.contem("nome", "_")))
                .extracting(ExemploListagemJpa::getCodigo)
                .containsExactly("C-2");
        assertThat(buscar(Especificacoes.contem("nome", "\\"))).isEmpty();
    }

    @Test
    void combinaContemIgualdadeEmIntervaloEDigitos() {
        Specification<ExemploListagemJpa> filtro = Especificacoes.<ExemploListagemJpa>contem("nome", "ávila")
                .and(Especificacoes.igual("uf", " SP "))
                .and(Especificacoes.em("codigo", List.of("A-1", "A-2", "B-1")))
                .and(Especificacoes.<ExemploListagemJpa, BigDecimal>entre("valor",
                        new BigDecimal("15.00"), new BigDecimal("25.00")))
                .and(Especificacoes.digitos("cpf", "123.456.789-00"));

        assertThat(buscar(filtro)).extracting(ExemploListagemJpa::getCodigo).containsExactly("A-2");
    }

    @Test
    void aplicaOIntervaloInclusivo() {
        assertThat(buscar(Especificacoes.entre("valor", new BigDecimal("20.00"), new BigDecimal("40.00"))))
                .extracting(ExemploListagemJpa::getCodigo)
                .containsExactlyInAnyOrder("A-2", "B-1", "C-1");
    }

    @Test
    void comparaCpfSoPelosDigitos() {
        assertThat(buscar(Especificacoes.digitos("cpf", "987.654.321-00")))
                .extracting(ExemploListagemJpa::getCodigo)
                .containsExactly("B-1");
    }

    @Test
    void filtraPorRelacionamentoSemDuplicarLinhas() {
        Specification<ExemploListagemJpa> filtro = Especificacoes.existe("tags", "nome", "poesia");

        Pagina<ExemploListagemJpa> pagina = ConversorDePaginacao.paraPagina(
                exemplos.findAll(filtro, ConversorDePaginacao.paraPageable(Paginacao.padrao())));

        assertThat(pagina.totalItens()).isEqualTo(1);
        assertThat(pagina.itens()).extracting(ExemploListagemJpa::getCodigo).containsExactly("A-1");
    }

    @Test
    void filtraPorRelacionamentoComVariosValoresUsandoOu() {
        Specification<ExemploListagemJpa> filtro = Especificacoes.existe("tags", "nome",
                List.of("poesia", "romance"));

        Pagina<ExemploListagemJpa> pagina = ConversorDePaginacao.paraPagina(
                exemplos.findAll(filtro, ConversorDePaginacao.paraPageable(Paginacao.padrao())));

        assertThat(pagina.totalItens()).isEqualTo(2);
        assertThat(pagina.itens()).extracting(ExemploListagemJpa::getCodigo)
                .containsExactlyInAnyOrder("A-1", "A-2");
    }

    @Test
    void ignoraFiltrosNulosOuEmBranco() {
        assertThat(buscar(Especificacoes.contem("nome", "  "))).hasSize(6);
        assertThat(buscar(Especificacoes.igual("uf", " "))).hasSize(6);
        assertThat(buscar(Especificacoes.em("uf", Arrays.asList(" ", null)))).hasSize(6);
        assertThat(buscar(Especificacoes.existe("tags", "nome", List.of(" ", "")))).hasSize(6);
        assertThat(buscar(Especificacoes.existe("tags", "nome", (String) null))).hasSize(6);
        assertThat(buscar(Especificacoes.digitos("cpf", "  "))).hasSize(6);
    }

    @Test
    void recusaDigitosQuandoOTermoNaoTemNumero() {
        assertThatThrownBy(() -> Especificacoes.digitos("cpf", "sem números"))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("cpf")
                .satisfies(e -> assertThat(((ParametroInvalido) e).getParametro()).isEqualTo("cpf"));
    }

    @Test
    void recusaIntervaloComInicioMaiorQueOFim() {
        assertThatThrownBy(() -> Especificacoes.entre("valor", new BigDecimal("40"), new BigDecimal("20")))
                .isInstanceOf(ParametroInvalido.class)
                .hasMessageContaining("valor");
    }

    private ExemploListagemJpa salvar(String nome, String codigo, String cpf, String uf, String valor,
            LocalDate dataCadastro) {
        return exemplos.saveAndFlush(new ExemploListagemJpa(nome, codigo, cpf, uf,
                new BigDecimal(valor), dataCadastro));
    }

    private List<ExemploListagemJpa> buscar(Specification<ExemploListagemJpa> especificacao) {
        return exemplos.findAll(especificacao);
    }
}
