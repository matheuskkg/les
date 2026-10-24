package br.fatec.les.livraria.clientes.dominio.regra;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoEndereco;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacao;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Endereços do cadastro do cliente (RN-CLI-003): percorre a lista e aplica {@link EnderecoCompleto} a cada item,
 * numerando as mensagens. A validação de um único endereço fica em {@code EnderecoCompleto} e é reutilizada pelos
 * casos de uso de adição e alteração (RF-CLI-006, RNF-CLI-004).
 */
@Component
@RequiredArgsConstructor
public class EnderecosCompletos implements Regra<NovoCliente> {

    private final EnderecoCompleto enderecoCompleto;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        List<NovoEndereco> enderecos = alvo.enderecos();
        for (int i = 0; i < enderecos.size(); i++) {
            String referencia = "Endereço " + (i + 1) + ": ";
            NovoEndereco endereco = enderecos.get(i);
            if (endereco == null) {
                violacoes.adicionar("RN-CLI-003", referencia + "informe os dados do endereço.");
                continue;
            }
            Violacoes doEndereco = new Violacoes();
            enderecoCompleto.validar(endereco, doEndereco);
            for (Violacao violacao : doEndereco.lista()) {
                violacoes.adicionar(violacao.codigo(), referencia + minusculaInicial(violacao.mensagem()));
            }
        }
    }

    private static String minusculaInicial(String mensagem) {
        return Character.toLowerCase(mensagem.charAt(0)) + mensagem.substring(1);
    }
}
