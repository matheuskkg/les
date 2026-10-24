package br.fatec.les.livraria.notificacoes.infraestrutura.web.dto;

/**
 * Contador de notificações não lidas do cabeçalho (DEC-13).
 *
 * @param quantidade notificações não lidas do usuário autenticado
 */
public record ContagemDeNaoLidasDto(long quantidade) {
}
