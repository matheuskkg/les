package br.fatec.les.livraria.vendas.aplicacao.porta;

import java.util.List;

import br.fatec.les.livraria.compartilhado.aplicacao.Pagina;
import br.fatec.les.livraria.compartilhado.aplicacao.Paginacao;
import br.fatec.les.livraria.vendas.aplicacao.consulta.TrocaResumo;
import br.fatec.les.livraria.vendas.dominio.troca.StatusTroca;

/** Porta de saída da consulta de trocas do admin (RF-VEN-013, DEC-07, DEC-12). */
public interface TrocasConsultas {

    /**
     * @param status status desejados, combinados com OU; vazio não filtra
     * @param paginacao página, tamanho e ordenação já validados
     * @return a página de trocas
     */
    Pagina<TrocaResumo> listar(List<StatusTroca> status, Paginacao paginacao);
}
