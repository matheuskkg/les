---
tags: [code-review]
max_turns: 20
timeout_seconds: 600
allowed_tools: [Read, Glob, Grep, Skill, Write, Edit, Bash]
---

Repositório: /home/matheus/Projects/Fatec/LES. O diff abaixo é o escopo da revisão (não está aplicado no working tree; o repo serve para consultar as instruções e o código ao redor). Revise e me entregue só o relatório na conversa, sem editar arquivos.

Arquivo: apps/server/src/main/java/br/fatec/les/livraria/vendas/infraestrutura/web/dto/NovoPedidoDto.java (novo)

public record NovoPedidoDto(long clienteId, double valorTotal, List<String> isbns) {}

Arquivo: apps/server/src/main/java/br/fatec/les/livraria/clientes/aplicacao/casodeuso/CadastrarCliente.java (trecho alterado)

private final List<Regra<Cliente>> regras;

@Transactional
public ClienteId executar(NovoCliente comando) {
  for (Regra<Cliente> regra : regras) {
    regra.validar(comando, new Violacoes());
  }
  // monta o Cliente e salva
}

Arquivo: apps/server/src/main/java/br/fatec/les/livraria/vendas/infraestrutura/persistencia/PedidoJpa.java (trecho alterado)

@ManyToOne
private ClienteJpa cliente;
