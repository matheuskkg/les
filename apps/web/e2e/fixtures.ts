import {
  expect,
  test as base,
  type APIRequestContext,
  type Locator,
  type Page,
} from '@playwright/test';

/** Apoio exposto pelos testes; o reset roda sozinho antes de cada teste (DEC-09). */
interface ApoioAosTestes {
  ajustarRelogio: (instante: string) => Promise<void>;
  resetarBanco: void;
}

export const test = base.extend<ApoioAosTestes>({
  // POST /api/teste/reset: volta o banco ao seed e o relógio ao do sistema.
  resetarBanco: [
    async ({ request }, use) => {
      const resposta = await request.post('/api/teste/reset');
      expect(
        resposta.status(),
        `POST /api/teste/reset falhou (${resposta.status()}): ${await resposta.text()}`,
      ).toBe(204);
      await use();
    },
    { auto: true },
  ],

  // POST /api/teste/relogio — corpo esperado pelo controller: { "agora": "<ISO-8601>" }.
  ajustarRelogio: async ({ request }, use) => {
    await use(async (instante: string) => {
      const resposta = await request.post('/api/teste/relogio', {
        data: { agora: instante },
      });
      expect(
        resposta.status(),
        `POST /api/teste/relogio falhou (${resposta.status()}): ${await resposta.text()}`,
      ).toBe(204);
    });
  },
});

export { expect };

/**
 * Rola a página até o elemento antes da validação, como o usuário faria para enxergar o resultado.
 * As ações (click, fill, check) já rolam sozinhas; use isto nas asserções de conteúdo que podem
 * ficar abaixo da dobra, para a apresentação mostrar o alvo do teste na tela.
 */
export async function rolarAte(locator: Locator): Promise<void> {
  await locator.scrollIntoViewIfNeeded();
}

/**
 * Contexto de API que compartilha os cookies do navegador. Use para preparar dados que não são o assunto do
 * cenário (um usuário, um endereço, um cartão) sem passar pela tela.
 */
export function apiDaPagina(page: Page): APIRequestContext {
  return page.context().request;
}

/**
 * Lê o cookie XSRF-TOKEN que o server grava em `GET /api/sessao`. Toda escrita via API precisa do valor no
 * cabeçalho `X-XSRF-TOKEN`; chame de novo depois de login/logout, que rotacionam o token.
 */
export async function tokenCsrf(page: Page): Promise<string> {
  const consulta = await apiDaPagina(page).get('/api/sessao');
  expect([200, 401]).toContain(consulta.status());
  const token = (await page.context().cookies()).find((cookie) => cookie.name === 'XSRF-TOKEN')?.value;
  expect(token, 'cookie XSRF-TOKEN').toBeTruthy();
  return token ?? '';
}

/**
 * Entra pela API, sem passar pela tela de login, e deixa o cookie de sessão no contexto do navegador.
 * Use nos cenários em que o login não é o assunto; chame antes de abrir a tela do fluxo.
 */
export async function entrarNaApi(page: Page, login: string, senha: string): Promise<void> {
  const csrf = await tokenCsrf(page);
  const resposta = await apiDaPagina(page).post('/api/sessao', {
    headers: { 'X-XSRF-TOKEN': csrf },
    data: { login, senha },
  });
  expect(
    resposta.status(),
    `POST /api/sessao falhou (${resposta.status()}): ${await resposta.text()}`,
  ).toBe(200);
}

/** Abre o menu do usuário no cabeçalho (avatar, nome e seta), onde ficaram "Minha conta" e "Sair" (ALT-16). */
export async function abrirMenuDoUsuario(page: Page): Promise<void> {
  await page.getByRole('banner').getByRole('button', { name: /opções da conta/i }).click();
}

/** Entra pela tela /login e espera o cabeçalho mostrar a sessão. Use só em cenário que demonstra o login. */
export async function entrarComo(page: Page, login: string, senha: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('E-mail ou telefone').fill(login);
  await page.getByLabel('Senha').fill(senha);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('banner').getByRole('button', { name: /opções da conta/i })).toBeVisible();
}

/** Sai pelo menu do usuário no cabeçalho. */
export async function sair(page: Page): Promise<void> {
  await abrirMenuDoUsuario(page);
  await page.getByRole('menu').getByRole('menuitem', { name: 'Sair' }).click();
}

/** Põe um Dom Casmurro (livro 1, R$ 47,88) no carrinho pela página do livro. */
export async function adicionarDomCasmurro(page: Page): Promise<void> {
  await page.goto('/livros/1');
  await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
  await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();
}

/**
 * A cliente já logada compra um Dom Casmurro (R$ 47,88) para o primeiro endereço, em SP (frete R$ 12,00), pagando com
 * o cartão salvo de final {@link finalDoCartao}, e devolve o número do pedido aprovado (RF-VEN-008).
 */
export async function comprarDomCasmurro(page: Page, finalDoCartao: string, idDoCartao: number): Promise<string> {
  await adicionarDomCasmurro(page);
  await page.goto('/carrinho');
  await page.getByRole('button', { name: 'Ir para pagamento' }).click();
  await expect(page.getByTestId('restante')).toHaveText('R$ 59,88');
  await page.getByRole('group', { name: 'Cartões' }).getByLabel(new RegExp(finalDoCartao)).check();
  await page.locator(`#cartao-${idDoCartao}-cvv`).fill('123');
  await page.getByRole('button', { name: 'Finalizar compra' }).click();
  return numeroDoPedidoAprovado(page);
}

/** Lê o número do pedido na confirmação de compra aprovada (RF-VEN-008). */
export async function numeroDoPedidoAprovado(page: Page): Promise<string> {
  const aprovado = page.getByTestId('pedido-aprovado');
  await expect(aprovado).toContainText(/Pedido nº \d+ realizado/);
  const numero = /Pedido nº (\d+)/.exec((await aprovado.textContent()) ?? '')?.[1];
  expect(numero, 'número do pedido aprovado').toBeTruthy();
  return numero ?? '';
}
