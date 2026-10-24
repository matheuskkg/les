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
 * Tabela {@code item_checkout} do MER (DEC-16): uma linha por livro no checkout ({@code UNIQUE (checkout_id,
 * livro_id)}). O {@code livro_id} é referência ao módulo {@code catalogo} só pelo id, sem FK.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "item_checkout",
        uniqueConstraints = @UniqueConstraint(name = "uk_item_checkout_livro", columnNames = { "checkout_id",
                "livro_id" }))
public class ItemCheckoutJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_checkout_seq")
    @SequenceGenerator(name = "item_checkout_seq", sequenceName = "item_checkout_seq", initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkout_id", nullable = false)
    private CheckoutJpa checkout;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;
}
