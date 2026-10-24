package br.fatec.les.livraria.clientes.dominio.regra;

import static br.fatec.les.livraria.clientes.ClientesDeTeste.comCartoes;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comEnderecos;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.comNome;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.endereco;
import static br.fatec.les.livraria.clientes.ClientesDeTeste.violacoes;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.clientes.infraestrutura.persistencia.CartaoJpa;
import br.fatec.les.livraria.clientes.infraestrutura.persistencia.ClienteJpa;
import br.fatec.les.livraria.clientes.infraestrutura.persistencia.EnderecoJpa;
import jakarta.persistence.Column;

/** RN-CLI-006, RN-CLI-003, RN-CLI-004: limites das colunas do cliente, do endereço e do cartão. */
class TamanhosMaximosClienteTest {

    private final TamanhosMaximosCliente regra =
            new TamanhosMaximosCliente(new TamanhosMaximosEndereco(), new TamanhosMaximosCartao());

    @Test
    void rnCli006_nomeNoLimiteEhAceito() {
        assertThat(violacoes(comNome("a".repeat(TamanhosMaximosCliente.NOME)), regra)).isEmpty();
    }

    @Test
    void rnCli006_nomeAcimaDoLimiteEhRejeitado() {
        rejeita("RN-CLI-006", "nome do cliente", comNome("a".repeat(TamanhosMaximosCliente.NOME + 1)));
    }

    @Test
    void rnCli003_enderecoNoLimiteEhAceito() {
        assertThat(violacoes(comEnderecos(List.of(enderecoComLimites())), regra)).isEmpty();
    }

    @Test
    void rnCli003_camposDoEnderecoAcimaDoLimiteSaoRejeitados() {
        rejeita("RN-CLI-003", "nome curto", comEnderecos(List.of(enderecoComNomeCurto(maior(NOME_CURTO)))));
        rejeita("RN-CLI-003", "logradouro", comEnderecos(List.of(enderecoComLogradouro(maior(LOGRADOURO)))));
        rejeita("RN-CLI-003", "número", comEnderecos(List.of(enderecoComNumero(maior(NUMERO_DO_ENDERECO)))));
        rejeita("RN-CLI-003", "bairro", comEnderecos(List.of(enderecoComBairro(maior(BAIRRO)))));
        rejeita("RN-CLI-003", "cidade", comEnderecos(List.of(enderecoComCidade(maior(CIDADE)))));
        rejeita("RN-CLI-003", "observações", comEnderecos(List.of(enderecoComObservacoes(maior(OBSERVACOES)))));
    }

    @Test
    void rnCli004_nomeImpressoNoLimiteEhAceito() {
        assertThat(violacoes(comCartoes(List.of(cartaoComNomeImpresso(limite(NOME_IMPRESSO)))), regra)).isEmpty();
    }

    @Test
    void rnCli004_nomeImpressoAcimaDoLimiteEhRejeitado() {
        rejeita("RN-CLI-004", "nome impresso",
                comCartoes(List.of(cartaoComNomeImpresso(maior(NOME_IMPRESSO)))));
    }

    /** As constantes da regra precisam continuar iguais às colunas JPA que elas protegem. */
    @ParameterizedTest
    @MethodSource("colunas")
    void limiteEspelhaAColunaJpa(Class<?> regraDoLimite, String constante, Class<?> entidade, String campo)
            throws Exception {
        int limite = regraDoLimite.getField(constante).getInt(null);
        Column coluna = entidade.getDeclaredField(campo).getAnnotation(Column.class);

        assertThat(coluna).as("coluna %s.%s", entidade.getSimpleName(), campo).isNotNull();
        assertThat(coluna.length()).as("limite de %s", constante).isEqualTo(limite);
    }

