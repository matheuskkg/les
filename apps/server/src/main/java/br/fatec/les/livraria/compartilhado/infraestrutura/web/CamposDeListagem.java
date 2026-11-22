package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import br.fatec.les.livraria.compartilhado.aplicacao.Ordem;
import br.fatec.les.livraria.compartilhado.aplicacao.OrdenacaoPermitida;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interpreta a configuração de {@link Listagem} uma única vez para o MVC e para o OpenAPI (DEC-07). Erros aqui são de
 * programação: a anotação é validada na inicialização, nunca em resposta a um parâmetro da requisição.
 */
final class CamposDeListagem {

    private CamposDeListagem() {}

    /** Lê as entradas {@code campoDaApi} e {@code campoDaApi=propriedadeDoModelo} da anotação. */
    static Map<String, String> de(Listagem listagem) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (String entrada : listagem.campos()) {
            if (entrada == null) {
                throw new IllegalArgumentException("@Listagem.campos() contém uma entrada nula.");
            }
            String[] partes = entrada.split("=", -1);
            if (partes.length > 2) {
                throw new IllegalArgumentException(
                        "Campo de listagem inválido: \""
                                + entrada
                                + "\". Use campoDaApi ou campoDaApi=propriedadeDoModelo.");
            }
            String daApi = partes[0].trim();
            String propriedade = (partes.length == 2 ? partes[1] : partes[0]).trim();
            if (daApi.isEmpty() || propriedade.isEmpty()) {
                throw new IllegalArgumentException(
                        "Campo de listagem inválido: \""
                                + entrada
                                + "\". O nome na API e a propriedade do modelo não podem ficar em branco.");
            }
            if (campos.putIfAbsent(daApi, propriedade) != null) {
                throw new IllegalArgumentException("Campo de listagem repetido: \"" + daApi + "\".");
            }
        }
        return Collections.unmodifiableMap(campos);
    }

    /** Nomes dos campos como aparecem na API, na ordem declarada; usado na descrição do OpenAPI. */
    static List<String> nomesDaApi(Listagem listagem) {
        return List.copyOf(de(listagem).keySet());
    }

    /** Monta a ordenação permitida e valida a ordem padrão declarada. */
    static OrdenacaoPermitida ordenacao(Listagem listagem) {
        Map<String, String> campos = de(listagem);
        List<Ordem> padrao = new ArrayList<>();
        for (String valor : listagem.padrao()) {
            try {
                padrao.add(Ordem.de(valor));
            } catch (ParametroInvalido excecao) {
                throw new IllegalArgumentException(
                        "Ordem padrão inválida \"" + valor + "\": " + excecao.getMessage(), excecao);
            }
        }
        return new OrdenacaoPermitida(campos, padrao);
    }
}
