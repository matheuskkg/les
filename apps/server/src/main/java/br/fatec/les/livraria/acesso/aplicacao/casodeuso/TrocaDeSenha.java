package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.dominio.modelo.SenhaInformada;

/**
 * Comando da alteração de senha (RF-CLI-008, RNF-CLI-001, RNF-CLI-002). A senha atual é conferida contra o hash; a
 * nova senha é a que as regras de senha validam.
 */
public record TrocaDeSenha(String senhaAtual, String novaSenha, String confirmacaoSenha) implements SenhaInformada {

    @Override
    public String senha() {
        return novaSenha;
    }
}
