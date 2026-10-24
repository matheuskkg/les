package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import org.hibernate.envers.Audited;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code uso_cupom_promocional} do MER (DEC-15.2): cada cliente usa cada promocional uma vez, garantido pelo
 * {@code UNIQUE (cupom_promocional_id, cliente_id)}. O {@code cliente_id} é referência ao módulo {@code clientes} só
 * pelo id; o {@code pedido_id} aponta o pedido que usou o cupom.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "uso_cupom_promocional", uniqueConstraints = {
        @UniqueConstraint(name = "uk_uso_cupom_promocional_cliente",
                columnNames = { "cupom_promocional_id", "cliente_id" }),
        @UniqueConstraint(name = "uk_uso_cupom_promocional_pedido", columnNames = "pedido_id") })
public class UsoCupomPromocionalJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "uso_cupom_promocional_seq")
    @SequenceGenerator(name = "uso_cupom_promocional_seq", sequenceName = "uso_cupom_promocional_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cupom_promocional_id", nullable = false)
    private CupomPromocionalJpa cupomPromocional;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;
}
