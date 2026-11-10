import type { Page } from '@playwright/test';
import { entrarNaApi, expect, rolarAte, test } from './fixtures';

/**
 * Roteiro de apresentação da análise de vendas (RF-ANA-001 a RF-ANA-004, RNF-ANA-001 a RNF-ANA-004).
 *
 * Os testes falam com a aplicação como o administrador ou o cliente falariam: começam já autenticados pela API,
 * porque o login não é o cenário, e vão direto à tela que interessa; no cenário de entrega, passam também pela tela de
 * pedidos. O reset do banco e o relógio controlado ficam no `fixtures.ts`; nenhum teste depende de detalhe interno da
 * tela.
 *
 * O DRS manda somar pedidos APROVADA, EM TRANSPORTE e ENTREGUE (RN-ANA-004). O cenário 6 da atividade pedia que a
 * passagem para ENTREGUE alterasse o gráfico, o que não acontece nessa regra: o pedido já entrou antes. O teste 6
 * demonstra o comportamento do DRS, com a entrega acontecendo dentro do período consultado.
 */

const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };
const CLIENTE = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

const CATEGORIAS = ['Fantasia', 'Ficção Científica', 'História', 'Romance', 'Tecnologia'];

async function esperarAnalisePronta(page: Page): Promise<void> {
  await expect(page.getByRole('heading', { name: 'Análise de vendas' })).toBeVisible();
  // Só depois do catálogo a tela estabiliza os filtros; preencher antes disso faz o formulário voltar ao padrão.
  await expect(page.getByLabel('Romance', { exact: true })).toBeVisible();
}

async function abrirAnalise(page: Page): Promise<void> {
  await entrarNaApi(page, ADMIN.login, ADMIN.senha);
  await page.goto('/admin/analise');
  await esperarAnalisePronta(page);
}

async function informarPeriodo(page: Page, inicio: string, fim: string): Promise<void> {
  await page.getByLabel('Mês inicial').fill(inicio);
  await page.getByLabel('Mês final').fill(fim);
}

/** Deixa marcadas só as categorias pedidas, como quem desmarca as caixas na tela. */
async function escolherCategorias(page: Page, desejadas: readonly string[]): Promise<void> {
  for (const categoria of CATEGORIAS) {
    const caixa = page.getByLabel(categoria, { exact: true });
    if (desejadas.includes(categoria)) {
      await caixa.check();
    } else {
      await caixa.uncheck();
    }
  }
}

async function atualizar(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Atualizar' }).click();
}

function linhaDoMes(page: Page, mes: string) {
  return page.getByRole('row', { name: new RegExp(`^${mes.replace('/', '\\/')}`) });
}

/** Rola até o título do resultado e confere o período, para o gráfico aparecer na tela. */
async function conferirResultado(page: Page, inicio: string, fim: string): Promise<void> {
  const titulo = page.getByRole('heading', { name: `Evolução de ${inicio} a ${fim}` });
  await rolarAte(titulo);
  await expect(titulo).toBeVisible();
}

/** Rola até a linha do mês e confere os valores, para a tabela aparecer na tela. */
async function conferirLinha(page: Page, mes: string, valores: string[]): Promise<void> {
  const linha = linhaDoMes(page, mes);
  await rolarAte(linha);
  await expect(linha.getByRole('cell')).toHaveText(valores);
}

/** Rola até a legenda do gráfico e confere as categorias. */
async function conferirLegenda(page: Page, categorias: string[]): Promise<void> {
  const legenda = page.getByRole('list', { name: 'Legenda' });
  await rolarAte(legenda);
  await expect(legenda.getByRole('listitem')).toHaveText(categorias);
}

test('1. administrador consulta uma categoria no período e vê os totais dos meses (RF-ANA-001, RN-ANA-001)', async ({
  page,
}) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-01', '2025-12');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);

  await conferirResultado(page, '01/2025', '12/2025');
  await conferirLegenda(page, ['Romance']);
  await conferirLinha(page, '03/2025', ['R$ 95,76']);
  await conferirLinha(page, '04/2025', ['R$ 168,99']);
  await conferirLinha(page, '12/2025', ['R$ 209,00']);
});

test('2. administrador compara duas categorias no mesmo período (RF-ANA-001, RF-ANA-003)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-01', '2025-12');
  await escolherCategorias(page, ['História', 'Romance']);
  await atualizar(page);

  await conferirResultado(page, '01/2025', '12/2025');
  await conferirLegenda(page, ['História', 'Romance']);
  await conferirLinha(page, '03/2025', ['R$ 95,76', 'R$ 95,76']);
  await conferirLinha(page, '04/2025', ['R$ 875,73', 'R$ 168,99']);
  await conferirLinha(page, '12/2025', ['R$ 429,40', 'R$ 209,00']);
});

test('3. resultado aparece em gráfico de linhas e tabela, em reais e por mês (RNF-ANA-001, RNF-ANA-002)', async ({
  page,
}) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2024-11', '2026-10');
  await escolherCategorias(page, ['Fantasia', 'Romance']);
  await atualizar(page);

  await conferirResultado(page, '11/2024', '10/2026');

  const grafico = page.getByRole('img', { name: /Gráfico de linhas/ });
  await rolarAte(grafico);
  await expect(grafico).toBeVisible();

  const tabela = page.getByRole('table', { name: 'Vendas por mês e categoria' });
  await rolarAte(tabela);
  await expect(page.getByRole('columnheader', { name: 'Fantasia' })).toBeVisible();
  await expect(page.getByRole('columnheader', { name: 'Romance' })).toBeVisible();
  await conferirLinha(page, '11/2024', ['R$ 145,80', 'R$ 132,05']);
  await conferirLinha(page, '03/2025', ['R$ 80,00', 'R$ 95,76']);
  await conferirLinha(page, '04/2025', ['R$ 557,04', 'R$ 168,99']);
  await conferirLinha(page, '10/2026', ['R$ 198,54', 'R$ 84,17']);
});

