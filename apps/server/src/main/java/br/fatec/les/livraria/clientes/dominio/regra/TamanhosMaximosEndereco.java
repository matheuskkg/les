package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * Tamanhos máximos dos dados do endereço (RN-CLI-003). As constantes espelham as colunas JPA de {@code endereco}; sem
 * esta regra, um valor maior que a coluna chegaria ao banco e viraria erro 500 em vez de uma violação 422. O CEP tem
 * regra própria de formato (8 dígitos).
 */
@Component
public class TamanhosMaximosEndereco implements Regra<NovoEndereco> {

    public static final int NOME_CURTO = 40;
    public static final int LOGRADOURO = 120;
    public static final int NUMERO_DO_ENDERECO = 10;
    public static final int BAIRRO = 80;
    public static final int CEP = 8;
    public static final int CIDADE = 80;
    public static final int OBSERVACOES = 255;

    private static final String CODIGO = "RN-CLI-003";

    @Override
    public void validar(NovoEndereco endereco, Violacoes violacoes) {
        if (endereco == null) {
            return;
        }
        limite(violacoes, "O nome curto", endereco.nomeCurto(), NOME_CURTO);
        limite(violacoes, "O logradouro", endereco.logradouro(), LOGRADOURO);
        limite(violacoes, "O número", endereco.numero(), NUMERO_DO_ENDERECO);
        limite(violacoes, "O bairro", endereco.bairro(), BAIRRO);
        limite(violacoes, "A cidade", endereco.cidade(), CIDADE);
        limite(violacoes, "As observações", endereco.observacoes(), OBSERVACOES);
    }

    private static void limite(Violacoes violacoes, String campo, String valor, int limite) {
        if (valor != null && valor.strip().length() > limite) {
            violacoes.adicionar(CODIGO, campo + " deve ter no máximo " + limite + " caracteres.");
        }
    }
}
