package br.fatec.les.livraria.vendas.infraestrutura.persistencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabela {@code carrinho} do MER (DEC-14, DEC-16): um carrinho por cliente. O {@code cliente_id} é referência ao
 * módulo {@code clientes} só pelo id, sem FK (DEC-16). O índice em {@code expira_em} atende à expiração (VEN-02).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "carrinho", indexes = {
        @Index(name = "ux_carrinho_cliente", columnList = "cliente_id", unique = true),
        @Index(name = "ix_carrinho_expira_em", columnList = "expira_em") })
public class CarrinhoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "carrinho_seq")
    @SequenceGenerator(name = "carrinho_seq", sequenceName = "carrinho_seq", initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @Column(name = "aviso_enviado_em")
    private LocalDateTime avisoEnviadoEm;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemCarrinhoJpa> itens = new ArrayList<>();
}
