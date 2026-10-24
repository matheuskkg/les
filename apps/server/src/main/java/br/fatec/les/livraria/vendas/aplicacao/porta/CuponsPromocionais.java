package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.util.Optional;

import br.fatec.les.livraria.vendas.dominio.pagamento.CupomPromocional;

/** Cupons promocionais e o uso deles por cliente (RF-VEN-007, DEC-11, DEC-15.2). */
public interface CuponsPromocionais {

    /** @param codigo código já normalizado em maiúsculas */
    Optional<CupomPromocional> buscarPorCodigo(String codigo);

    /** Se o cliente já tem um uso registrado do promocional (DEC-15.2). */
    boolean usadoPeloCliente(Long cupomPromocionalId, Long clienteId);
}
