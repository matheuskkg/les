package br.fatec.les.livraria.clientes.dominio.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Tamanhos máximos dos dados do cadastro do cliente (RN-CLI-006, RN-CLI-003, RN-CLI-004). As constantes espelham as
 * colunas JPA de {@code cliente}, {@code endereco} e {@code cartao}; sem esta regra, um valor maior que a coluna
 * chegaria ao banco e viraria erro 500 em vez de uma violação 422. Os campos com formato próprio já têm regra que
 * limita o tamanho: CEP (8 dígitos), CPF (11), e-mail (254), telefone (DDD 2 e número 8 ou 9), número do cartão (13 a
 * 19) e senha (72 bytes).
 */
@Component
public class TamanhosMaximosCliente implements Regra<NovoCliente> {

    public static final int NOME = 120;
    public static final int CPF = 11;
    public static final int NOME_CURTO = 40;
    public static final int LOGRADOURO = 120;
    public static final int NUMERO_DO_ENDERECO = 10;
    public static final int BAIRRO = 80;
    public static final int CEP = 8;
    public static final int CIDADE = 80;
    public static final int OBSERVACOES = 255;
    public static final int NUMERO_DO_CARTAO = 19;
    public static final int NOME_IMPRESSO = 60;
    public static final int VALIDADE_DO_CARTAO = 7;

    private static final String CLIENTE = "RN-CLI-006";
    private static final String ENDERECO = "RN-CLI-003";
    private static final String CARTAO = "RN-CLI-004";

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (excede(alvo.nome(), NOME)) {
            violacoes.adicionar(CLIENTE, "O nome do cliente deve ter no máximo " + NOME + " caracteres.");
        }
        List<NovoEndereco> enderecos = alvo.enderecos();
        for (int i = 0; i < enderecos.size(); i++) {
            NovoEndereco endereco = enderecos.get(i);
            if (endereco == null) {
                continue;
            }
            String referencia = "Endereço " + (i + 1) + ": ";
            limite(violacoes, ENDERECO, referencia, "o nome curto", endereco.nomeCurto(), NOME_CURTO);
            limite(violacoes, ENDERECO, referencia, "o logradouro", endereco.logradouro(), LOGRADOURO);
            limite(violacoes, ENDERECO, referencia, "o número", endereco.numero(), NUMERO_DO_ENDERECO);
            limite(violacoes, ENDERECO, referencia, "o bairro", endereco.bairro(), BAIRRO);
            limite(violacoes, ENDERECO, referencia, "a cidade", endereco.cidade(), CIDADE);
            limite(violacoes, ENDERECO, referencia, "as observações", endereco.observacoes(), OBSERVACOES);
        }
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            if (cartao == null) {
                continue;
            }
            limite(violacoes, CARTAO, "Cartão " + (i + 1) + ": ", "o nome impresso", cartao.nomeImpresso(),
                    NOME_IMPRESSO);
        }
    }

    private static void limite(Violacoes violacoes, String codigo, String referencia, String campo, String valor,
            int limite) {
        if (excede(valor, limite)) {
            violacoes.adicionar(codigo, referencia + campo + " deve ter no máximo " + limite + " caracteres.");
        }
    }

    private static boolean excede(String valor, int limite) {
        return valor != null && valor.strip().length() > limite;
    }
}
