package br.fatec.les.livraria.recomendacao.dominio.regra;

import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AlteracaoDaCadeia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PosicaoNaCadeia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ProvedorIa;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A alteração da cadeia traz cada provedor existente exatamente uma vez (RNF-REC-001, DEC-03): o ADMIN reordena e ativa
 * ou desativa, mas não cria nem remove provedores, que dependem de um adapter no server.
 */
@Component
public class CadeiaComTodosOsProvedores implements Regra<AlteracaoDaCadeia> {

    private static final String CODIGO = "RNF-REC-001";

    @Override
    public void validar(AlteracaoDaCadeia alvo, Violacoes violacoes) {
        Set<Long> existentes = alvo.atuais().stream().map(ProvedorIa::id).collect(Collectors.toSet());
        Set<Long> informados = new HashSet<>();
        boolean repetido = false;
        boolean desconhecido = false;
        for (PosicaoNaCadeia posicao : alvo.posicoes()) {
            if (posicao == null || posicao.id() == null || !existentes.contains(posicao.id())) {
                desconhecido = true;
            } else if (!informados.add(posicao.id())) {
                repetido = true;
            }
        }
        if (desconhecido) {
            violacoes.adicionar(CODIGO, "A cadeia só pode ter os provedores cadastrados.");
        }
        if (repetido) {
            violacoes.adicionar(CODIGO, "Cada provedor aparece uma única vez na cadeia.");
        }
        if (!informados.containsAll(existentes)) {
            violacoes.adicionar(CODIGO, "Informe todos os provedores da cadeia.");
        }
    }
}
