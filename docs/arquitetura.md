# Arquitetura do `server`

Back-end do e-commerce de livros. A aplicação `web` é tratada à parte e não está coberta aqui.

## Stack

- Java 21, Spring Boot 4.1.x, Maven.
- Spring Web, Spring Data JPA (Hibernate), Spring Security (JWT, papéis `CLIENTE` e `ADMIN`), Hibernate Envers.
- Bancos suportados: **PostgreSQL** e **H2**, escolhidos por profile. Sem ferramenta de migrations: o schema é gerado
  pelo ORM e os dados vêm de scripts de seed executados na inicialização.
- Lombok.
- MapStruct (mapeamento domínio ↔ persistência ↔ DTO).
- springdoc (especificação OpenAPI, fonte dos tipos do `web`; ver [DEC-07](decisoes/DEC-07-convencoes-da-api-rest.md)).
- Testes: JUnit 5, AssertJ, ArchUnit.

## Estilo: hexagonal por módulo

Monólito modular. Cada grupo do DRS é um módulo (pacote de topo), e cada módulo tem as próprias camadas hexagonais.

| Módulo | Grupos do DRS |
|---|---|
| `clientes` | `CLI` |
| `catalogo` | `LIV` (somente leitura no MVP) |
| `vendas` | `VEN` |
| `estoque` | `EST` |
| `analise` | `ANA` |
| `recomendacao` | `REC` |
| `acesso` | `GER`: usuários, login e sessão ([DEC-08](decisoes/DEC-08-autenticacao-e-sessao.md)) |
| `notificacoes` | notificações genéricas por usuário e envio por SSE ([DEC-13](decisoes/DEC-13-processamento-do-pedido-e-notificacoes.md)) |
| `compartilhado` | `GER` e infraestrutura transversal |

### Camadas e dependências

```
infraestrutura  ──▶  aplicacao  ──▶  dominio
```

- **`dominio`**: modelos, value objects, regras puras e cálculos. Pode usar Spring (`@Component`, injeção) e
  Lombok; não usa JPA.
- **`aplicacao`**: casos de uso, regras que precisam consultar dados e **portas** (interfaces) de saída:
  repositórios, operadora de cartão, frete, IA. O caso de uso é a própria porta de entrada (não há interface
  separada para ele). A transação é demarcada no caso de uso (`@Transactional`).
- **`infraestrutura`**: adapters. Os de entrada são controllers e DTOs, e os de saída são persistência JPA, serviços
  fake e clientes externos.

Hexagonal estrita: as entidades JPA (`*Jpa`) ficam em `infraestrutura/persistencia`, separadas dos modelos de
domínio, com mappers MapStruct. Um teste ArchUnit faz o build falhar se `dominio` importar JPA, `aplicacao` ou
`infraestrutura`, ou se `aplicacao` importar `infraestrutura`.

Módulos se comunicam pelos casos de uso ou portas públicas do outro módulo, nunca acessando a persistência dele.
Exemplo: `vendas` chama `estoque` para reservar e baixar itens. A chamada é direta, na transação de quem chama, sem
eventos de domínio ([DEC-13](decisoes/DEC-13-processamento-do-pedido-e-notificacoes.md)).

### Estrutura de pacotes

