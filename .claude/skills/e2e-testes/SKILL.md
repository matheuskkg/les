---
name: e2e-testes
description: Escreve e revisa roteiros E2E de apresentação do projeto do ponto de vista do usuário. Use ao criar ou alterar specs em apps/web/e2e, ao montar roteiros de apresentação de qualquer módulo, ao investigar testes frágeis ou ao ver alguém testando detalhes internos da tela (botão desabilitado, classe CSS, chamada de API isolada). Também use quando o pedido mencionar Playwright, apresentação para o professor, roteiro de demonstração ou testes funcionais da Livraria.
---

# Testes E2E da Livraria

Neste projeto, E2E não é suíte de regressão: todo spec é um roteiro de apresentação para o professor. Os E2E existem
para provar que uma pessoa consegue realizar o fluxo na aplicação. O professor vê o Chrome, não o código. Um teste
que passa por causa de um `data-testid` escondido, de um botão desabilitado ou de uma chamada de API direta não
demonstra o sistema; ele demonstra a implementação. O objetivo aqui é fazer o teste contar a mesma
história que o usuário viveria, em qualquer módulo: vitrine, carrinho, checkout, pedidos, trocas, conta, análise,
assistente ou outro.

## Quando usar

- Ao criar ou mudar um roteiro de apresentação em `apps/web/e2e/`.
- Ao preparar ou revisar o roteiro de apresentação de um módulo.
- Quando um teste começa a falhar por mudança de layout, e a tentação é aumentar o seletor em vez de rever o fluxo.
- Quando alguém pede para testar "se o botão fica habilitado", "se a URL mudou" ou "se a API respondeu X".

## Princípios

1. **Comece pelo usuário.** O teste deve começar no login ou em uma tela que o usuário abriria, e terminar em algo
   que ele vê: uma mensagem, uma linha da tabela, um status, um valor em reais, um gráfico. Use as abas, os campos e
   os botões como o usuário usa.
2. **Não teste o que só o programador vê.** Classe CSS, `data-testid`, estado interno do componente, quantidade de
   requisições e parâmetro de URL são meios, não fins. Um `aria-label` ou papel acessível é aceitável porque também
   é o que uma pessoa com leitor de tela usa.
3. **Estado desabilitado não é cenário.** Verificar que "Finalizar compra" ficou desabilitado enquanto envia é
   detalhe de implementação. O cenário é o usuário preencher, clicar e receber o resultado certo. Se o teste precisa
   esperar, espere pelo resultado visível, não pelo atributo.
4. **O setup fica no `fixtures.ts`.** O reset do banco e o relógio controlado são apoio de teste, não parte do
   roteiro. O `beforeEach` automático já chama `POST /api/teste/reset`; use `ajustarRelogio` quando o cenário
   depender de uma data. Não chame a API de negócio direto para "preparar" o que o usuário deveria fazer.
5. **Um teste, um cenário demonstrável.** O nome deve dizer o que o usuário faz e o requisito envolvido, por exemplo
   "administrador compara duas categorias no mesmo período (RF-ANA-001)". Evite nomes como "testa o componente".
6. **Os dados esperados vêm do seed.** Confira o valor no SQL ou no comentário do gerador. Não copie o valor da
   resposta da API no momento em que o teste falha; isso esconde regressão. Se o valor for calculado, explique a
   conta no comentário.
7. **O requisito do projeto manda mais que o roteiro da atividade.** Se a atividade pedir um comportamento diferente
   do DRS ou de uma decisão registrada, não mude a regra de negócio para o teste passar. Exemplo: a análise conta
   APROVADA, EM TRANSPORTE e ENTREGUE (RN-ANA-004), mesmo que um roteiro peça só ENTREGUE. Registre a diferença e
   teste o comportamento real.
8. **Tempo é requisito, não desculpa.** Para o limite de 1 segundo, faça uma consulta antes para aquecer e meça a
   resposta da consulta seguinte. Um `expect(...).toBeVisible({ timeout: 1000 })` pode passar por sorte do polling;
   prefira medir o intervalo entre o clique e a resposta que a tela usa. O `TempoDeRespostaTest` cobre o back-end.
9. **Teste independente.** Cada teste começa do seed e não depende da ordem dos outros. Não reaproveite número de
   pedido, saldo de estoque ou sessão de um teste anterior sem criar o dado pelo fluxo.
10. **Acessibilidade ajuda o teste e o usuário.** Prefira `getByRole`, `getByLabel` e texto visível. Se não houver
    rótulo, o primeiro conserto é na tela, não no seletor.

## Estrutura de um spec

