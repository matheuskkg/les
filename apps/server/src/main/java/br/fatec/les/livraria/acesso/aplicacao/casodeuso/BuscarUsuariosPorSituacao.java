package br.fatec.les.livraria.acesso.aplicacao.casodeuso;

import br.fatec.les.livraria.acesso.aplicacao.porta.UsuarioRepositorio;
import br.fatec.les.livraria.acesso.dominio.modelo.SituacaoConta;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

/**
 * Consulta pública do {@code acesso} que resolve os ids dos usuários nas situações informadas (RF-CLI-004, DEC-10). A
 * situação é derivada de {@code inativado_em} e do prazo de reativação: a comparação é feita em SQL, sem carregar todos
 * os usuários.
 */
@Component
@RequiredArgsConstructor
public class BuscarUsuariosPorSituacao {

    private final UsuarioRepositorio usuarios;

    /**
     * @param situacoes situações desejadas, combinadas com OU; vazio não filtra
     * @param agora instante da consulta, o mesmo usado para exibir a situação dos itens da página
     * @return os ids encontrados; vazio quando nenhuma conta está nas situações pedidas
     */
    @Transactional(readOnly = true)
    public Set<Long> executar(Collection<SituacaoConta> situacoes, LocalDateTime agora) {
        if (situacoes == null || situacoes.isEmpty()) {
            return Set.of();
        }
        return usuarios.listarIdsPorSituacao(situacoes, agora);
    }
}