```
apps/server/
├── pom.xml
├── docker-compose.yml          Postgres de desenvolvimento
└── src/
    ├── main/
    │   ├── java/br/fatec/les/livraria/
    │   │   ├── LivrariaApplication.java
    │   │   ├── compartilhado/
    │   │   │   ├── dominio/            Dinheiro, Regra, Violacoes, Violacao, ExcecaoDeNegocio, Cpf, Email
    │   │   │   ├── aplicacao/porta/    UsuarioAtual, Relogio
    │   │   │   └── infraestrutura/     seed/, tempo/, auditoria/, seguranca/, web/, e2e/
    │   │   └── vendas/                 (mesma forma em todos os módulos)
    │   │       ├── dominio/
    │   │       │   ├── modelo/         Pedido, ItemPedido, StatusPedido, Carrinho, Checkout, Troca, Cupom ...
    │   │       │   └── regra/          ValorMinimoPorCartao, UmCupomPromocionalPorCompra ...
    │   │       ├── aplicacao/
    │   │       │   ├── casodeuso/      FinalizarCompra, DespacharPedido, AutorizarTroca ...
    │   │       │   ├── regra/          EstoqueDisponivelParaCarrinho ...
    │   │       │   └── porta/          PedidoRepositorio, OperadoraCartao, CalculadoraFrete
    │   │       └── infraestrutura/
    │   │           ├── web/            PedidoController, CarrinhoController, dto/
    │   │           ├── persistencia/   PedidoJpa, PedidoJpaRepository, PedidoRepositorioJpa, PedidoMapper
    │   │           └── externo/        OperadoraCartaoFake, CalculadoraFreteFake
    │   └── resources/
    │       ├── application.yml
    │       ├── application-h2.yml
    │       ├── application-postgres.yml
    │       └── seed/                   001_dominios.sql, 002_livros.sql, 003_estoque.sql ...
    └── test/java/br/fatec/les/livraria/  (espelha main; + ArquiteturaTest)
```

## Um arquivo por regra, validação e operação

| Tipo | Onde | Forma | Exemplo |
|---|---|---|---|
| Regra/validação pura | `dominio/regra` | implementa `Regra<T>`, não consulta dados | `SenhaForte` (RNF-CLI-001), `ValorMinimoPorCartao` (RN-VEN-004) |
| Regra que consulta dados | `aplicacao/regra` | implementa `Regra<T>`, recebe portas no construtor | `CpfUnico`, `BandeiraRegistrada` (RN-CLI-005), `EstoqueDisponivelParaCarrinho` (RN-VEN-001) |
| Cálculo de negócio | `dominio/...` | classe com um único método público | `CalcularPrecoVenda` (RF-EST-002, RN-EST-002), `DistribuirPagamento` (RN-VEN-005, RN-VEN-006) |
| Operação | `aplicacao/casodeuso` | uma classe com `executar(Comando)` | `CadastrarCliente`, `AlterarSenha`, `SolicitarTroca` |
| Transição de status | `aplicacao/casodeuso` | um caso de uso por transição | `DespacharPedido` (`APROVADA` → `EM TRANSPORTE`, RN-VEN-009) |

Contrato das regras:

```java
public interface Regra<T> {
    void validar(T alvo, Violacoes violacoes); // acumula violações, não lança
}
```

- `T` é o que a operação valida: um modelo de domínio ou um comando ou record de entrada, por exemplo
  `PagamentoProposto` com o total, os cartões e os cupons.
- Regras, cálculos e casos de uso são beans anotados com `@Component`. A injeção é preferencialmente por construtor
  (Lombok `@RequiredArgsConstructor`), o que permite instanciá-los com `new` nos testes unitários.
- **Regras são agrupadas por operação, não por entidade.** O caso de uso recebe pelo construtor exatamente as regras
  que aplica. Não se injeta `List<Regra<Cliente>>` genericamente, porque cadastro, alteração e inativação usam
  conjuntos diferentes.
- O caso de uso executa todas as regras e, se houver violações, lança `ExcecaoDeNegocio`. O `TratadorDeErros`
  responde HTTP 422 em `ProblemDetail`, com a lista completa (código do requisito + mensagem) na extensão `violacoes`
  ([DEC-07](decisoes/DEC-07-convencoes-da-api-rest.md)).
- O ID do requisito fica no Javadoc da classe e no nome do teste. A coluna "Onde no código" da matriz de
  rastreabilidade aponta para o arquivo.
- Os modelos de domínio protegem os próprios invariantes. Por exemplo, `Pedido` recusa uma transição de status
  inválida mesmo que um caso de uso tente fazê-la.

Esboço de caso de uso:

