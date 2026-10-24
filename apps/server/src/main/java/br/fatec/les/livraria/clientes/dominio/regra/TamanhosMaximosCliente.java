package br.fatec.les.livraria.clientes.dominio.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCartao;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Tamanhos máximos dos dados do cadastro do cliente (RN-CLI-006). Os limites do endereço e do cartão ficam nas regras
 * próprias deles — {@link TamanhosMaximosEndereco} e {@link TamanhosMaximosCartao}, reutilizadas nos casos de uso de
 * adição e alteração (RF-CLI-006, RF-CLI-007) —, e aqui a lista do cadastro é percorrida com a numeração dos itens.
 * Os campos com formato próprio já têm regra que limita o tamanho: CEP (8 dígitos), CPF (11), e-mail (254), telefone
 * (DDD 2 e número 8 ou 9), número do cartão (13 a 19) e senha (72 bytes).
 */
@Component
@RequiredArgsConstructor
public class TamanhosMaximosCliente implements Regra<NovoCliente> {

    public static final int NOME = 120;
    public static final int CPF = 11;

    private static final String CODIGO = "RN-CLI-006";

    private final TamanhosMaximosEndereco tamanhosMaximosEndereco;
    private final TamanhosMaximosCartao tamanhosMaximosCartao;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        if (alvo.nome() != null && alvo.nome().strip().length() > NOME) {
            violacoes.adicionar(CODIGO, "O nome do cliente deve ter no máximo " + NOME + " caracteres.");
        }
        List<NovoEndereco> enderecos = alvo.enderecos();
        for (int i = 0; i < enderecos.size(); i++) {
            NovoEndereco endereco = enderecos.get(i);
            if (endereco == null) {
                continue;
            }
            Violacoes doEndereco = new Violacoes();
            tamanhosMaximosEndereco.validar(endereco, doEndereco);
            prefixar(violacoes, "Endereço " + (i + 1) + ": ", doEndereco);
        }
        List<NovoCartao> cartoes = alvo.cartoes();
        for (int i = 0; i < cartoes.size(); i++) {
            NovoCartao cartao = cartoes.get(i);
            if (cartao == null) {
                continue;
            }
            Violacoes doCartao = new Violacoes();
            tamanhosMaximosCartao.validar(cartao, doCartao);
            prefixar(violacoes, "Cartão " + (i + 1) + ": ", doCartao);
        }
    }

    private static void prefixar(Violacoes violacoes, String referencia, Violacoes doItem) {
        for (Violacao violacao : doItem.lista()) {
            violacoes.adicionar(violacao.codigo(), referencia + minusculaInicial(violacao.mensagem()));
        }
    }

    private static String minusculaInicial(String mensagem) {
        return Character.toLowerCase(mensagem.charAt(0)) + mensagem.substring(1);
    }
}
