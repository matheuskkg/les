package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.time.LocalDateTime;

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
 * Tabela {@code item_removido_carrinho} do MER (DEC-16, RNF-VEN-001): livros retirados do carrinho por expiração,
 * um por livro ({@code UNIQUE (carrinho_id, livro_id)}). O {@code livro_id} é referência ao módulo {@code catalogo}
 * só pelo id, sem FK.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "item_removido_carrinho",
        uniqueConstraints = @UniqueConstraint(name = "uk_item_removido_carrinho_livro", columnNames = {
                "carrinho_id", "livro_id" }))
public class ItemRemovidoCarrinhoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "item_removido_carrinho_seq")
    @SequenceGenerator(name = "item_removido_carrinho_seq", sequenceName = "item_removido_carrinho_seq",
            initialValue = 10000, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrinho_id", nullable = false)
    private CarrinhoJpa carrinho;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @Column(name = "quantidade", nullable = false)
    private int quantidade;

    @Column(name = "removido_em", nullable = false)
    private LocalDateTime removidoEm;
}
