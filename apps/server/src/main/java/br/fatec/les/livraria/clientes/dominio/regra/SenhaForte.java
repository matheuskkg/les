package br.fatec.les.livraria.clientes.dominio.regra;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.clientes.dominio.modelo.NovoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Senha forte (RNF-CLI-001): pelo menos 8 caracteres, com letra maiúscula, letra minúscula e caractere especial. Também
 * limita a senha ao tamanho máximo aceito pelo BCrypt, em bytes UTF-8 (RNF-CLI-003).
 */
@Component
public class SenhaForte implements Regra<NovoCliente> {

    private static final String CODIGO = "RNF-CLI-001";
    private static final int TAMANHO_MINIMO = 8;

    @Override
    public void validar(NovoCliente alvo, Violacoes violacoes) {
        String senha = alvo.senha();
        if (senha == null || senha.isBlank()) {
            violacoes.adicionar(CODIGO, "Informe a senha do cliente.");
            return;
        }
        if (senha.length() < TAMANHO_MINIMO) {
            violacoes.adicionar(CODIGO, "A senha deve ter pelo menos 8 caracteres.");
        }
        if (senha.chars().noneMatch(Character::isUpperCase)) {
            violacoes.adicionar(CODIGO, "A senha deve ter pelo menos uma letra maiúscula.");
        }
        if (senha.chars().noneMatch(Character::isLowerCase)) {
            violacoes.adicionar(CODIGO, "A senha deve ter pelo menos uma letra minúscula.");
        }
        if (senha.chars().noneMatch(c -> !Character.isLetterOrDigit(c))) {
            violacoes.adicionar(CODIGO, "A senha deve ter pelo menos um caractere especial.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > CodificadorDeSenha.TAMANHO_MAXIMO_EM_BYTES) {
            violacoes.adicionar(CODIGO, "A senha deve ter no máximo "
                    + CodificadorDeSenha.TAMANHO_MAXIMO_EM_BYTES + " bytes.");
        }
    }
}
