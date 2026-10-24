package br.fatec.les.livraria.vendas.aplicacao;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.compartilhado.aplicacao.porta.Relogio;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio;
import br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsDeTroca;
import br.fatec.les.livraria.vendas.aplicacao.porta.CuponsPromocionais;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomPromocional;
import br.fatec.les.livraria.vendas.dominio.pagamento.CupomTroca;
import br.fatec.les.livraria.vendas.dominio.pagamento.CuponsDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.CuponsDeTrocaNaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuicaoDoPagamento;
import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuirPagamento;
import br.fatec.les.livraria.vendas.dominio.pagamento.PromocionalNaCompra;
import br.fatec.les.livraria.vendas.dominio.regra.CupomPromocionalValido;
import br.fatec.les.livraria.vendas.dominio.regra.CuponsDeTrocaDisponiveis;
import br.fatec.les.livraria.vendas.dominio.regra.CuponsDeTrocaSemRepeticao;
import br.fatec.les.livraria.vendas.dominio.regra.CuponsDesnecessarios;
import br.fatec.les.livraria.vendas.dominio.regra.UmCupomPromocionalPorCompra;
import lombok.RequiredArgsConstructor;

/**
 * Confere os cupons de uma compra e distribui o total entre eles (RF-VEN-007, RN-VEN-003, RN-VEN-006, RN-VEN-007,
 * DEC-11, DEC-15.2). Promocional com código desconhecido é violação (422); cupom de troca de outro cliente não é
 * encontrado (404), como o endereço e o cartão.
 */
@Component
@RequiredArgsConstructor
public class ConferirCupons {

    private final UmCupomPromocionalPorCompra umPromocional; // RN-VEN-003
    private final CuponsDeTrocaSemRepeticao semRepeticao; // RN-VEN-007
    private final CupomPromocionalValido promocionalValido; // RN-VEN-007
    private final CuponsDeTrocaDisponiveis trocasDisponiveis; // RN-VEN-007
    private final CuponsDesnecessarios desnecessarios; // RN-VEN-006
    private final CuponsPromocionais promocionais;
    private final CuponsDeTroca cuponsDeTroca;
    private final Relogio relogio;

    /**
     * @param cuponsDeTroca ids na ordem em que cobrem o total
     * @throws ExcecaoDeNegocio com as violações dos cupons
     * @throws ExcecaoDeRecursoNaoEncontrado se um cupom de troca não for do cliente
     */
    @Transactional(readOnly = true)
    public DistribuicaoDoPagamento conferir(Long clienteId, Dinheiro subtotal, Dinheiro frete,
            List<String> codigosPromocionais, List<Long> cuponsDeTroca) {
        Violacoes violacoes = new Violacoes();
        umPromocional.validar(codigosPromocionais, violacoes);
        semRepeticao.validar(cuponsDeTroca, violacoes);
        violacoes.lancarSeHouver();

        CupomPromocional promocional = codigosPromocionais.isEmpty() ? null : promocional(codigosPromocionais.get(0));
        List<CupomTroca> trocas = trocasDoCliente(clienteId, cuponsDeTroca);

        if (promocional != null) {
            promocionalValido.validar(new PromocionalNaCompra(promocional, subtotal, relogio.hoje(),
                    promocionais.usadoPeloCliente(promocional.id(), clienteId)), violacoes);
        }
        trocasDisponiveis.validar(new CuponsDeTrocaNaCompra(trocas, relogio.hoje()), violacoes);
        violacoes.lancarSeHouver();

        CuponsDaCompra compra = new CuponsDaCompra(subtotal, frete, promocional, trocas);
        Violacoes.verificar(compra, desnecessarios);
        return DistribuirPagamento.de(compra);
    }

    private CupomPromocional promocional(String codigo) {
        String normalizado = codigo == null ? "" : codigo.strip().toUpperCase(Locale.ROOT);
        return promocionais.buscarPorCodigo(normalizado).orElseThrow(
                () -> new ExcecaoDeNegocio("RN-VEN-007", "Cupom " + normalizado + " não encontrado."));
    }

    /** Na ordem informada, que é a ordem em que os cupons cobrem o total (DEC-11). */
    private List<CupomTroca> trocasDoCliente(Long clienteId, List<Long> ids) {
        List<CupomTroca> encontrados = cuponsDeTroca.buscarDoCliente(clienteId, ids);
        return ids.stream()
                .map(id -> encontrados.stream().filter(cupom -> cupom.id().equals(id)).findFirst()
                        .orElseThrow(() -> new ExcecaoDeRecursoNaoEncontrado("Cupom de troca não encontrado.")))
                .toList();
    }
}
