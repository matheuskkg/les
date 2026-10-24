package br.fatec.les.livraria.acesso.dominio.modelo;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

/**
 * Calcula a situação da conta (RF-CLI-003, DEC-10): ativa sem inativação; inativa até 7 dias depois dela; encerrada
 * depois desse prazo. O prazo fica centralizado no módulo {@code acesso}.
 */
@Component
public class SituacaoDaConta {

    /** Prazo em que o login com a senha correta ainda reativa a conta (DEC-10). */
    public static final Duration PRAZO_DE_REATIVACAO = Duration.ofDays(7);

    public SituacaoConta calcular(Usuario usuario, LocalDateTime agora) {
        LocalDateTime inativadoEm = usuario.getInativadoEm();
        if (inativadoEm == null) {
            return SituacaoConta.ATIVA;
        }
        // "há no máximo 7 dias" ainda é INATIVA; "há mais de 7 dias" é ENCERRADA.
        return inativadoEm.plus(PRAZO_DE_REATIVACAO).isBefore(agora)
                ? SituacaoConta.ENCERRADA
                : SituacaoConta.INATIVA;
    }
}