test('4. alterar a data final do período traz um novo resultado ao gráfico (RF-ANA-002)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-01', '2025-06');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);
  await conferirResultado(page, '01/2025', '06/2025');
  await conferirLinha(page, '01/2025', ['R$ 172,06']);
  await conferirLinha(page, '06/2025', ['R$ 327,65']);

  await page.getByLabel('Mês final').fill('2025-12');
  await atualizar(page);

  await conferirResultado(page, '01/2025', '12/2025');
  await conferirLinha(page, '12/2025', ['R$ 209,00']);
});

test('5. pedido reprovado não entra no valor da categoria (RN-ANA-004)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-01', '2025-12');
  // Em março, o único pedido de Tecnologia foi REPROVADA; em abril, há vendas aprovadas.
  await escolherCategorias(page, ['Romance', 'Tecnologia']);
  await atualizar(page);

  await conferirResultado(page, '01/2025', '12/2025');
  await conferirLinha(page, '03/2025', ['R$ 95,76', 'R$ 0,00']);
  await conferirLinha(page, '04/2025', ['R$ 168,99', 'R$ 671,10']);
});

test('6. compra que passa a ENTREGUE dentro do período continua contabilizada (RN-ANA-004)', async ({
  page,
  ajustarRelogio,
}) => {
  // A entrega é feita em 20/10/2026, dentro do período consultado. O pedido 242 é o que está EM TRANSPORTE no seed.
  await ajustarRelogio('2026-10-20T12:00:00-03:00');
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-11', '2026-10');
  await escolherCategorias(page, ['Fantasia', 'Ficção Científica', 'História']);
  await atualizar(page);
  await conferirResultado(page, '11/2025', '10/2026');

  const linhaDeOutubro = linhaDoMes(page, '10/2026');
  await rolarAte(linhaDeOutubro);
  const antes = await linhaDeOutubro.getByRole('cell').allTextContents();
  expect(antes).toEqual(['R$ 198,54', 'R$ 235,04', 'R$ 353,28']);

  // O administrador encontra o pedido 242, confirma a entrega e volta à análise.
  await page.getByRole('link', { name: 'Pedidos' }).click();
  await expect(page.getByRole('heading', { name: 'Pedidos' })).toBeVisible();
  await page.getByText('Mais filtros').click();
  await page.getByLabel('Número do pedido').fill('242');
  await page.getByRole('button', { name: 'Filtrar' }).click();
  const linhaDoPedido = page
    .getByRole('row')
    .filter({ has: page.getByRole('link', { name: '242', exact: true }) });
  await rolarAte(linhaDoPedido);
  await expect(linhaDoPedido).toContainText('EM TRANSPORTE');
  await linhaDoPedido.getByRole('link', { name: '242', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Pedido 242' })).toBeVisible();
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  const statusEntregue = page.getByText('ENTREGUE', { exact: true });
  await rolarAte(statusEntregue);
  await expect(statusEntregue).toBeVisible();
  const entregueEm = page.getByText('Entregue em', { exact: true });
  await rolarAte(entregueEm);
  await expect(entregueEm).toBeVisible();

  await page.getByRole('link', { name: 'Análise de vendas' }).click();
  await esperarAnalisePronta(page);
  await informarPeriodo(page, '2025-11', '2026-10');
  await escolherCategorias(page, ['Fantasia', 'Ficção Científica', 'História']);
  await atualizar(page);

  await conferirResultado(page, '11/2025', '10/2026');
  await conferirLinha(page, '10/2026', antes);
});

test('7. cliente não abre a análise, que é exclusiva do administrador (RF-ANA-001)', async ({ page }) => {
  await entrarNaApi(page, CLIENTE.login, CLIENTE.senha);
  await page.goto('/admin/analise');

  await expect(page.getByRole('heading', { name: 'Acesso negado' })).toBeVisible();
  await expect(page.getByText('Esta área não está disponível para o perfil da sua conta.')).toBeVisible();
  await expect(page.getByRole('img', { name: /Gráfico de linhas/ })).toHaveCount(0);
});

test('8. consulta da análise responde em até 1 segundo (RNF-GER-001)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-01', '2025-06');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);
  await conferirResultado(page, '01/2025', '06/2025');

  await informarPeriodo(page, '2025-01', '2025-12');
  const inicio = Date.now();
  await Promise.all([
    page.waitForResponse(
      (resposta) =>
        resposta.url().includes('/api/admin/analise?') &&
        resposta.url().includes('inicio=2025-01') &&
        resposta.url().includes('fim=2025-12') &&
        resposta.status() === 200,
    ),
    atualizar(page),
  ]);
  const tempo = Date.now() - inicio;

  expect(tempo).toBeLessThan(1000);
  await conferirResultado(page, '01/2025', '12/2025');
  await conferirLinha(page, '03/2025', ['R$ 95,76']);
});