    static Stream<Arguments> colunas() {
        return Stream.of(
                Arguments.of(TamanhosMaximosCliente.class, "NOME", ClienteJpa.class, "nome"),
                Arguments.of(TamanhosMaximosCliente.class, "CPF", ClienteJpa.class, "cpf"),
                Arguments.of(TamanhosMaximosEndereco.class, "NOME_CURTO", EnderecoJpa.class, "nomeCurto"),
                Arguments.of(TamanhosMaximosEndereco.class, "LOGRADOURO", EnderecoJpa.class, "logradouro"),
                Arguments.of(TamanhosMaximosEndereco.class, "NUMERO_DO_ENDERECO", EnderecoJpa.class, "numero"),
                Arguments.of(TamanhosMaximosEndereco.class, "BAIRRO", EnderecoJpa.class, "bairro"),
                Arguments.of(TamanhosMaximosEndereco.class, "CEP", EnderecoJpa.class, "cep"),
                Arguments.of(TamanhosMaximosEndereco.class, "CIDADE", EnderecoJpa.class, "cidade"),
                Arguments.of(TamanhosMaximosEndereco.class, "OBSERVACOES", EnderecoJpa.class, "observacoes"),
                Arguments.of(TamanhosMaximosCartao.class, "NUMERO_DO_CARTAO", CartaoJpa.class, "numero"),
                Arguments.of(TamanhosMaximosCartao.class, "NOME_IMPRESSO", CartaoJpa.class, "nomeImpresso"),
                Arguments.of(TamanhosMaximosCartao.class, "VALIDADE_DO_CARTAO", CartaoJpa.class, "validade"));
    }

    private static final int NOME_CURTO = TamanhosMaximosEndereco.NOME_CURTO;
    private static final int LOGRADOURO = TamanhosMaximosEndereco.LOGRADOURO;
    private static final int NUMERO_DO_ENDERECO = TamanhosMaximosEndereco.NUMERO_DO_ENDERECO;
    private static final int BAIRRO = TamanhosMaximosEndereco.BAIRRO;
    private static final int CIDADE = TamanhosMaximosEndereco.CIDADE;
    private static final int OBSERVACOES = TamanhosMaximosEndereco.OBSERVACOES;
    private static final int NOME_IMPRESSO = TamanhosMaximosCartao.NOME_IMPRESSO;

    private void rejeita(String codigo, String campo, NovoCliente alvo) {
        assertThat(violacoes(alvo, regra))
                .singleElement()
                .satisfies(violacao -> {
                    assertThat(violacao.codigo()).isEqualTo(codigo);
                    assertThat(violacao.mensagem()).contains(campo);
                });
    }

    private static String maior(int limite) {
        return "a".repeat(limite + 1);
    }

    private static String limite(int limite) {
        return "a".repeat(limite);
    }

    private static NovoEndereco enderecoComLimites() {
        return new NovoEndereco(limite(NOME_CURTO), endereco().tipoEndereco(), endereco().tipoLogradouro(),
                limite(LOGRADOURO), limite(NUMERO_DO_ENDERECO), limite(BAIRRO), endereco().cep(),
                limite(CIDADE), endereco().uf(), limite(OBSERVACOES));
    }

    private static NovoEndereco enderecoComNomeCurto(String nomeCurto) {
        return comEndereco(nomeCurto, endereco().logradouro(), endereco().numero(), endereco().bairro(),
                endereco().cidade(), endereco().observacoes());
    }

    private static NovoEndereco enderecoComLogradouro(String logradouro) {
        return comEndereco(endereco().nomeCurto(), logradouro, endereco().numero(), endereco().bairro(),
                endereco().cidade(), endereco().observacoes());
    }

    private static NovoEndereco enderecoComNumero(String numero) {
        return comEndereco(endereco().nomeCurto(), endereco().logradouro(), numero, endereco().bairro(),
                endereco().cidade(), endereco().observacoes());
    }

    private static NovoEndereco enderecoComBairro(String bairro) {
        return comEndereco(endereco().nomeCurto(), endereco().logradouro(), endereco().numero(), bairro,
                endereco().cidade(), endereco().observacoes());
    }

    private static NovoEndereco enderecoComCidade(String cidade) {
        return comEndereco(endereco().nomeCurto(), endereco().logradouro(), endereco().numero(), endereco().bairro(),
                cidade, endereco().observacoes());
    }

    private static NovoEndereco enderecoComObservacoes(String observacoes) {
        return comEndereco(endereco().nomeCurto(), endereco().logradouro(), endereco().numero(), endereco().bairro(),
                endereco().cidade(), observacoes);
    }

    private static NovoEndereco comEndereco(String nomeCurto, String logradouro, String numero, String bairro,
            String cidade, String observacoes) {
        NovoEndereco base = endereco();
        return new NovoEndereco(nomeCurto, base.tipoEndereco(), base.tipoLogradouro(), logradouro, numero, bairro,
                base.cep(), cidade, base.uf(), observacoes);
    }

    private static NovoCartao cartaoComNomeImpresso(String nomeImpresso) {
        NovoCartao base = br.fatec.les.livraria.clientes.ClientesDeTeste.cartao();
        return new NovoCartao(base.numero(), nomeImpresso, base.bandeiraId(), base.validade());
    }
}
