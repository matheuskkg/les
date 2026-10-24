package br.fatec.les.livraria.compartilhado.infraestrutura.auditoria;

import org.hibernate.envers.RevisionListener;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import lombok.RequiredArgsConstructor;

/**
 * Preenche o autor de cada revisão (RNF-GER-002, DEC-08) com a porta {@link UsuarioAtual}: id e e-mail do
 * usuário autenticado, {@code anonimo} sem login e {@code sistema} fora de requisição HTTP.
 *
 * <p>O Hibernate instancia este listener pelo {@code BeanContainer} do Spring, que o Spring Boot configura
 * automaticamente para o {@code EntityManagerFactory}. Por isso o construtor recebe a porta: a instância não é
 * um {@code @Component}, mas é criada com as dependências injetadas pelo container.</p>
 */
@RequiredArgsConstructor
public class RevisaoListener implements RevisionListener {

    private final UsuarioAtual usuarioAtual;

    @Override
    public void newRevision(Object revisao) {
        RevisaoJpa entidade = (RevisaoJpa) revisao;
        entidade.setUsuario(usuarioAtual.usuario());
        entidade.setUsuarioId(usuarioAtual.usuarioId().orElse(null));
    }
}
