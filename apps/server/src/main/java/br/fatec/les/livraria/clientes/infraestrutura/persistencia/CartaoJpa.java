package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.envers.Audited;

import br.fatec.les.livraria.clientes.dominio.regra.TamanhosMaximosCliente;

/**
 * Tabela {@code cartao} do MER (RN-CLI-004, RN-CLI-005, DEC-06, DEC-10, DEC-14, DEC-16). Guarda a validade como
 * {@code AAAA-MM} e não guarda o código de segurança. Bandeira e cliente são FKs do próprio módulo.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "cartao", indexes = {
        @Index(name = "ix_cartao_cliente_id", columnList = "cliente_id"),
        @Index(name = "ix_cartao_bandeira_id", columnList = "bandeira_id")
})
public class CartaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cartao_seq")
    @SequenceGenerator(name = "cartao_seq", sequenceName = "cartao_seq", initialValue = 10000,
            allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteJpa cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bandeira_id", nullable = false)
    private BandeiraJpa bandeira;

    @Column(name = "numero", nullable = false, length = TamanhosMaximosCliente.NUMERO_DO_CARTAO)
    private String numero;

    @Column(name = "nome_impresso", nullable = false, length = TamanhosMaximosCliente.NOME_IMPRESSO)
    private String nomeImpresso;

    /** Validade no formato {@code AAAA-MM} (DEC-07, DEC-06). */
    @Column(name = "validade", nullable = false, length = TamanhosMaximosCliente.VALIDADE_DO_CARTAO)
    private String validade;

    @Column(name = "preferencial", nullable = false)
    private boolean preferencial;
}
