package br.fatec.les.livraria.estoque.dominio.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Bloqueio temporário de unidades de um livro para um carrinho ou um checkout (RN-VEN-014, RN-EST-006, DEC-16). O dono
 * da reserva é opaco para o {@code estoque}: só a origem e o id dele (DEC-16).
 *
 * <p>Uma reserva está ativa enquanto {@code expiraEm} estiver no futuro; ela é o que consome o disponível (DEC-15.3).
 */
public class Reserva {

    private final Long id;
    private final Long livroId;
    private OrigemReserva origem;
    private Long origemId;
    private int quantidade;
    private LocalDateTime expiraEm;

    public Reserva(Long id, Long livroId, OrigemReserva origem, Long origemId, int quantidade, LocalDateTime expiraEm) {
        this.id = id;
        this.livroId = Objects.requireNonNull(livroId, "livroId");
        this.origem = Objects.requireNonNull(origem, "origem");
        this.origemId = Objects.requireNonNull(origemId, "origemId");
        this.quantidade = exigirQuantidade(quantidade);
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm");
    }

    /**
     * @return reserva nova, ainda sem id.
     */
    public static Reserva nova(
            Long livroId, OrigemReserva origem, Long origemId, int quantidade, LocalDateTime expiraEm) {
        return new Reserva(null, livroId, origem, origemId, quantidade, expiraEm);
    }

    /** Altera a quantidade bloqueada e o prazo, mantendo o mesmo dono (RN-VEN-014, RF-VEN-002). */
    public void alterar(int quantidade, LocalDateTime expiraEm) {
        this.quantidade = exigirQuantidade(quantidade);
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm");
    }

    /** Renova o prazo do bloqueio quando o carrinho recebe um item novo (DEC-05.1). */
    public void renovarPrazo(LocalDateTime expiraEm) {
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm");
    }

    /**
     * Passa a reserva a outro dono com a quantidade do item e um novo prazo, como do carrinho ao checkout (DEC-12,
     * DEC-16).
     */
    public void transferir(OrigemReserva origem, Long origemId, int quantidade, LocalDateTime expiraEm) {
        this.origem = Objects.requireNonNull(origem, "origem");
        this.origemId = Objects.requireNonNull(origemId, "origemId");
        alterar(quantidade, expiraEm);
    }

    public boolean ativaEm(LocalDateTime agora) {
        return expiraEm.isAfter(agora);
    }

    private static int exigirQuantidade(int quantidade) {
        if (quantidade < 1) {
            throw new IllegalArgumentException("A quantidade reservada deve ser no mínimo 1.");
        }
        return quantidade;
    }

    public Long getId() {
        return id;
    }

    public Long getLivroId() {
        return livroId;
    }

    public OrigemReserva getOrigem() {
        return origem;
    }

    public Long getOrigemId() {
        return origemId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }
}
