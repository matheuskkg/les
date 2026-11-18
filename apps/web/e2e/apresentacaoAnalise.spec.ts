import type { Page } from '@playwright/test';
import { readFile } from 'node:fs/promises';
import { entrarNaApi, expect, rolarAte, test } from './fixtures';

/**
 * Roteiro de apresentação da análise de vendas (RF-ANA-001 a RF-ANA-004, RNF-ANA-001 a RNF-ANA-004).
 * Atividade ANA-01: RF0055 ↔ RF-ANA-001/002/003, RNF0043 ↔ RNF-ANA-001 e RNF0011 ↔ RNF-GER-001.
 *
 * Os testes falam com a aplicação como o administrador ou o cliente falariam: começam já autenticados pela API,
 * porque o login não é o cenário, e vão direto à tela que interessa; no cenário de entrega, passam também pela tela de
 * pedidos. O reset do banco e o relógio controlado ficam no `fixtures.ts`; nenhum teste depende de detalhe interno da
 * tela.
 *
 * O DRS manda somar pedidos APROVADA, EM TRANSPORTE e ENTREGUE (RN-ANA-004); o critério (d) da atividade pede
 * somente ENTREGUE. O projeto segue o DRS: o teste 5 deixa REPROVADA de fora e o teste 6 mostra que um pedido
 * EM TRANSPORTE já conta e continua contando depois da entrega, dentro do período consultado. O teste 9 valida o
 * período invertido (RF-ANA-002), o 10 exporta o CSV (RF-ANA-004), o 11 mostra o filtro por dias dentro de um mês e o
 * 3 anexa uma captura do gráfico ao relatório. Desde o DEC-19 as datas são exatas, mas o eixo X continua mensal.
 */

const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };
const CLIENTE = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

const CATEGORIAS = ['Fantasia', 'Ficção Científica', 'História', 'Romance', 'Tecnologia'];

/** Cores fixas das séries do teste 3, definidas pela paleta de `filtrosDaAnalise.ts`. */
const CORES_DAS_SERIES = {
  Fantasia: '#1baf7a',
  Romance: '#2a78d6',
} as const;

/**
 * Confere que o canvas do gráfico tem pixels das cores das séries. É a prova de que as linhas foram desenhadas, e
 * não apenas de que o canvas existe; a captura de tela do teste 3 mostra o resultado.
 */
async function conferirCoresNoGrafico(page: Page, cores: readonly string[]): Promise<void> {
  const canvas = page.locator('figure.grafico canvas').first();
  await expect(canvas).toBeVisible();
  await expect
    .poll(
      () =>
        canvas.evaluate((elemento, alvos) => {
          const canvas = elemento as HTMLCanvasElement;
          const contexto = canvas.getContext('2d');
          if (!contexto) {
            return [];
          }
          const { data } = contexto.getImageData(0, 0, canvas.width, canvas.height);
          const canais = (cor: string) =>
            [1, 3, 5].map((inicio) => Number.parseInt(cor.slice(inicio, inicio + 2), 16));
          return alvos.filter((cor) => {
            const [r = 0, g = 0, b = 0] = canais(cor);
            for (let i = 0; i < data.length; i += 4) {
              if (
                Math.abs((data[i] ?? 0) - r) <= 2 &&
                Math.abs((data[i + 1] ?? 0) - g) <= 2 &&
                Math.abs((data[i + 2] ?? 0) - b) <= 2 &&
                (data[i + 3] ?? 0) > 0
              ) {
                return true;
              }
            }
            return false;
          });
        }, [...cores]),
      { message: 'As cores das séries devem aparecer no canvas do gráfico.' },
    )
    .toEqual([...cores]);
}

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
  await page.getByLabel('Data inicial').fill(inicio);
  await page.getByLabel('Data final').fill(fim);
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
  await informarPeriodo(page, '2025-01-01', '2025-12-31');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);

  await conferirResultado(page, '01/01/2025', '31/12/2025');
  await conferirLegenda(page, ['Romance']);
  // O pedido 44 tem R$ 95,76 de itens, R$ 18,00 de frete e R$ 10,00 de desconto; o gráfico mostra os itens, sem
  // frete e sem abater o desconto.
  await conferirLinha(page, '03/2025', ['R$ 95,76']);
  await conferirLinha(page, '04/2025', ['R$ 168,99']);
  await conferirLinha(page, '12/2025', ['R$ 209,00']);
});

