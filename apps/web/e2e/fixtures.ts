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

/** Credenciais do seed usadas nos roteiros de venda ([DEC-14](../docs/decisoes/DEC-14-ids-nomes-e-seed.md)). */
export const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' } as const;
export const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' } as const;

/** Entra como a cliente do seed. O login não é o cenário dos roteiros de venda. */
export async function entrarComoCliente(page: Page): Promise<void> {
  await entrarNaApi(page, ANA.login, ANA.senha);
}

/** Entra como o administrador do seed. */
export async function entrarComoAdmin(page: Page): Promise<void> {
  await entrarNaApi(page, ADMIN.login, ADMIN.senha);
}

/** Abre a gaveta do carrinho pelo cabeçalho e devolve o `dialog` para as asserções. */
export async function abrirCarrinho(page: Page): Promise<Locator> {
  await page.getByRole('banner').getByRole('button', { name: /^Carrinho/ }).click();
  const gaveta = page.getByRole('dialog', { name: 'Carrinho' });
  await expect(gaveta).toBeVisible();
  return gaveta;
}

/** Adiciona um livro pela tela, informando a quantidade no campo da página do livro quando pedida. */
export async function adicionarLivro(page: Page, livroId: number, quantidade?: number): Promise<void> {
  await page.goto(`/livros/${livroId}`);
  if (quantidade !== undefined) {
    const campo = page.getByLabel('Quantidade');
    await campo.fill(String(quantidade));
    await campo.press('Enter');
  }
  await page.getByRole('button', { name: 'Adicionar ao carrinho' }).click();
  await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();
}

/** Sai da página do carrinho para o checkout (RF-VEN-003). */
export async function irParaPagamento(page: Page): Promise<void> {
  await page.goto('/carrinho');
  await page.getByRole('button', { name: 'Ir para pagamento' }).click();
  await expect(page.getByRole('heading', { name: 'Pagamento', exact: true })).toBeVisible();
}

/** Espera o valor visível de "A pagar com cartão" no resumo do checkout. */
export async function esperarRestante(page: Page, valor: string): Promise<void> {
  await expect(page.getByTestId('restante')).toHaveText(valor);
}

/** Marca um cartão salvo pelo final do número, como o usuário faz no passo Cartões. */
export async function selecionarCartaoSalvo(page: Page, final: string): Promise<void> {
  await page.getByRole('group', { name: 'Cartões' }).getByLabel(new RegExp(final)).check();
}

/** Preenche o código de segurança de um cartão salvo (`id`) ou do cartão novo (`novo`). */
export async function preencherCvv(page: Page, chave: string | number, cvv: string): Promise<void> {
  await page.locator(`#cartao-${chave}-cvv`).fill(cvv);
}

/** Confere a distribuição na tela antes de finalizar. */
export async function conferirPagamento(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Conferir pagamento' }).click();
  await expect(page.getByRole('status').filter({ hasText: 'Pagamento conferido:' })).toBeVisible();
}

/** Finaliza a compra e devolve o número do pedido aprovado. */
export async function finalizarCompra(page: Page): Promise<string> {
  await page.getByRole('button', { name: 'Finalizar compra' }).click();
  return numeroDoPedidoAprovado(page);
}

/** Abre "Meus pedidos" pela navegação da conta. */
export async function abrirMeusPedidos(page: Page): Promise<void> {
  await page.goto('/minha-conta');
  await page.getByRole('navigation', { name: 'Seções da conta' }).getByRole('link', { name: 'Meus pedidos' }).click();
  await expect(page.getByRole('heading', { name: 'Meus pedidos' })).toBeVisible();
}

interface DadosDoEndereco {
  nomeCurto: string;
  cep: string;
  cidade: string;
  uf: string;
  salvarNoCadastro?: boolean;
}

/** Preenche um endereço novo no checkout (RN-CLI-003) e confirma com "Usar este endereço". */
export async function preencherNovoEndereco(page: Page, dados: DadosDoEndereco): Promise<void> {
  const entrega = page.getByRole('group', { name: 'Endereço de entrega' });
  await entrega.getByRole('radio', { name: 'Outro endereço' }).check();
  await page.locator('#entrega-nome').fill(dados.nomeCurto);
  await page.locator('#entrega-tipo').selectOption('CASA');
  await page.locator('#entrega-logradouro-tipo').selectOption('AVENIDA');
  await page.locator('#entrega-logradouro').fill('Avenida Beira Mar');
  await page.locator('#entrega-numero').fill('500');
  await page.locator('#entrega-bairro').fill('Centro');
  await page.locator('#entrega-cep').fill(dados.cep);
  await page.locator('#entrega-cidade').fill(dados.cidade);
  await page.locator('#entrega-uf').selectOption(dados.uf);
  if (dados.salvarNoCadastro) {
    await entrega.getByLabel('Salvar no meu cadastro').check();
  }
  await entrega.getByRole('button', { name: 'Usar este endereço' }).click();
  // Com "Salvar no meu cadastro" o endereço passa a ser um radio pelo nome curto; sem ele, fica o aviso de uso.
  await expect(entrega).toContainText(
    dados.salvarNoCadastro ? dados.nomeCurto : 'Usando o endereço informado:',
  );
}