```java
@Component
@RequiredArgsConstructor
public class CadastrarCliente {
    private final DadosObrigatoriosCliente dadosObrigatorios; // RN-CLI-006
    private final SenhaForte senhaForte;                      // RNF-CLI-001
    private final ConfirmacaoDeSenha confirmacaoDeSenha;      // RNF-CLI-002
    private final CpfUnico cpfUnico;
    private final ClienteRepositorio clientes;
    private final CodificadorDeSenha senhas;

    @Transactional
    public ClienteId executar(NovoCliente comando) {
        // lança ExcecaoDeNegocio com todas as violações, se houver
        Violacoes.verificar(comando, dadosObrigatorios, senhaForte, confirmacaoDeSenha, cpfUnico);
        // monta o Cliente, codifica a senha, salva
    }
}
```

## Banco de dados e seed

### Profiles

| Profile | Datasource | `ddl-auto` |
|---|---|---|
| `h2` (padrão) | `jdbc:h2:mem:livraria;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE` | `create-drop` |
| `postgres` | PostgreSQL local (Docker Compose) | `update` |

Seleção via `spring.profiles.active`. O H2 roda em modo PostgreSQL para que os mesmos scripts de seed e as consultas
nativas funcionem nos dois bancos. Os scripts devem usar só SQL comum aos dois.

`ddl-auto=update` não remove nem altera colunas existentes. Se o modelo mudar de forma incompatível, recria-se o
banco Postgres de desenvolvimento (não há dados reais).

### Seed

- Scripts em `resources/seed/NNN_descricao.sql`, executados em ordem lexicográfica. O local é configurável em
  `livraria.seed.local`, e `livraria.seed.habilitado=false` desliga a execução.
- `ExecutorDeSeeds` (`compartilhado/infraestrutura/seed`) é um `ApplicationRunner`, portanto roda depois de o
  Hibernate criar o schema.
- Ele mantém a tabela `seed_execucao (arquivo, checksum, executado_em)`. Cada arquivo roda em uma transação e é
  registrado ao terminar. Um arquivo já registrado é pulado, e um checksum diferente gera um aviso no log.
- Vale para os dois bancos com o mesmo código. No H2 a tabela some com o banco a cada execução.
- O seed contém as tabelas de domínio (RNF-LIV-002), os livros, as entradas de estoque com o custo, os cupons
  promocionais, os clientes de teste e os pedidos históricos. Cada script entra no card que cria as entidades dele,
  e a faixa do nome (`0NN` a `4NN`) define a ordem ([DEC-14](decisoes/DEC-14-ids-nomes-e-seed.md)).
- O seed usa IDs explícitos de 1 a 9999. As sequences começam em 10000, então nunca colidem com ele.

### IDs, nomes e índices

Definidos no [DEC-14](decisoes/DEC-14-ids-nomes-e-seed.md):

- ID `Long` por `SEQUENCE` própria de cada tabela (`<tabela>_seq`, valor inicial 10000). O ID é o código exibido de
  cliente, livro e pedido.
- Tabelas e colunas em snake_case, em português e no singular, com `@Table(name = ...)` em cada `*Jpa`. Tabelas do
  Envers com sufixo `_aud`.
- Índices em `@Table(indexes = ...)` nas colunas filtradas ou ordenadas e nas chaves estrangeiras consultadas
  (RNF-GER-001).

## Decisões por requisito

- **Reserva × baixa de estoque.**
  - O bloqueio ao adicionar ao carrinho (RN-VEN-014) é uma `Reserva` com validade, e não altera o estoque.
  - Disponível = quantidade em estoque − reservas ativas.
  - Em "Ir para pagamento", as reservas passam do carrinho para o `Checkout`, que tem prazo próprio
    ([DEC-12](decisoes/DEC-12-pedido-checkout-e-trocas.md)).
  - A baixa (RF-EST-003) só ocorre quando o pedido é aprovado. Num pedido reprovado, as reservas continuam no checkout
    para outra tentativa e só são liberadas quando ele expira (RN-EST-006).
  - Para evitar venda além do estoque com acessos simultâneos, a linha de estoque usa lock pessimista ao reservar e
    ao baixar.
