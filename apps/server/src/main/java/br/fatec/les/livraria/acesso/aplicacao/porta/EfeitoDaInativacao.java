package br.fatec.les.livraria.acesso.aplicacao.porta;

/**
 * Efeito da inativação da conta em outro módulo (DEC-10), executado pelo {@code InativarConta} na mesma transação. O
 * {@code acesso} não conhece os módulos que dependem dele: cada um implementa esta porta, como o {@code vendas}, que
 * esvazia o carrinho e libera as reservas.
 */
public interface EfeitoDaInativacao {

    /**
     * @param usuarioId usuário cuja conta está sendo inativada
     */
    void aplicar(Long usuarioId);
}
