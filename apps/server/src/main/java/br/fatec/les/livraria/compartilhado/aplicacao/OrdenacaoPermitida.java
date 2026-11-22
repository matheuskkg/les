package br.fatec.les.livraria.compartilhado.aplicacao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Campos que uma listagem aceita em {@code ordem}, com o nome exposto na API e a propriedade correspondente no modelo,
 * além da ordem padrão usada quando o cliente não informa nenhum critério (DEC-07).
 */
public record OrdenacaoPermitida(Map<String, String> campos, List<Ordem> padrao) {

    public OrdenacaoPermitida {
        campos = Collections.unmodifiableMap(new LinkedHashMap<>(campos));
        padrao = padrao == null ? List.of() : List.copyOf(padrao);
        for (Ordem ordem : padrao) {
            if (!campos.containsKey(ordem.campo())) {
                throw new IllegalArgumentException("Ordem padrão não declarada para o campo " + ordem.campo() + ".");
            }
        }
    }

    /** Declara campos cujo nome na API é igual ao nome da propriedade no modelo. */
    public static OrdenacaoPermitida de(List<Ordem> padrao, String... campos) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (String campo : campos) {
            mapa.put(campo, campo);
        }
        return new OrdenacaoPermitida(mapa, padrao);
    }

    /** Declara um campo cujo nome na API difere do nome da propriedade no modelo. */
    public OrdenacaoPermitida com(String campoDaApi, String propriedade) {
        Map<String, String> mapa = new LinkedHashMap<>(campos);
        mapa.put(campoDaApi, propriedade);
        return new OrdenacaoPermitida(mapa, padrao);
    }

    /** Traduz o campo da API para a propriedade do modelo; campo não declarado causa HTTP 400. */
    public String propriedade(String campo) {
        String propriedade = campos.get(campo);
        if (propriedade == null) {
            throw new ParametroInvalido(
                    "ordem",
                    "Campo de ordenação não permitido: "
                            + campo
                            + ". Campos permitidos: "
                            + String.join(", ", campos.keySet())
                            + ".");
        }
        return propriedade;
    }
}
