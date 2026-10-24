package br.fatec.les.livraria.vendas.aplicacao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.fatec.les.livraria.clientes.aplicacao.casodeuso.ResolverCartaoDaCompra;
import br.fatec.les.livraria.clientes.dominio.modelo.Cartao;
import br.fatec.les.livraria.compartilhado.dominio.Dinheiro;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.vendas.aplicacao.casodeuso.CartaoInformado;
import br.fatec.les.livraria.vendas.aplicacao.consulta.CartaoNoPagamento;
import br.fatec.les.livraria.vendas.dominio.pagamento.CartoesDaCompra;
import br.fatec.les.livraria.vendas.dominio.pagamento.DistribuicaoDoPagamento;
import br.fatec.les.livraria.vendas.dominio.pagamento.PagamentoEmCartao;
import br.fatec.les.livraria.vendas.dominio.regra.CartaoRepetido;
import br.fatec.les.livraria.vendas.dominio.regra.SomaDosCartoes;
import br.fatec.les.livraria.vendas.dominio.regra.ValorInformadoDoCartao;
import br.fatec.les.livraria.vendas.dominio.regra.ValorMinimoPorCartao;
import lombok.RequiredArgsConstructor;

/**
 * Confere os cartões que pagam o restante depois dos cupons (RF-VEN-006, RN-VEN-004, RN-VEN-005, DEC-10, DEC-11):
 * cada um cadastrado ou novo, com valor em centavos inteiros, sem repetir, de ao menos R$ 10,00 (salvo a exceção do
 * RN-VEN-005) e somando exatamente o restante.
 */
@Component
@RequiredArgsConstructor
public class ConferirCartoes {

    private final ResolverCartaoDaCompra resolverCartao;
    private final ValorInformadoDoCartao valorInformado; // RN-VEN-004
    private final CartaoRepetido cartaoRepetido; // RN-VEN-004
    private final SomaDosCartoes somaDosCartoes; // RN-VEN-004
    private final ValorMinimoPorCartao valorMinimo; // RN-VEN-004, RN-VEN-005

    /**
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio com as violações dos cartões
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeRecursoNaoEncontrado se um cartão não for do
     *         cliente
     */
    @Transactional(readOnly = true)
    public List<CartaoNoPagamento> conferir(DistribuicaoDoPagamento distribuicao, boolean comCupom,
            List<CartaoInformado> informados) {
        List<Cartao> cartoes = informados.stream().map(informado -> resolverCartao.executar(informado.escolha()))
                .toList();
        List<PagamentoEmCartao> pagamentos = new ArrayList<>();
        for (int i = 0; i < cartoes.size(); i++) {
            pagamentos.add(new PagamentoEmCartao(cartoes.get(i).numero(), informados.get(i).valor()));
        }

        CartoesDaCompra alvo = new CartoesDaCompra(distribuicao.restante(), comCupom, pagamentos);
        Violacoes violacoes = new Violacoes();
        valorInformado.validar(alvo, violacoes);
        violacoes.lancarSeHouver();
        Violacoes.verificar(alvo, cartaoRepetido, somaDosCartoes, valorMinimo);

        return IntStream.range(0, cartoes.size())
                .mapToObj(i -> new CartaoNoPagamento(cartoes.get(i), Dinheiro.de(informados.get(i).valor())))
                .toList();
    }
}