interface DadosDoCartao {
  numero: string;
  nomeImpresso: string;
  validade: string;
  cvv?: string;
  salvarNoCadastro?: boolean;
}

/** Preenche "Outro cartão" no checkout (RN-CLI-004/005) e, se pedido, incorpora o cartão ao perfil. */
export async function preencherNovoCartao(page: Page, dados: DadosDoCartao): Promise<void> {
  const cartoes = page.getByRole('group', { name: 'Cartões' });
  await cartoes.getByLabel('Outro cartão').check();
  await page.locator('#cartao-novo-numero').fill(dados.numero);
  await page.locator('#cartao-novo-nome').fill(dados.nomeImpresso);
  await page.locator('#cartao-novo-validade').fill(dados.validade);
  if (dados.cvv !== undefined) {
    await page.locator('#cartao-novo-cvv').fill(dados.cvv);
  }
  if (dados.salvarNoCadastro) {
    await cartoes.getByRole('button', { name: 'Salvar no meu cadastro' }).click();
    // Depois de salvo, o cartão migra da chave "novo" para o id gravado, levando valor e CVV digitados.
    await expect(cartoes).toContainText(dados.numero.slice(-4));
  }
}

/** Cria um cartão salvo pela API — dado de apoio quando o cenário é pagar com mais de um cartão. */
export async function criarCartaoSalvo(
  page: Page,
  dados: { numero: string; nomeImpresso: string; validade: string },
): Promise<void> {
  const csrf = await tokenCsrf(page);
  const resposta = await apiDaPagina(page).post('/api/me/cartoes', {
    headers: { 'X-XSRF-TOKEN': csrf },
    data: dados,
  });
  expect([200, 201], `POST /api/me/cartoes falhou (${resposta.status()}): ${await resposta.text()}`).toContain(
    resposta.status(),
  );
}

/**
 * Prepara um pedido ENTREGUE da Ana inteiramente pela tela, para os roteiros de troca. A compra, o despacho e a
 * confirmação da entrega são o cenário dos testes C1/C2; aqui só servem de apoio.
 */
export async function prepararPedidoEntregue(page: Page, quantidade = 2): Promise<string> {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1, quantidade);
  await irParaPagamento(page);
  await selecionarCartaoSalvo(page, '1111');
  await preencherCvv(page, 1, '123');
  const numero = await finalizarCompra(page);

  await entrarComoAdmin(page);
  await page.goto(`/admin/pedidos/${numero}`);
  await expect(page.getByTestId('status-do-pedido')).toHaveText('APROVADA');
  await page.getByRole('button', { name: 'Despachar' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('EM TRANSPORTE');
  await page.getByRole('button', { name: 'Confirmar entrega' }).click();
  await expect(page.getByTestId('status-do-pedido')).toHaveText('ENTREGUE');

  await entrarComoCliente(page);
  return numero;
}

/** Solicita uma troca no detalhe do pedido ENTREGUE e devolve o número do pedido. */
export async function solicitarTroca(page: Page, numero: string, quantidade: number): Promise<void> {
  await page.goto(`/minha-conta/pedidos/${numero}`);
  const formulario = page.locator('form').filter({ has: page.getByRole('heading', { name: 'Solicitar troca' }) });
  await expect(formulario).toBeVisible();
  await formulario.getByLabel(/Dom Casmurro/).fill(String(quantidade));
  await formulario.getByRole('button', { name: 'Solicitar troca' }).click();
  await expect(page.getByText('Troca solicitada. Avisaremos quando ela for analisada.')).toBeVisible();
}

/** Administra a troca até o recebimento, para o cenário que usa o cupom gerado. */
export async function concluirTroca(page: Page, quantidadeNoPedido = 2, quantidadeTrocada = 1): Promise<string> {
  const numero = await prepararPedidoEntregue(page, quantidadeNoPedido);
  await solicitarTroca(page, numero, quantidadeTrocada);

  await entrarComoAdmin(page);
  await page.goto(`/admin/pedidos/${numero}`);
  await page.getByRole('button', { name: 'Autorizar' }).click();
  const tabelaDeTrocas = page.getByTestId('trocas-do-pedido');
  await expect(tabelaDeTrocas.getByRole('cell', { name: 'TROCA AUTORIZADA' })).toBeVisible();
  await page.getByRole('button', { name: 'Confirmar recebimento' }).click();
  await expect(tabelaDeTrocas.getByRole('cell', { name: 'TROCADO' })).toBeVisible();

  await entrarComoCliente(page);
  return numero;
}
