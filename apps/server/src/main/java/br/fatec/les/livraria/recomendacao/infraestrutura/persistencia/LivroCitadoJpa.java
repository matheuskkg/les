package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Linha de {@code mensagem_livro} (RNF-REC-001, DEC-03): livro citado pela resposta do assistente e a posição na
 * resposta. {@code livro_id} é referência a outro módulo, só a coluna.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class LivroCitadoJpa {

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @Column(name = "posicao", nullable = false)
    private int posicao;
}
