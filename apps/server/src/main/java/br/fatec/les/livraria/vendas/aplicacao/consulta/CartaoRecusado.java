package br.fatec.les.livraria.vendas.aplicacao.consulta;

import java.util.Objects;

/** Cartão recusado pela operadora e o motivo, para o cliente corrigir e tentar de novo (RN-VEN-007, DEC-05.2). */
public record CartaoRecusado(String ultimosDigitos, String motivo) {

    public CartaoRecusado {
        Objects.requireNonNull(ultimosDigitos, "ultimosDigitos");
        Objects.requireNonNull(motivo, "motivo");
    }
}
