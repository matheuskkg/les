import type { Page } from '@playwright/test';
import { entrarComo, expect, test } from './fixtures';

/**
 * Roteiro de apresentação da análise de vendas (RF-ANA-001 a RF-ANA-004, RNF-ANA-001 a RNF-ANA-004).
 *
 * Os testes falam com a aplicação como o administrador ou o cliente falariam: entram pela tela de login, usam as abas
 * do admin, preenchem o período, marcam categorias e leem o gráfico e a tabela. O reset do banco e o relógio
 * controlado ficam no `fixtures.ts`; nenhum teste depende de detalhe interno da tela.
 *
 * O DRS manda somar pedidos APROVADA, EM TRANSPORTE e ENTREGUE (RN-ANA-004). O cenário 6 da atividade pedia que a
 * passagem para ENTREGUE alterasse o gráfico, o que não acontece nessa regra: o pedido já entrou antes. O teste 6
 * demonstra o comportamento do DRS, com a entrega acontecendo dentro do período consultado.
 */

const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };
const CLIENTE = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

const CATEGORIAS = ['Fantasia', 'Ficção Científica', 'História', 'Romance', 'Tecnologia'];

async function abrirAnalise(page: Page): Promise<void> {
  await entrarComo(page, ADMIN.login, ADMIN.senha);
  await page.getByRole('link', { name: 'Administração' }).click();
  await page.getByRole('link', { name: 'Análise de vendas' }).click();
  await expect(page.getByRole('heading', { name: 'Análise de vendas' })).toBeVisible();
  await expect(page.getByLabel('Romance', { exact: true })).toBeVisible();
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

test('1. administrador consulta uma categoria no período e vê o total do mês (RF-ANA-001, RN-ANA-001)', async ({
  page,
}) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-03', '2025-03');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);

  await expect(page.getByRole('heading', { name: 'Evolução de 03/2025 a 03/2025' })).toBeVisible();
  await expect(linhaDoMes(page, '03/2025').getByRole('cell')).toHaveText(['R$ 95,76']);
  await expect(page.getByRole('list', { name: 'Legenda' }).getByRole('listitem')).toHaveText(['Romance']);
});

test('2. administrador compara duas categorias no mesmo período (RF-ANA-001, RF-ANA-003)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-03', '2025-03');
  await escolherCategorias(page, ['História', 'Romance']);
  await atualizar(page);

  await expect(page.getByRole('heading', { name: 'Evolução de 03/2025 a 03/2025' })).toBeVisible();
  await expect(page.getByRole('list', { name: 'Legenda' }).getByRole('listitem')).toHaveText([
    'História',
    'Romance',
  ]);
  await expect(linhaDoMes(page, '03/2025').getByRole('cell')).toHaveText(['R$ 95,76', 'R$ 95,76']);
});

test('3. resultado aparece em gráfico de linhas e tabela, em reais e por mês (RNF-ANA-001, RNF-ANA-002)', async ({
  page,
}) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-03', '2025-04');
  await escolherCategorias(page, ['Fantasia', 'Romance']);
  await atualizar(page);

  await expect(page.getByRole('heading', { name: 'Evolução de 03/2025 a 04/2025' })).toBeVisible();
  await expect(page.getByRole('img', { name: /Gráfico de linhas/ })).toBeVisible();
  await expect(page.getByRole('columnheader', { name: 'Fantasia' })).toBeVisible();
  await expect(page.getByRole('columnheader', { name: 'Romance' })).toBeVisible();
  await expect(linhaDoMes(page, '03/2025').getByRole('cell')).toHaveText(['R$ 80,00', 'R$ 95,76']);
  await expect(linhaDoMes(page, '04/2025').getByRole('cell')).toHaveText(['R$ 557,04', 'R$ 168,99']);
});

