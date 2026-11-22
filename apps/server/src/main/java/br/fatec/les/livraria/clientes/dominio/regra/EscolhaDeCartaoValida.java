package br.fatec.les.livraria.clientes.dominio.regra;

import br.fatec.les.livraria.clientes.dominio.modelo.EscolhaDeCartao;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * Escolha do cartão da compra (RF-VEN-006, DEC-10): um cartão cadastrado ou um novo, nunca os dois nem nenhum. Os dados
 * do novo são validados pelas regras do próprio cartão (RN-CLI-004, RN-CLI-005, DEC-06).
 */
@Component
public class EscolhaDeCartaoValida implements Regra<EscolhaDeCartao> {

    @Override
    public void validar(EscolhaDeCartao escolha, Violacoes violacoes) {
        if (escolha == null || (escolha.cartaoId() == null) == (escolha.novo() == null)) {
            violacoes.adicionar("RF-VEN-006", "Escolha um cartão cadastrado ou informe um novo.");
        }
    }
}
