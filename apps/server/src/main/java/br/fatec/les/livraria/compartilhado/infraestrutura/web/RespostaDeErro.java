package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.util.List;

import br.fatec.les.livraria.compartilhado.dominio.Violacao;

/** Corpo das respostas de erro da API. */
public record RespostaDeErro(String mensagem, List<Violacao> violacoes) {
}
