package br.fatec.les.livraria.acesso.infraestrutura.seguranca;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.porta.CodificadorDeSenha;

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
        return codificador.matches(senha, senhaHash);
    }
}