test('2. administrador compara duas categorias no mesmo período (RF-ANA-001, RF-ANA-003)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-01-01', '2025-12-31');
  await escolherCategorias(page, ['História', 'Romance']);
  await atualizar(page);

  await conferirResultado(page, '01/01/2025', '31/12/2025');
  await conferirLegenda(page, ['História', 'Romance']);
  await conferirLinha(page, '03/2025', ['R$ 95,76', 'R$ 95,76']);
  await conferirLinha(page, '04/2025', ['R$ 875,73', 'R$ 168,99']);
  await conferirLinha(page, '12/2025', ['R$ 429,40', 'R$ 209,00']);
});

test('3. resultado aparece em gráfico de linhas e tabela, em reais e por mês (RNF-ANA-001, RNF-ANA-002)', async ({
  page,
}) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2024-11-01', '2026-10-31');
  await escolherCategorias(page, ['Fantasia', 'Romance']);
  await atualizar(page);

  await conferirResultado(page, '01/11/2024', '31/10/2026');

  const grafico = page.getByRole('img', { name: /Gráfico de linhas/ });
  await rolarAte(grafico);
  await expect(grafico).toBeVisible();
  await conferirCoresNoGrafico(page, [CORES_DAS_SERIES.Fantasia, CORES_DAS_SERIES.Romance]);

  // Anexa o gráfico com a legenda ao relatório: a imagem é a prova visual das duas linhas, além dos valores da tabela.
  const figura = page.locator('figure.grafico');
  await expect(figura).toBeVisible();
  await test.info().attach('grafico-de-linhas', {
    body: await figura.screenshot(),
    contentType: 'image/png',
  });

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
  await informarPeriodo(page, '2025-01-01', '2025-06-30');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);
  await conferirResultado(page, '01/01/2025', '30/06/2025');
  await conferirLinha(page, '01/2025', ['R$ 172,06']);
  await conferirLinha(page, '06/2025', ['R$ 327,65']);

  await page.getByLabel('Data final').fill('2025-12-31');
  await atualizar(page);

  await conferirResultado(page, '01/01/2025', '31/12/2025');
  await conferirLinha(page, '12/2025', ['R$ 209,00']);
});

test('5. pedido reprovado não entra no valor da categoria (RN-ANA-004)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-01-01', '2025-12-31');
  // O DRS conta APROVADA, EM TRANSPORTE e ENTREGUE; REPROVADA fica fora. O teste de controller de 10/2026 fixa os
  // três status que contam. Em março, o único pedido de Tecnologia foi REPROVADA; em abril, há vendas entregues.
  await escolherCategorias(page, ['Romance', 'Tecnologia']);
  await atualizar(page);

  await conferirResultado(page, '01/01/2025', '31/12/2025');
  await conferirLinha(page, '03/2025', ['R$ 95,76', 'R$ 0,00']);
  await conferirLinha(page, '04/2025', ['R$ 168,99', 'R$ 671,10']);
});

test('6. compra que passa a ENTREGUE dentro do período continua contabilizada (RN-ANA-004)', async ({
  page,
  ajustarRelogio,
}) => {
  // A entrega é feita em 20/10/2026, dentro do período consultado. O pedido 242 é o que está EM TRANSPORTE no seed.
  // O DRS já contava esse pedido; por isso a entrega não muda os valores. Ele contribui com R$ 47,88 em Fantasia,
  // R$ 47,88 em Ficção Científica e R$ 56,28 em História. O cenário 6 da atividade esperava uma mudança, mas o
  // projeto segue o RN-ANA-004.
  await ajustarRelogio('2026-10-20T12:00:00-03:00');
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-11-01', '2026-10-31');
  await escolherCategorias(page, ['Fantasia', 'Ficção Científica', 'História']);
  await atualizar(page);
  await conferirResultado(page, '01/11/2025', '31/10/2026');

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
  await informarPeriodo(page, '2025-11-01', '2026-10-31');
  await escolherCategorias(page, ['Fantasia', 'Ficção Científica', 'História']);
  await atualizar(page);

  await conferirResultado(page, '01/11/2025', '31/10/2026');
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
  await informarPeriodo(page, '2025-01-01', '2025-06-30');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);
  await conferirResultado(page, '01/01/2025', '30/06/2025');

  await informarPeriodo(page, '2025-01-01', '2025-12-31');
  const inicio = Date.now();
  await Promise.all([
    page.waitForResponse(
      (resposta) =>
        resposta.url().includes('/api/admin/analise?') &&
        resposta.url().includes('inicio=2025-01-01') &&
        resposta.url().includes('fim=2025-12-31') &&
        resposta.status() === 200,
    ),
    atualizar(page),
  ]);
  const tempo = Date.now() - inicio;

  expect(tempo).toBeLessThan(1000);
  await conferirResultado(page, '01/01/2025', '31/12/2025');
  await conferirLinha(page, '03/2025', ['R$ 95,76']);
});

