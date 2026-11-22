package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.RecusaDeTroca;

import org.springframework.stereotype.Component;

/** A recusa da troca exige um motivo, que vai na notificação ao cliente (RF-VEN-012, DEC-12, DEC-13). */
@Component
public class MotivoDaRecusaInformado implements Regra<RecusaDeTroca> {

    @Override
    public void validar(RecusaDeTroca alvo, Violacoes violacoes) {
        if (alvo.motivo() == null || alvo.motivo().isBlank()) {
            violacoes.adicionar("RF-VEN-012", "Informe o motivo da recusa da troca.");
        }
    }
}
