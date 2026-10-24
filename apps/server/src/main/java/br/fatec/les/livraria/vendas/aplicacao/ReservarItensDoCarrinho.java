package br.fatec.les.livraria.vendas.aplicacao;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.estoque.aplicacao.casodeuso.Reservar;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Reserva de novo todos os itens do carrinho até {@code expiraEm} (RN-VEN-001, RN-VEN-014, DEC-05.1). Cada item passa
 * pelo {@link Reservar}, que trava o saldo e confere o disponível: uma reserva já vencida só volta a valer se ainda
 * houver estoque, e nenhuma unidade liberada é bloqueada duas vezes.
 */
@Component
@RequiredArgsConstructor
public class ReservarItensDoCarrinho {

    private final Reservar reservar;

    /** @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com RN-VEN-001 se faltar estoque */
    public void executar(Carrinho carrinho, LocalDateTime expiraEm) {
        for (ItemCarrinho item : carrinho.getItens()) {
            reservar.executar(item.getLivroId(), item.getQuantidade(), OrigemReserva.CARRINHO, carrinho.getId(),
                    expiraEm);
        }
    }
}
