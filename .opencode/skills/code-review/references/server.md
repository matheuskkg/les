# Roteiro de revisão: `apps/server`

Abra esta página quando o diff toca `apps/server`. Ela organiza a leitura; a regra vale o que estiver no documento
apontado, não o resumo daqui.

## Leitura obrigatória

1. `docs/arquitetura.md` — stack, módulos, camadas, convenção de um arquivo por regra, banco e seed.
2. `AGENTS.md` da raiz — convenções de código e escopo do MVP.
3. `docs/requisitos/<grupo>.md` do módulo tocado (tabela abaixo), em especial a matriz de rastreabilidade.
4. Índice `docs/decisoes/README.md` e os DECs aplicáveis ao assunto.

| Módulo tocado | Requisitos | Decisões que costumam aparecer |
|---|---|---|
| `clientes` | `docs/requisitos/clientes.md` | DEC-08 (sessão), DEC-10 (endereços/cartões), DEC-06 e DEC-18 (cartão) |
| `catalogo` | `docs/requisitos/livros.md` | DEC-14 (ids/seed), DEC-15.3 (preço vem do estoque) |
| `vendas` | `docs/requisitos/vendas.md` | DEC-05.1, DEC-05.2, DEC-05.3, DEC-11, DEC-12, DEC-13, DEC-15.1, DEC-15.2 |
| `estoque` | `docs/requisitos/estoque.md` | DEC-14, DEC-15.1, DEC-15.3, DEC-16 |
| `analise` | `docs/requisitos/analise.md` | DEC-15.4 (acesso e exportação) |
| `recomendacao` | `docs/requisitos/recomendacao.md` | DEC-03, DEC-04 |
| `acesso`, `compartilhado`, `notificacoes` | `docs/requisitos/geral.md` | DEC-08, DEC-13, DEC-16 |

## O que conferir

### Camadas e dependências

- A direção é `infraestrutura → aplicacao → dominio`. ArchUnit falha o build se `dominio` importar JPA,
  `aplicacao` ou `infraestrutura`, ou se `aplicacao` importar `infraestrutura`.
- Entidade JPA (`*Jpa`) fica em `infraestrutura/persistencia`, separada do modelo de domínio, com mapper MapStruct.
- Um módulo chama outro por caso de uso ou porta pública, nunca pela persistência alheia.
- Controllers e DTOs ficam em `infraestrutura/web`; validação de negócio não vai parar ali.

### Regras, cálculos e casos de uso

- Regra pura em `dominio/regra` implementa `Regra<T>`, acumula `Violacoes` em vez de lançar exceção e não consulta
  dados. Regra que precisa de porta vai em `aplicacao/regra`.
- Regras são agrupadas por operação, não por entidade: o caso de uso recebe no construtor exatamente as regras que
  aplica. `List<Regra<X>>` genérico está errado.
- Cálculo de negócio é classe com um único método público em `dominio`. Caso de uso é `executar(Comando)` com
  `@Transactional` e injeção por construtor.
- O ID do requisito aparece no Javadoc da classe e no nome do teste.
- Transição de status é um caso de uso por transição, e o modelo de domínio recusa transição inválida.

### Dados, dinheiro e API

- `Dinheiro` sobre `BigDecimal`, escala 2 e `HALF_UP`; arredondamento só no fim do cálculo (DEC-15.1). Nunca
  `double` nem `float`.
- Datas e formatos JSON seguem DEC-07: dinheiro como string decimal, datas ISO-8601, enums pelo nome Java, campos
  nulos omitidos.
- Tabelas e colunas em snake_case, português e singular; índices nas colunas filtradas e ordenadas (RNF-GER-001);
  referência a outro módulo só por id, sem `@ManyToOne` entre módulos nem FK (DEC-16).
- Seed em `resources/seed/NNN_*.sql`, SQL comum a H2 e PostgreSQL. IDs explícitos até 9999; sequences começam em
  10000 (DEC-14). Não há migrations.
- Auditoria com Hibernate Envers (`@Audited`, tabelas `_aud`) para log de transação (RNF-GER-002).

### Testes e contrato

- Testes espelham `main` e incluem `ArquiteturaTest`. Toda regra nova precisa de teste unitário isolado com o ID do
  requisito no nome.
- Testes de integração usam H2. O perfil `h2,dev` liga o fake da IA sem rede; `h2,e2e` é o do Playwright.
- Mudou dto/controller? O contrato em `apps/web/openapi.json` e os tipos em `apps/web/src/api/esquema.d.ts`
  precisam ser regenerados (ver `AGENTS.md` da raiz e DEC-07).

## Comandos úteis

```bash
cd apps/server
mvn test
mvn test -Dtest=ContratoOpenApiTest -Dlivraria.openapi.gravar=true
```

## Anti-padrões específicos do `server`

- Colocar `BigDecimal` ou `Dinheiro` direto no controller sem seguir o (de)serializador do DEC-07.
- Criar uma regra que lance exceção em vez de acumular `Violacoes`.
- Injeter uma lista genérica de regras em vez de declarar as regras da operação.
- Referenciar tabela de outro módulo com `@ManyToOne`.
- Implementar CRUD de livros ou entrada manual de estoque e chamar isso de melhoria: fora do MVP.
- Mudar regra de negócio para satisfazer E2E; o teste é que se adapta ao DRS.
