package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.recomendacao.aplicacao.consulta.ProvedorConfigurado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.MetricasIaRepositorio;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.UltimaFalha;

/**
 * Cadeia de provedores de IA para o ADMIN (RNF-REC-001, DEC-03): ordem, ativação, modelo, se há chave configurada e
 * a última falha de cada um. A chave vem do adapter de mesmo nome; um provedor sem adapter aparece sem chave.
 */
@Component
public class ConsultarProvedoresIa {

    private final ProvedoresIaRepositorio provedores;
    private final MetricasIaRepositorio metricas;
    private final Map<String, AssistenteDeLivros> assistentesPorNome;

    public ConsultarProvedoresIa(
            List<AssistenteDeLivros> assistentes,
            ProvedoresIaRepositorio provedores,
            MetricasIaRepositorio metricas) {
        this.assistentesPorNome = assistentes.stream()
                .collect(Collectors.toUnmodifiableMap(AssistenteDeLivros::nome, Function.identity()));
        this.provedores = provedores;
        this.metricas = metricas;
    }

    @Transactional(readOnly = true)
    public List<ProvedorConfigurado> executar() {
        Map<Long, UltimaFalha> falhas = metricas.ultimasFalhas();
        return provedores.listarEmOrdem().stream()
                .map(provedor -> {
                    AssistenteDeLivros assistente = assistentesPorNome.get(provedor.nome());
                    return new ProvedorConfigurado(
                            provedor,
                            assistente == null ? null : assistente.modelo(),
                            assistente != null && assistente.temChave(),
                            falhas.get(provedor.id()));
                })
                .toList();
    }
}
