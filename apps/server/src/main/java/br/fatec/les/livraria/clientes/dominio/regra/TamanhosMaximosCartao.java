package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * Tamanhos máximos dos dados do cartão (RN-CLI-004). As constantes espelham as colunas JPA de {@code cartao}; os campos
 * com formato próprio já têm regra que limita o tamanho: número (13 a 19 dígitos) e validade ({@code AAAA-MM}).
 */
@Component
public class TamanhosMaximosCartao implements Regra<NovoCartao> {

    public static final int NUMERO_DO_CARTAO = 19;
    public static final int NOME_IMPRESSO = 60;
    public static final int VALIDADE_DO_CARTAO = 7;

    private static final String CODIGO = "RN-CLI-004";

    @Override
    public void validar(NovoCartao cartao, Violacoes violacoes) {
        if (cartao == null) {
            return;
        }
        if (cartao.nomeImpresso() != null && cartao.nomeImpresso().strip().length() > NOME_IMPRESSO) {
            violacoes.adicionar(CODIGO, "O nome impresso deve ter no máximo " + NOME_IMPRESSO + " caracteres.");
        }
    }
}
