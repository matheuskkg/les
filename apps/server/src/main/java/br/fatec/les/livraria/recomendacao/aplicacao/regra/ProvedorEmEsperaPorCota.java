package br.fatec.les.livraria.recomendacao.aplicacao.regra;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;

/**
 * Espera em memória de um provedor que respondeu 429 (RNF-REC-001, DEC-03): ele sai da cadeia por 5 minutos e volta
 * sozinho depois. O estado não é persistido; um reinício libera todos os provedores.
 */
public class ProvedorEmEsperaPorCota {

    static final Duration ESPERA = Duration.ofMinutes(5);

    private final Relogio relogio;
    private final Map<String, LocalDateTime> liberaEm = new ConcurrentHashMap<>();

    public ProvedorEmEsperaPorCota(Relogio relogio) {
        this.relogio = relogio;
    }

    /** Indica se o provedor ainda está em espera; ao vencer o prazo, a entrada é removida. */
    public boolean emEspera(String provedor) {
        LocalDateTime limite = liberaEm.get(provedor);
        if (limite == null) {
            return false;
        }
        if (relogio.agora().isBefore(limite)) {
            return true;
        }
        liberaEm.remove(provedor, limite);
        return false;
    }

    /** Marca o provedor como fora da cadeia por 5 minutos a partir de agora. */
    public void registrarCotaEsgotada(String provedor) {
        liberaEm.put(provedor, relogio.agora().plus(ESPERA));
    }

    /** Limpa as esperas, usado pelo reset dos E2E (DEC-09). */
    public void limpar() {
        liberaEm.clear();
    }
}