test('4. alterar a data final do período traz um novo resultado ao gráfico (RF-ANA-002)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-03', '2025-03');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);
  await expect(page.getByRole('heading', { name: 'Evolução de 03/2025 a 03/2025' })).toBeVisible();
  await expect(linhaDoMes(page, '03/2025').getByRole('cell')).toHaveText(['R$ 95,76']);

  await page.getByLabel('Mês final').fill('2025-04');
  await atualizar(page);

  await expect(page.getByRole('heading', { name: 'Evolução de 03/2025 a 04/2025' })).toBeVisible();
  await expect(linhaDoMes(page, '04/2025').getByRole('cell')).toHaveText(['R$ 168,99']);
});

test('5. pedido reprovado não entra no valor da categoria (RN-ANA-004)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-03', '2025-03');
  // Em março, o único pedido de Tecnologia foi REPROVADA; Romance tem uma venda entregue.
  await escolherCategorias(page, ['Romance', 'Tecnologia']);
  await atualizar(page);

  await expect(linhaDoMes(page, '03/2025').getByRole('cell')).toHaveText(['R$ 95,76', 'R$ 0,00']);
});

test('6. compra que passa a ENTREGUE dentro do período continua contabilizada (RN-ANA-004)', async ({
  page,
  ajustarRelogio,
}) => {
  // A entrega é feita em 20/10/2026, dentro do mês consultado. O pedido 242 é o que está EM TRANSPORTE no seed.
  await ajustarRelogio('2026-10-20T12:00:00-03:00');
  await abrirAnalise(page);
  await informarPeriodo(page, '2026-10', '2026-10');
  await escolherCategorias(page, ['Fantasia', 'Ficção Científica', 'História']);
  await atualizar(page);
  await expect(page.getByRole('heading', { name: 'Evolução de 10/2026 a 10/2026' })).toBeVisible();

  const antes = await linhaDoMes(page, '10/2026').getByRole('cell').allTextContents();
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
  await expect(linhaDoPedido).toContainText('EM TRANSPORTE');
  await linhaDoPedido.getByRole('link', { name: '242', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Pedido 242' })).toBeVisible();
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByText('ENTREGUE', { exact: true })).toBeVisible();
  await expect(page.getByText('Entregue em', { exact: true })).toBeVisible();

  await page.getByRole('link', { name: 'Análise de vendas' }).click();
  await expect(page.getByRole('heading', { name: 'Análise de vendas' })).toBeVisible();
  await informarPeriodo(page, '2026-10', '2026-10');
  await escolherCategorias(page, ['Fantasia', 'Ficção Científica', 'História']);
  await atualizar(page);

  await expect(linhaDoMes(page, '10/2026').getByRole('cell')).toHaveText(antes);
});

test('7. cliente não abre a análise, que é exclusiva do administrador (RF-ANA-001)', async ({ page }) => {
  await entrarComo(page, CLIENTE.login, CLIENTE.senha);
  await page.goto('/admin/analise');

  await expect(page.getByRole('heading', { name: 'Acesso negado' })).toBeVisible();
  await expect(page.getByText('Esta área não está disponível para o perfil da sua conta.')).toBeVisible();
  await expect(page.getByRole('img', { name: /Gráfico de linhas/ })).toHaveCount(0);
});

test('8. consulta da análise responde em até 1 segundo (RNF-GER-001, RNF0011)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-02', '2025-02');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);
  await expect(page.getByRole('heading', { name: 'Evolução de 02/2025 a 02/2025' })).toBeVisible();

  await informarPeriodo(page, '2025-03', '2025-03');
  const inicio = Date.now();
  await Promise.all([
    page.waitForResponse(
      (resposta) =>
        resposta.url().includes('/api/admin/analise?') &&
        resposta.url().includes('inicio=2025-03') &&
        resposta.url().includes('fim=2025-03') &&
        resposta.status() === 200,
    ),
    atualizar(page),
  ]);
  const tempo = Date.now() - inicio;

  expect(tempo).toBeLessThan(1000);
  await expect(linhaDoMes(page, '03/2025').getByRole('cell')).toHaveText(['R$ 95,76']);
});
