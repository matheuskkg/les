package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.NomeDoUsuario;
import br.fatec.les.livraria.acesso.dominio.modelo.Papel;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Nome exibido na sessão (DEC-08): o administrador aparece como "Administração"; os demais, pelo nome que algum módulo
 * conhece (o do cliente) ou, sem ele, pelo e-mail.
 */
@Component
@RequiredArgsConstructor
public class ConsultarNomeDeExibicao {

    static final String NOME_DO_ADMIN = "Administração";

    private final List<NomeDoUsuario> nomes;

    @Transactional(readOnly = true)
    public String executar(Long usuarioId, String email, Papel papel) {
        if (papel == Papel.ADMIN) {
            return NOME_DO_ADMIN;
        }
        return nomes.stream().map(nome -> nome.buscar(usuarioId)).flatMap(Optional::stream).findFirst().orElse(email);
    }
}