test('9. (extra) período inválido mostra a violação e não desenha o gráfico (RF-ANA-002)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-12-01', '2025-01-31');
  await escolherCategorias(page, ['Romance']);
  await atualizar(page);

  const alerta = page.getByRole('alert').filter({ hasText: 'RF-ANA-002' });
  await rolarAte(alerta);
  await expect(alerta).toContainText('A data final não pode ser anterior à inicial.');
  await expect(page.getByRole('heading', { name: /Evolução de/ })).toHaveCount(0);
  await expect(page.getByRole('img', { name: /Gráfico de linhas/ })).toHaveCount(0);
});

test('10. (extra) administrador exporta os dados do gráfico em CSV (RF-ANA-004)', async ({ page }) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-03-01', '2025-04-30');
  await escolherCategorias(page, ['História', 'Romance']);
  await atualizar(page);
  await conferirResultado(page, '01/03/2025', '30/04/2025');
  await conferirLinha(page, '03/2025', ['R$ 95,76', 'R$ 95,76']);
  await conferirLinha(page, '04/2025', ['R$ 875,73', 'R$ 168,99']);

  const [download, resposta] = await Promise.all([
    page.waitForEvent('download'),
    page.waitForResponse(
      (requisicao) =>
        requisicao.url().includes('/api/admin/analise/exportacao') && requisicao.status() === 200,
    ),
    page.getByRole('button', { name: 'Exportar CSV' }).click(),
  ]);

  expect(download.suggestedFilename()).toBe('analise-de-vendas-2025-03-01-a-2025-04-30.csv');
  expect(resposta.headers()['content-disposition'] ?? '').toContain(
    'analise-de-vendas-2025-03-01-a-2025-04-30.csv',
  );
  // O navegador consome a resposta para gerar o download; o conteúdo é lido do arquivo que o usuário recebeu.
  const caminho = await download.path();
  expect(caminho, 'arquivo CSV baixado').toBeTruthy();
  const csv = await readFile(caminho ?? '', 'utf8');
  expect(csv).toBe(
    '\uFEFFmes;categoria;valor\r\n' +
      '2025-03;História;95,76\r\n' +
      '2025-03;Romance;95,76\r\n' +
      '2025-04;História;875,73\r\n' +
      '2025-04;Romance;168,99\r\n',
  );
  await test.info().attach('csv-da-analise', { body: csv, contentType: 'text/csv' });
});

test('11. (extra) filtra por dias dentro de um mês e mantém um único ponto no gráfico (RF-ANA-002, DEC-19)', async ({
  page,
}) => {
  await abrirAnalise(page);
  await informarPeriodo(page, '2025-03-01', '2025-03-10');
  await escolherCategorias(page, ['Ficção Científica', 'Fantasia', 'Romance']);
  await atualizar(page);

  await conferirResultado(page, '01/03/2025', '10/03/2025');
  // O pedido 44, de 05/03, é o único do recorte: Dom Casmurro entra cheio em Romance (2 × R$ 47,88).
  await conferirLinha(page, '03/2025', ['R$ 0,00', 'R$ 0,00', 'R$ 95,76']);
  // Mesmo filtrando por dias, o eixo segue mensal: março aparece uma única vez na tabela.
  await expect(page.getByRole('row', { name: /^\d{2}\/\d{4}/ })).toHaveCount(1);

  // O administrador aproxima o recorte para 12/03–20/03: agora entram os pedidos 45 e 46, e não o de 05/03.
  await informarPeriodo(page, '2025-03-12', '2025-03-20');
  await atualizar(page);

  await conferirResultado(page, '12/03/2025', '20/03/2025');
  // 12/03: Fundação (3 × R$ 48,00 menos a unidade trocada) = R$ 96,00. 18/03: O Hobbit (2 × R$ 40,00) = R$ 80,00.
  await conferirLinha(page, '03/2025', ['R$ 80,00', 'R$ 96,00', 'R$ 0,00']);
  await expect(page.getByRole('row', { name: /^\d{2}\/\d{4}/ })).toHaveCount(1);
});
