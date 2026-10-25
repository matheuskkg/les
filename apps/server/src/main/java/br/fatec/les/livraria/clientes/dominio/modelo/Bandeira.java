package br.fatec.les.livraria.clientes.dominio.modelo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Bandeira de cartão de crédito registrada no sistema (RN-CLI-005, DEC-16, DEC-18). Os prefixos e os tamanhos vêm da
 * tabela do seed: o número do cartão determina a bandeira, e a bandeira determina os comprimentos aceitos.
 */
public record Bandeira(Long id, String nome, List<PrefixoDeCartao> prefixos, List<Integer> tamanhos) {

    public Bandeira {
        prefixos = List.copyOf(Objects.requireNonNull(prefixos, "prefixos"));
        tamanhos = List.copyOf(Objects.requireNonNull(tamanhos, "tamanhos"));
    }

    /**
     * Bandeira só com id e nome, para leitura de um cartão já persistido: a FK basta e a leitura não depende de
     * interpretar as colunas do seed (DEC-18).
     */
    public Bandeira(Long id, String nome) {
        this(id, nome, List.of(), List.of());
    }

    /**
     * Monta a bandeira a partir das colunas do seed/mapper: {@code prefixos} como {@code 4} ou
     * {@code 51-55,2221-2720} e {@code tamanhos} como {@code 13,16,19}.
     */
    public static Bandeira de(Long id, String nome, String prefixos, String tamanhos) {
        return new Bandeira(id, nome, prefixosDe(prefixos), tamanhosDe(tamanhos));
    }

    private static List<PrefixoDeCartao> prefixosDe(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return Arrays.stream(texto.split(","))
                .map(String::strip)
                .filter(item -> !item.isEmpty())
                .map(PrefixoDeCartao::de)
                .toList();
    }

    private static List<Integer> tamanhosDe(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return Arrays.stream(texto.split(","))
                .map(String::strip)
                .filter(item -> !item.isEmpty())
                .map(Integer::valueOf)
                .toList();
    }
}
