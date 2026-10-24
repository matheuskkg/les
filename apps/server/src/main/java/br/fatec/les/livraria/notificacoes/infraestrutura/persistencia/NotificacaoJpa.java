package br.fatec.les.livraria.notificacoes.infraestrutura.persistencia;

import java.time.LocalDateTime;

import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.fatec.les.livraria.notificacoes.dominio.modelo.TipoNotificacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code notificacao} do MER (DEC-14, DEC-16). O {@code usuario_id} é referência ao módulo {@code acesso}
 * apenas pelo id, sem {@code @ManyToOne} e sem FK (DEC-16). Os índices atendem à listagem por usuário (não lidas
 * primeiro), à contagem e ao reenvio por {@code Last-Event-ID} (DEC-13, RNF-GER-001).
 */
@Audited
@Entity
@Table(name = "notificacao", indexes = {
        @Index(name = "ix_notificacao_usuario_lida", columnList = "usuario_id, lida_em, id"),
        @Index(name = "ix_notificacao_usuario_id", columnList = "usuario_id, id")
})
@Getter
@Setter
@NoArgsConstructor
public class NotificacaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificacao_seq")
    @SequenceGenerator(name = "notificacao_seq", sequenceName = "notificacao_seq", initialValue = 10000,
            allocationSize = 1)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoNotificacao tipo;

    @Column(name = "mensagem", nullable = false, length = 500)
    private String mensagem;

    /** Referência opcional a um recurso de outro módulo (por exemplo {@code pedido}); sempre acompanhada do id. */
    @Column(name = "recurso", length = 40)
    private String recurso;

    @Column(name = "recurso_id")
    private Long recursoId;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    /** Nulo enquanto a notificação não foi lida. */
    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    /**
     * 0 para não lida e 1 para lida, calculado pelo banco. Ordena as não lidas primeiro sem depender de onde cada banco
     * põe os nulos de {@code lida_em} (DEC-13).
     */
    @NotAudited
    @Formula("case when lida_em is null then 0 else 1 end")
    private int lida;
}
