package br.fatec.les.livraria.vendas.aplicacao;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.estoque.aplicacao.casodeuso.Reservar;
import br.fatec.les.livraria.estoque.dominio.modelo.OrigemReserva;
import br.fatec.les.livraria.vendas.dominio.modelo.Carrinho;
import br.fatec.les.livraria.vendas.dominio.modelo.ItemCarrinho;
import lombok.RequiredArgsConstructor;

/**
 * Reserva de novo todos os itens do carrinho até {@code expiraEm} (RN-VEN-001, RN-VEN-014, DEC-05.1). Cada item passa
 * pelo {@link Reservar}, que trava o saldo e confere o disponível: uma reserva já vencida só volta a valer se ainda
 * houver estoque, e nenhuma unidade liberada é bloqueada duas vezes. Os livros são travados em ordem de id, para que
 * dois carrinhos com os mesmos livros não se bloqueiem mutuamente.
 */
@Component
@RequiredArgsConstructor
public class ReservarItensDoCarrinho {

    private final Reservar reservar;

    /** @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com RN-VEN-001 se faltar estoque */
    public void executar(Carrinho carrinho, LocalDateTime expiraEm) {
        List<ItemCarrinho> itens = carrinho.getItens().stream()
                .sorted(Comparator.comparing(ItemCarrinho::getLivroId))
                .toList();
        for (ItemCarrinho item : itens) {
            reservar.executar(item.getLivroId(), item.getQuantidade(), OrigemReserva.CARRINHO, carrinho.getId(),
                    expiraEm);
        }
    }
}
