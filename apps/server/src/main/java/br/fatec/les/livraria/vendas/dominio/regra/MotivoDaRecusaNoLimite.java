package br.fatec.les.livraria.vendas.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.dominio.troca.RecusaDeTroca;

import org.springframework.stereotype.Component;

/**
 * O motivo da recusa tem até {@value RecusaDeTroca#TAMANHO_MAXIMO_DO_MOTIVO} caracteres, para caber inteiro na
 * notificação ao cliente (RF-VEN-012, DEC-12, DEC-13).
 */
@Component
public class MotivoDaRecusaNoLimite implements Regra<RecusaDeTroca> {

    @Override
    public void validar(RecusaDeTroca alvo, Violacoes violacoes) {
        if (alvo.motivo() != null && alvo.motivo().strip().length() > RecusaDeTroca.TAMANHO_MAXIMO_DO_MOTIVO) {
            violacoes.adicionar(
                    "RF-VEN-012",
                    "O motivo da recusa pode ter até %d caracteres.".formatted(RecusaDeTroca.TAMANHO_MAXIMO_DO_MOTIVO));
        }
    }
}
