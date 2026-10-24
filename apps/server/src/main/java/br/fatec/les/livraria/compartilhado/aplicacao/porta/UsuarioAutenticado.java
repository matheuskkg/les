package br.fatec.les.livraria.compartilhado.aplicacao.porta;

/**
 * Principal de uma requisição autenticada pela sessão (DEC-08). O tipo fica no pacote de portas para que a
 * infraestrutura compartilhada leia o usuário sem depender da infraestrutura do módulo {@code acesso}.
 */
public interface UsuarioAutenticado {

    Long usuarioId();

    String email();
}
