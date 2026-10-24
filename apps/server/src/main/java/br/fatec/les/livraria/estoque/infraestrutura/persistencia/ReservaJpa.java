package br.fatec.les.livraria.estoque.infraestrutura.persistencia;

import java.time.LocalDateTime;

import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
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
 * Bloqueio temporário de estoque do carrinho ou do checkout (RN-VEN-014, DEC-16). O
 * {@code livro_id} é referência a outro módulo, sem {@code @ManyToOne} e sem FK.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reserva", indexes = {
        @Index(name = "ix_reserva_livro_expira_em", columnList = "livro_id, expira_em"),
        @Index(name = "ix_reserva_origem", columnList = "origem, origem_id") })
public class ReservaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reserva_seq")
    @SequenceGenerator(name = "reserva_seq", sequenceName = "reserva_seq", initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 20)
    private OrigemReserva origem;

    @Column(name = "origem_id", nullable = false)
    private Long origemId;
}
