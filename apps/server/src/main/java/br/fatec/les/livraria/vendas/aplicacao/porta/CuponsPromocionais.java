package br.fatec.les.livraria.vendas.aplicacao.porta;

import br.fatec.les.livraria.vendas.dominio.pagamento.CupomPromocional;

import java.util.Optional;

/** Cupons promocionais e o uso deles por cliente (RF-VEN-007, DEC-11, DEC-15.2). */
public interface CuponsPromocionais {

    /**
     * @param codigo código já normalizado em maiúsculas
     */
    Optional<CupomPromocional> buscarPorCodigo(String codigo);

    /** Se o cliente já tem um uso registrado do promocional (DEC-15.2). */
    boolean usadoPeloCliente(Long cupomPromocionalId, Long clienteId);

    /**
     * Registra o uso do promocional pelo cliente no pedido (DEC-15.2).
     *
     * @throws org.springframework.dao.DataIntegrityViolationException se o cliente já tiver um uso registrado, como
     *     numa finalização simultânea (409)
     */
    void registrarUso(Long cupomPromocionalId, Long clienteId, Long pedidoId);

    /** Apaga o uso registrado pelo pedido reprovado, devolvendo o promocional ao cliente (DEC-15.2). */
    void removerUso(Long pedidoId);
}
