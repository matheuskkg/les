package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;
import br.fatec.les.livraria.compartilhado.dominio.Senha;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** BCrypt com fator de custo 12 (RNF-CLI-003). */
@Component
public class CodificadorDeSenhaBCrypt implements CodificadorDeSenha {

    private static final int FORCA = 12;

    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder(FORCA);

    @Override
    public String codificar(String senha) {
        return codificador.encode(senha);
    }

    @Override
    public boolean confere(String senha, String senhaHash) {
        if (senha == null
                || senhaHash == null
                || senha.getBytes(StandardCharsets.UTF_8).length > Senha.TAMANHO_MAXIMO_EM_BYTES) {
            return false;
        }
        return codificador.matches(senha, senhaHash);
    }
}
