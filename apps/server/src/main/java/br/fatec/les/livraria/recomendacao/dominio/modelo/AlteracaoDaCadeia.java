package br.fatec.les.livraria.recomendacao.dominio.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Nova ordem e ativação da cadeia de provedores pedidas pelo ADMIN (RNF-REC-001, DEC-03). A regra {@code
 * CadeiaComTodosOsProvedores} garante que a lista traz cada provedor existente uma única vez.
 *
 * @param atuais provedores gravados em {@code provedor_ia}
 * @param posicoes provedores na ordem desejada, com a ativação de cada um
 */
public record AlteracaoDaCadeia(List<ProvedorIa> atuais, List<PosicaoNaCadeia> posicoes) {

    public AlteracaoDaCadeia {
        atuais = List.copyOf(Objects.requireNonNull(atuais, "atuais"));
        posicoes = posicoes == null ? List.of() : new ArrayList<>(posicoes);
    }

    /** Os provedores com a ordem igual à posição na lista (a partir de 1) e a ativação informada. */
    public List<ProvedorIa> aplicar() {
        Map<Long, ProvedorIa> porId = atuais.stream().collect(Collectors.toMap(ProvedorIa::id, Function.identity()));
        List<ProvedorIa> alterados = new ArrayList<>();
        for (PosicaoNaCadeia posicao : posicoes) {
            ProvedorIa atual = porId.get(posicao.id());
            alterados.add(new ProvedorIa(atual.id(), atual.nome(), alterados.size() + 1, posicao.ativo()));
        }
        return alterados;
    }
}
