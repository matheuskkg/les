package br.fatec.les.livraria.acesso.dominio.regra;

import br.fatec.les.livraria.acesso.dominio.modelo.SenhaInformada;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

import org.springframework.stereotype.Component;

/**
 * Exige a nova senha na alteração de senha (RF-CLI-008, RNF-CLI-001). {@link SenhaForte} ignora uma senha em branco
 * para não repetir a violação do cadastro; por isso a alteração precisa desta regra para responder 422 quando a nova
 * senha não for informada.
 */
@Component
public class NovaSenhaObrigatoria implements Regra<SenhaInformada> {

    private static final String CODIGO = "RNF-CLI-001";

    @Override
    public void validar(SenhaInformada alvo, Violacoes violacoes) {
        if (alvo.senha() == null || alvo.senha().isBlank()) {
            violacoes.adicionar(CODIGO, "Informe a nova senha.");
        }
    }
}
