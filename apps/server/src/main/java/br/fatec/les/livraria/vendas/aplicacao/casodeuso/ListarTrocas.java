package br.fatec.les.livraria.vendas.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.vendas.aplicacao.consulta.TrocaResumo;
import br.fatec.les.livraria.vendas.aplicacao.porta.TrocasConsultas;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Consulta de trocas do administrador, filtrada pelo status de cada troca (RF-VEN-013, DEC-12). É daqui que o admin
 * encontra as trocas {@code EM TROCA}, mesmo quando o pedido tem outra troca mais recente.
 */
@Component
@RequiredArgsConstructor
public class ListarTrocas {

    private final TrocasConsultas trocas;

    /**
     * @param status status desejados, combinados com OU; nulo ou vazio não filtra
     * @param paginacao página, tamanho e ordenação já validados
     * @return a página de trocas
     */
    @Transactional(readOnly = true)
    public Pagina<TrocaResumo> executar(List<StatusTroca> status, Paginacao paginacao) {
        Objects.requireNonNull(paginacao, "paginacao");
        return trocas.listar(
                status == null ? List.of() : status.stream().filter(Objects::nonNull).distinct().toList(), paginacao);
    }
}
