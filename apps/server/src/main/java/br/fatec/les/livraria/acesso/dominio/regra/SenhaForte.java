package br.fatec.les.livraria.acesso.dominio.regra;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.dominio.modelo.SenhaInformada;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Senha;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;

/**
 * Senha forte (RNF-CLI-001): pelo menos 8 caracteres, com letra maiúscula, letra minúscula e caractere especial.
 * Espaço em branco e caractere de controle não contam como especial e são rejeitados. Também limita a senha ao
 * tamanho máximo aceito pelo BCrypt, em bytes UTF-8 (RNF-CLI-003). Uma senha em branco é reportada pela regra de
 * dados obrigatórios da operação; esta regra só avalia o conteúdo das senhas preenchidas.
 */
@Component
public class SenhaForte implements Regra<SenhaInformada> {

    private static final String CODIGO = "RNF-CLI-001";
    private static final int TAMANHO_MINIMO = 8;

    @Override
    public void validar(SenhaInformada alvo, Violacoes violacoes) {
        String senha = alvo.senha();
        if (senha == null || senha.isBlank()) {
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
        if (senha.chars().anyMatch(SenhaForte::espaco)) {
            violacoes.adicionar(CODIGO, "A senha não pode conter espaços em branco.");
        }
        if (senha.chars().anyMatch(Character::isISOControl)) {
            violacoes.adicionar(CODIGO, "A senha não pode conter caracteres de controle.");
        }
        if (senha.chars().noneMatch(SenhaForte::caractereEspecial)) {
            violacoes.adicionar(CODIGO, "A senha deve ter pelo menos um caractere especial.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > Senha.TAMANHO_MAXIMO_EM_BYTES) {
            violacoes.adicionar(CODIGO, "A senha deve ter no máximo "
                    + Senha.TAMANHO_MAXIMO_EM_BYTES + " bytes.");
        }
    }

    /** Espaço comum, tabulação, quebra de linha e afins. */
    private static boolean espaco(int caractere) {
        return Character.isWhitespace(caractere) || Character.isSpaceChar(caractere);
    }

    /** Especial é tudo que não é letra, dígito, espaço em branco nem caractere de controle. */
    private static boolean caractereEspecial(int caractere) {
        return !Character.isLetterOrDigit(caractere) && !espaco(caractere)
                && !Character.isISOControl(caractere);
    }
}
