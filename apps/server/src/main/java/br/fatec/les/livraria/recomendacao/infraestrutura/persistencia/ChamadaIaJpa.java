package br.fatec.les.livraria.recomendacao.infraestrutura.persistencia;

import br.fatec.les.livraria.recomendacao.dominio.modelo.OperacaoIa;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ResultadoDaChamada;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;

/**
 * Tabela {@code chamada_ia} do MER (RNF-REC-001, DEC-03, DEC-14): log só de inserção de cada tentativa da cadeia, com o
 * provedor, o resultado, a latência e os tokens. Não é auditada: é um log e não uma entidade de negócio alterável,
 * então não tem tabela {@code _aud} (DEC-03).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "chamada_ia",
        indexes = @Index(name = "ix_chamada_ia_provedor_iniciada", columnList = "provedor_ia_id, iniciada_em"))
public class ChamadaIaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chamada_ia_seq")
    @SequenceGenerator(
            name = "chamada_ia_seq",
            sequenceName = "chamada_ia_seq",
            initialValue = 10000,
            allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provedor_ia_id", nullable = false)
    private ProvedorIaJpa provedor;

    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "operacao", nullable = false, length = 20)
    private OperacaoIa operacao;

    @Column(name = "iniciada_em", nullable = false)
    private LocalDateTime iniciadaEm;

    @Column(name = "latencia_ms", nullable = false)
    private int latenciaMs;

    /** Número da tentativa na cadeia; 1 é o primeiro provedor efetivamente chamado (DEC-03). */
    @Column(name = "tentativa", nullable = false)
    private int tentativa;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private ResultadoDaChamada resultado;

    @Column(name = "status_http")
    private Integer statusHttp;

    /** Mensagem de erro já truncada em 500 caracteres pelo modelo de domínio (DEC-03). */
    @Column(name = "erro", length = 500)
    private String erro;

    @Column(name = "tokens_entrada")
    private Integer tokensEntrada;

    @Column(name = "tokens_saida")
    private Integer tokensSaida;

    /** Chamadas HTTP feitas ao provedor nesta mensagem; o fake grava 0 (DEC-03). */
    @Column(name = "chamadas_http", nullable = false)
    private int chamadasHttp;

    /** Tokens de entrada/saída de cada volta, separados por {@code ;}, com {@code -} quando não informados. */
    @Column(name = "tokens_das_voltas", length = 200)
    private String tokensDasVoltas;
}
