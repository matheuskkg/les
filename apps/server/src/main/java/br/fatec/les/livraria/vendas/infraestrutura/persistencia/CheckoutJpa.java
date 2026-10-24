package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;

import br.fatec.les.livraria.vendas.dominio.modelo.SituacaoCheckout;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code checkout} do MER (DEC-12, DEC-16): a sessão de compra, com {@code @Version} contra duas finalizações
 * simultâneas (DEC-13). O {@code cliente_id} é referência ao módulo {@code clientes} só pelo id, sem FK. Os índices
 * atendem à busca do checkout aberto do cliente e à tarefa de expiração.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "checkout", indexes = {
        @Index(name = "ix_checkout_cliente_situacao", columnList = "cliente_id, situacao"),
        @Index(name = "ix_checkout_situacao_expira_em", columnList = "situacao, expira_em") })
public class CheckoutJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "checkout_seq")
    @SequenceGenerator(name = "checkout_seq", sequenceName = "checkout_seq", initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoCheckout situacao;

    @Column(name = "iniciado_em", nullable = false)
    private LocalDateTime iniciadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "aviso_enviado_em")
    private LocalDateTime avisoEnviadoEm;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "checkout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemCheckoutJpa> itens = new ArrayList<>();
}
