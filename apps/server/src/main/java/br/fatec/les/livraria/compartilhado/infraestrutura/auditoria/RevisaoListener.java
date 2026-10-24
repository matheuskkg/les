package br.fatec.les.livraria.compartilhado.infraestrutura.auditoria;

import org.hibernate.envers.RevisionListener;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Autor;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.aplicacao.porta.UsuarioAtual;
import lombok.RequiredArgsConstructor;

/**
 * Preenche o autor e a data/hora de cada revisão (RNF-GER-002, DEC-08) com as portas {@link UsuarioAtual} e
 * {@link Relogio}: id e e-mail do usuário autenticado, {@code anonimo} sem login e {@code sistema} fora de requisição
 * HTTP; a data/hora vem do fuso de {@code RelogioDoSistema}, respeitando o relógio ajustável dos testes E2E.
 *
 * <p>O Hibernate instancia este listener pelo {@code BeanContainer} do Spring, que o Spring Boot configura
 * automaticamente para o {@code EntityManagerFactory}. Por isso o construtor recebe as portas: a instância não é
 * um {@code @Component}, mas é criada com as dependências injetadas pelo container.</p>
 *
 * <p>O Envers preenche o campo {@code @RevisionTimestamp} antes de chamar o listener, então sobrescrevê-lo aqui
 * troca o relógio padrão da JVM pelo relógio da aplicação.</p>
 */
@RequiredArgsConstructor
public class RevisaoListener implements RevisionListener {

    private final UsuarioAtual usuarioAtual;
    private final Relogio relogio;

    @Override
    public void newRevision(Object revisao) {
        RevisaoJpa entidade = (RevisaoJpa) revisao;
        Autor autor = usuarioAtual.autor();
        entidade.setUsuarioId(autor.usuarioId());
        entidade.setUsuario(autor.usuario());
        entidade.setDataHora(relogio.agora());
    }
}
