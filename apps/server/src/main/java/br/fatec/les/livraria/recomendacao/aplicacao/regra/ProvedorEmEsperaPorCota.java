package br.fatec.les.livraria.recomendacao.aplicacao.regra;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.recomendacao.dominio.calculo.CalcularEsperaDeCota;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Espera em memória de um provedor que respondeu 429 (RNF-REC-001, DEC-03): ele sai da cadeia pelo tempo sugerido pelo
 * provedor, entre 10 s e 5 min, ou até a meia-noite quando a cota diária se esgotou (REC-09), e volta sozinho depois. O
 * estado não é persistido; um reinício libera todos os provedores.
 */
public class ProvedorEmEsperaPorCota {

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

    /**
     * Marca o provedor como fora da cadeia a partir de agora. Sem tempo sugerido, usa 5 minutos; com cota diária,
     * espera até a meia-noite do {@link Relogio}.
     */
    public void registrarCotaEsgotada(String provedor, @Nullable Duration esperaSugerida, boolean cotaDiaria) {
        LocalDateTime agora = relogio.agora();
        liberaEm.put(provedor, agora.plus(CalcularEsperaDeCota.calcular(esperaSugerida, cotaDiaria, agora)));
    }

    /** Limpa as esperas, usado pelo reset dos E2E (DEC-09). */
    public void limpar() {
        liberaEm.clear();
    }
}