- **Expiração do carrinho e do checkout.**
  - A tarefa `ExpirarReservas` (`TarefaPorTempo`, a cada 30 s) avisa antes do vencimento e, vencido o prazo, remove
    as reservas e retira todos os itens do mesmo produto (RN-VEN-015).
  - Os itens removidos ficam registrados para exibição no carrinho (RNF-VEN-001).
  - Os prazos são parâmetros de configuração.
  - Aviso e expiração chegam ao `web` como notificação. O `expiraEm` devolvido serve só para exibição
    ([DEC-13](decisoes/DEC-13-processamento-do-pedido-e-notificacoes.md)).
- **Preço de venda.** É calculado como maior custo entre as entradas do livro × (1 + margem do grupo de precificação)
  (RF-EST-002, RN-EST-002). Não é gravado fixo no seed.
- **Pagamento.** As regras RN-VEN-003 a RN-VEN-006 são domínio puro e são validadas antes de chamar a
  `OperadoraCartao`. O adapter fake tem regras determinísticas (RN-VEN-007), e o resultado define `APROVADA` ou
  `REPROVADA` (RN-VEN-008).
- **Finalização.** `FinalizarCompra` grava o pedido `EM PROCESSAMENTO` numa transação, chama a operadora fora dela e
  grava a aprovação ou reprovação numa segunda transação
  ([DEC-13](decisoes/DEC-13-processamento-do-pedido-e-notificacoes.md)).
- **Frete.** A porta `CalculadoraFrete` tem um adapter fake determinístico, baseado em CEP e peso/dimensões
  (RF-VEN-004).
- **Log de transação (RNF-GER-002).** Hibernate Envers nas entidades `*Jpa` com `@Audited`, com uma entidade de
  revisão própria que guarda o usuário (porta `UsuarioAtual`) e a data/hora. Os dados alterados ficam nas tabelas
  `_aud`.
- **Notificações (RN-VEN-016, RN-VEN-014).**
  - Ficam persistidas por usuário no módulo genérico `notificacoes`, que os outros módulos chamam pelos casos de uso
    `Notificar` e `NotificarAdministradores`.
  - Chegam ao `web` por SSE (`SseEmitter`), enviadas depois do commit.
  - O `web` também pode listá-las e marcá-las como lidas
    ([DEC-13](decisoes/DEC-13-processamento-do-pedido-e-notificacoes.md)).
- **Dinheiro.** Value object `Dinheiro` sobre `BigDecimal`, com escala 2 e `HALF_EVEN`. Nunca `double`.
- **Senhas.** BCrypt, por meio da porta `CodificadorDeSenha` (RNF-CLI-003).
- **Análise (ANA).** Consulta nativa agregada por mês e categoria copiada no pedido, considerando só os status
  válidos e descontando as trocas concluídas (RN-ANA-004, [DEC-12](decisoes/DEC-12-pedido-checkout-e-trocas.md)).
  O preenchimento de meses sem venda com R$ 0,00 (RN-ANA-003) e a validação do período (RN-ANA-002) ficam em
  regras e no caso de uso. A exportação é em CSV ou XLSX.
- **Recomendação (REC).** Porta `AssistenteDeLivros` com adapter fake por padrão. O provedor de IA real será definido
  quando o módulo for implementado.

## Testes

| Nível | Escopo | Ferramentas |
|---|---|---|
| Unitário | cada regra e cálculo, sem Spring; ID do requisito no nome do teste | JUnit 5, AssertJ |
| Caso de uso | portas substituídas por fakes em memória | JUnit 5 |
| Integração | controllers + persistência, profile `h2` | `@SpringBootTest` |
| Arquitetura | direção das dependências entre camadas e módulos | ArchUnit |