```ts
import type { Page } from '@playwright/test';
import { entrarComo, expect, test } from './fixtures';

async function abrirAnalise(page: Page): Promise<void> {
  await entrarComo(page, 'admin@livraria.test', 'Admin@123');
  await page.getByRole('link', { name: 'Administração' }).click();
  await page.getByRole('link', { name: 'Análise de vendas' }).click();
  await expect(page.getByRole('heading', { name: 'Análise de vendas' })).toBeVisible();
}

test('administrador consulta uma categoria no período (RF-ANA-001)', async ({ page }) => {
  await abrirAnalise(page);
  await page.getByLabel('Mês inicial').fill('2025-03');
  await page.getByLabel('Mês final').fill('2025-03');
  await page.getByLabel('Romance', { exact: true }).check();
  await page.getByRole('button', { name: 'Atualizar' }).click();

  await expect(page.getByRole('row', { name: /^03\/2025/ }).getByRole('cell')).toHaveText(['R$ 95,76']);
});
```

Esse exemplo é da análise. Para outro fluxo, troque os passos de navegação e as asserções pelo que o usuário vê no
módulo, mas mantenha a estrutura: login, ação, resultado visível.

## Roteiros de apresentação

Todo E2E do projeto é um roteiro de apresentação. O padrão de nomes é `apresentacao<Fluxo>.spec.ts` (por exemplo,
`apresentacaoAnalise.spec.ts`) e `fixtures.ts` para o apoio compartilhado.

- `npm run e2e` roda todos os roteiros `apresentacao*.spec.ts`.
- `npm run e2e -- e2e/apresentacaoAnalise.spec.ts` roda só um fluxo.
- O `playwright.config.ts` executa só `apresentacao*.spec.ts`, abre o Chrome visível e espera meio segundo entre as
  ações; no CI roda headless e sem atraso.
- O pacote portátil copia a pasta `e2e` inteira, mas o `playwright.apresentacao.config.ts` executa só os arquivos
  `apresentacao*.spec.ts` contra o jar na porta 8080.
- O roteiro deve importar só `@playwright/test`, `./fixtures` e helpers dentro de `e2e/`; importar código da
  aplicação quebra o pacote portátil, que não leva o `src` do web.
- Não crie um script por fluxo no `package.json`; use `npm run e2e -- e2e/apresentacao<Fluxo>.spec.ts`.

Ao criar um roteiro novo, reaproveite o `fixtures.ts`, dê nomes numerados aos cenários e cite o requisito no título.
Os testes devem ser legíveis no relatório, porque é isso que o professor acompanha.

## Como rodar

```bash
cd apps/web
npm run typecheck
npm run e2e
npm run e2e -- e2e/apresentacaoAnalise.spec.ts
npm run e2e -- e2e/apresentacaoAnalise.spec.ts -g "6\."
```

Contra o jar empacotado, com o server em `h2,e2e`:

```bash
npm run e2e -- -c playwright.apresentacao.config.ts
```

O `playwright.config.ts` sobe server e Vite para o desenvolvimento. O `playwright.apresentacao.config.ts` não sobe
nada e espera a aplicação em `http://localhost:8080`.

## Antes de dar como pronto

- [ ] O teste começa em uma tela ou ação que o usuário faria.
- [ ] As asserções são sobre texto, valores, status ou elementos que o usuário vê.
- [ ] Não há asserção sobre `disabled`, classe CSS, `data-testid` ou chamada de API isolada.
- [ ] O nome do teste cita o requisito e o fluxo.
- [ ] Os valores esperados vêm do seed e estão explicados.
- [ ] O teste passa sozinho e passa na suíte inteira.
- [ ] `npm run typecheck` passa.
- [ ] A regra de negócio não foi alterada para o teste passar.

## Anti-padrões que já apareceram

- Esperar `getByRole('button', { name: 'Finalizar compra' }).toBeDisabled()`: o usuário vê o botão, clica e recebe
  uma mensagem; teste isso.
- Conferir que a URL ganhou `?categoria=2` ou `?status=ENTREGUE`: a URL é detalhe de navegação. Confira a legenda, a
  lista ou os valores que a tela mostra.
- Chamar `/api/admin/analise` no teste para comparar com a tela: o teste passaria mesmo se a tela não renderizasse.
- Criar pedido por SQL ou por API para depois "testar" a tela: use o fluxo de compra, que é o que o professor vai ver.
- Ajustar a regra de status para ENTREGUE só para o cenário 6 da análise passar: o DRS manda contar APROVADA e EM
  TRANSPORTE também. A divergência se explica, não se implementa escondido.
- Criar um `apresentacao<Fluxo>.spec.ts` que só verifica se a página abriu: o roteiro precisa percorrer a ação
  principal do módulo, não só a navegação.
- Criar um spec de regressão fora do padrão `apresentacao<Fluxo>.spec.ts`: o `playwright.config.ts` só executa os
  roteiros de apresentação.
