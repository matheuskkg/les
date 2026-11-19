import type { Page } from '@playwright/test';
import {
  apiDaPagina,
  entrarComo,
  entrarNaApi,
  expect,
  rolarAte,
  test,
  tokenCsrf,
} from './fixtures';

/**
 * Roteiro de apresentação do cadastro de clientes: criar, consultar, alterar e inativar (RF-CLI-001 a RF-CLI-008,
 * RNF-CLI-001 a RNF-CLI-005, RN-CLI-001 a RN-CLI-007). A atividade fala em "exclusão", mas o DEC-10 não exclui
 * cliente: o fim da conta é a inativação, reativável em 7 dias e encerrada depois disso. Os cenários 15 a 19 mostram
 * as três situações e o que muda (CPF, e-mail e telefone livres, histórico preservado).
 *
 * Como todo E2E do projeto, é roteiro de apresentação, não suíte de regressão: o login e os dados de apoio podem vir
 * da API, mas o que cada cenário quer provar acontece na tela e termina em algo visível. Os números esperados vêm do
 * seed (201_clientes.sql e 400_pedidos_historicos.sql) e estão explicados nos comentários.
 */

const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };
const ANA = 'ana.souza@livraria.test';
const BRUNO = 'bruno.lima@livraria.test';
const CARLA = 'carla.mendes@livraria.test';
const DIEGO = 'diego.rocha@livraria.test';
const ELISA = 'elisa.prado@livraria.test';
const SENHA = 'Senha@123';

/** 24/10/2026: Carla foi inativada em 20/10/2026, então está INATIVA (4 dias); Diego, inativado em 01/09, está ENCERRADA. */
const RELOGIO_DA_INATIVACAO = '2026-10-24T15:00:00Z';

async function abrirAdmin(page: Page, caminho = '/admin/clientes'): Promise<void> {
  await entrarNaApi(page, ADMIN.login, ADMIN.senha);
  await page.goto(caminho);
}

async function abrirCliente(page: Page, login: string, caminho: string): Promise<void> {
  await entrarNaApi(page, login, SENHA);
  await page.goto(caminho);
}

interface OpcoesDeCadastro {
  nome?: string;
  cpf?: string;
  email?: string;
  ddd?: string;
  telefone?: string;
  senha?: string;
  confirmacao?: string;
  cep?: string;
  comCartaoInvalido?: boolean;
}

/**
 * Preenche o cadastro como o visitante faria. CPF 111.222.333-96, e-mail e telefone novos: nenhum deles pertence a
 * uma conta em uso no seed. A máscara aceita os dígitos sem pontuação (cadastroDeCliente.ts).
 */
async function preencherCadastro(page: Page, opcoes: OpcoesDeCadastro = {}): Promise<void> {
  await page.goto('/cadastro');
  await expect(page.getByRole('heading', { name: 'Criar conta' })).toBeVisible();

  await page.getByLabel('Nome completo').fill(opcoes.nome ?? 'Maria Silva');
  await page.getByLabel('Gênero').selectOption('FEMININO');
  await page.getByLabel('Data de nascimento').fill('1991-05-20');
  await page.getByLabel('CPF').fill(opcoes.cpf ?? '11122233396');
  await page.getByLabel('E-mail').fill(opcoes.email ?? 'maria.silva@livraria.test');
  await page.getByLabel('Tipo de telefone').selectOption('CELULAR');
  await page.getByLabel('DDD').fill(opcoes.ddd ?? '11');
  await page.getByRole('textbox', { name: 'Telefone', exact: true }).fill(opcoes.telefone ?? '988887777');
  await page.getByLabel(/^Senha/).fill(opcoes.senha ?? SENHA);
  await page.getByLabel('Confirme a senha').fill(opcoes.confirmacao ?? opcoes.senha ?? SENHA);

  // RN-CLI-003: todos os campos obrigatórios do endereço. País é fixo, Brasil.
  await page.getByLabel('Nome curto').fill('Casa');
  await page.getByLabel('Tipo de endereço').selectOption('CASA');
  await page.getByLabel('Tipo de logradouro').selectOption('RUA');
  await page.getByRole('textbox', { name: 'Logradouro', exact: true }).fill('Rua das Acácias');
  await page.getByRole('textbox', { name: 'Número', exact: true }).fill('50');
  await page.getByLabel('Bairro').fill('Centro');
  await page.getByLabel('CEP').fill(opcoes.cep ?? '01310100');
  await page.getByLabel('Cidade').fill('São Paulo');
  await page.getByLabel('Estado (UF)').selectOption('SP');

  if (opcoes.comCartaoInvalido) {
    await page.getByRole('button', { name: 'Adicionar cartão' }).click();
    await page.getByLabel('Número do cartão').fill('9999999999999999');
    await page.getByLabel('Nome impresso').fill('MARIA SILVA');
    await page.getByLabel('Validade').fill('2030-12');
  }
}

async function enviarCadastro(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Criar conta' }).click();
}

test('1. visitante cadastra cliente com endereço e entra automaticamente (RF-CLI-001, RN-CLI-003, RN-CLI-006)', async ({
  page,
}) => {
  await preencherCadastro(page);
  await enviarCadastro(page);

  await expect(page.getByText('Olá, Maria Silva.')).toBeVisible();

  // O endereço informado no cadastro aparece em "Minha conta" (RF-CLI-006, RNF-CLI-004).
  await page.getByRole('link', { name: 'Endereços' }).click();
  const endereco = page.getByRole('listitem').filter({ hasText: 'Rua das Acácias' });
  await rolarAte(endereco);
  await expect(endereco).toContainText('Casa');
  await expect(endereco).toContainText('Rua das Acácias, 50');
  await expect(endereco).toContainText('CEP 01310-100');
});

test('2. cadastro recusa senha fraca e confirmação divergente (RNF-CLI-001, RNF-CLI-002)', async ({ page }) => {
  await preencherCadastro(page, { senha: 'senha1234', confirmacao: 'senha1234' });
  await enviarCadastro(page);

  // "senha1234" não tem caractere especial: a regra do servidor volta como violação RNF-CLI-001.
  const alerta = page.getByRole('alert').filter({ hasText: 'RNF-CLI-001' });
  await rolarAte(alerta);
  await expect(alerta).toContainText('A senha deve ter pelo menos um caractere especial.');

  // Agora a senha é forte, mas a confirmação é diferente.
  await page.getByLabel(/^Senha/).fill(SENHA);
  await page.getByLabel('Confirme a senha').fill('Senha@456');
  await enviarCadastro(page);
  await expect(page.getByText('As senhas não coincidem.')).toBeVisible();
});

test('3. cadastro recusa CPF de conta em uso (RN-CLI-006, DEC-10)', async ({ page }) => {
  // 529.982.247-25 é o CPF da Ana no seed (201_clientes.sql).
  await preencherCadastro(page, { cpf: '52998224725' });
  await enviarCadastro(page);

  const alerta = page.getByRole('alert').filter({ hasText: 'Já existe um cliente com este CPF' });
  await rolarAte(alerta);
  await expect(alerta).toContainText('Já existe um cliente com este CPF em uma conta em uso.');
});

test('4. cadastro recusa cartão de bandeira não aceita (RN-CLI-004, RN-CLI-005, DEC-18)', async ({ page }) => {
  await preencherCadastro(page, { comCartaoInvalido: true });
  await enviarCadastro(page);

  // Nenhuma bandeira do seed aceita o prefixo 9999 (200_bandeiras.sql).
  const alerta = page.getByRole('alert').filter({ hasText: 'RN-CLI-005' });
  await rolarAte(alerta);
  await expect(alerta).toContainText(/bandeira do cartão não aceita/i);
});

test('5. (extra) cadastro avisa CEP com menos de 8 dígitos (RN-CLI-003)', async ({ page }) => {
  await preencherCadastro(page, { cep: '123' });
  await enviarCadastro(page);

  await expect(page.getByText('Informe um CEP com 8 dígitos.')).toBeVisible();
  await expect(page.getByText('Olá, Maria Silva.')).toHaveCount(0);
});

test('6. administrador consulta clientes ativos e vê dados formatados e ranking (RF-CLI-004, RN-CLI-007)', async ({
  page,
}) => {
  await abrirAdmin(page);

  // No seed há 10 clientes; Carla está inativada e Diego encerrado, então 8 estão ativos.
  await page.getByRole('group', { name: 'Situação' }).getByRole('checkbox', { name: 'Ativa', exact: true }).check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page.getByText('8 clientes encontrados')).toBeVisible();
  await expect(page.getByRole('columnheader', { name: 'Ranking' })).toBeVisible();

  const linha = page.getByRole('row', { name: /Ana Souza/ });
  await rolarAte(linha);
  const celulas = linha.getByRole('cell');
  await expect(celulas.nth(0)).toHaveText('Ana Souza');
  await expect(celulas.nth(1)).toHaveText('529.982.247-25');
  await expect(celulas.nth(2)).toHaveText('ana.souza@livraria.test');
  await expect(celulas.nth(5)).toHaveText('Feminino');
  await expect(celulas.nth(6)).toHaveText('Ativa');
});

test('7. administrador combina filtros e limpa a consulta (RF-CLI-004)', async ({ page }) => {
  await abrirAdmin(page);

  // Gênero "Não informado" + situação "Ativa": Elisa, Fernanda, Gustavo, Helena, Ivan e Juliana (seed 201_clientes.sql).
  await page.getByRole('group', { name: 'Gênero' }).getByRole('checkbox', { name: 'Não informado', exact: true }).check();
  await page.getByRole('group', { name: 'Situação' }).getByRole('checkbox', { name: 'Ativa', exact: true }).check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page.getByText('6 clientes encontrados')).toBeVisible();
  await expect(page.getByRole('row', { name: /Elisa Prado/ })).toBeVisible();
  await expect(page.getByRole('row', { name: /Ana Souza/ })).toHaveCount(0);

  await page.getByRole('button', { name: 'Limpar filtros' }).click();
  await expect(page.getByText('10 clientes encontrados')).toBeVisible();
});

test('8. administrador abre detalhe com endereços, cartões e transações (RF-CLI-004, RF-CLI-005, RN-CLI-007)', async ({
  page,
}) => {
  await abrirAdmin(page);
  await page.getByLabel('Nome').fill('Elisa Prado');
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await page.getByRole('link', { name: 'Elisa Prado' }).click();

  await expect(page.getByRole('heading', { name: 'Elisa Prado' })).toBeVisible();
  await expect(page.getByText(/Rua dos Andradas, 700/)).toBeVisible();
  await expect(page.getByText(/Alameda Santos, 500/)).toBeVisible();

  // Os dois cartões do seed (ids 4 e 5) voltam mascarados e só um é preferencial (DEC-10).
  const cartoes = page.getByRole('heading', { name: 'Cartões' }).locator('..');
  await rolarAte(cartoes);
  await expect(cartoes).toContainText('**** **** **** 0005');
  await expect(cartoes).toContainText('**** **** **** 0000');
  await expect(cartoes.getByText('Preferencial')).toHaveCount(1);
  await expect(page.getByText('Ranking')).toBeVisible();

  // O histórico no cadastro inclui pedidos REPROVADA (seed 400_pedidos_historicos.sql: pedido 14, entre outros).
  const pedidos = page.getByRole('heading', { name: 'Pedidos' }).locator('..');
  await rolarAte(pedidos);
  await expect(pedidos).toContainText('REPROVADA');
  await expect(pedidos).toContainText(/Mostrando os 20 mais recentes de \d+/);
  await expect(pedidos.getByRole('row')).toHaveCount(21);

  // Os filtros da consulta acompanham o detalhe (RF-CLI-004): o retorno não os perde.
  await page.getByRole('link', { name: 'Voltar para os clientes' }).click();
  await expect(page.getByLabel('Nome')).toHaveValue('Elisa Prado');
  await expect(page.getByText('1 cliente encontrado')).toBeVisible();
});

test('9. cliente consulta todas as suas transações (RF-CLI-005)', async ({ page }) => {
  await abrirCliente(page, ELISA, '/minha-conta/pedidos');
  await expect(page.getByRole('heading', { name: 'Meus pedidos' })).toBeVisible();

  // Elisa tem pedidos ENTREGUE e REPROVADA no histórico; a consulta do cliente mostra todos, sem filtro de status.
  const reprovada = page.getByRole('row').filter({ hasText: 'REPROVADA' }).first();
  await rolarAte(reprovada);
  await expect(reprovada).toContainText('R$');
  await expect(page.getByRole('row').filter({ hasText: 'ENTREGUE' }).first()).toBeVisible();

  await reprovada.getByRole('link').first().click();
  await expect(page.getByRole('heading', { name: /Pedido \d+/ })).toBeVisible();
  await expect(page.getByText('REPROVADA', { exact: true })).toBeVisible();
});

test('10. cliente altera dados cadastrais sem mexer em endereços (RF-CLI-002)', async ({ page }) => {
  await abrirCliente(page, ANA, '/minha-conta/dados');
  await expect(page.getByRole('heading', { name: 'Meus dados' })).toBeVisible();
  await expect(page.getByText(/Ranking de cliente: \d+ de 100/)).toBeVisible();

  await page.getByRole('textbox', { name: 'Nome', exact: true }).fill('Ana Souza Lima');
  await page.getByLabel('E-mail').fill('ana.souza.lima@livraria.test');
  await page.getByRole('button', { name: 'Salvar alterações' }).click();

  await expect(page.getByText('Dados atualizados.')).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Nome', exact: true })).toHaveValue('Ana Souza Lima');
  await expect(page.getByLabel('E-mail')).toHaveValue('ana.souza.lima@livraria.test');
});

test('11. cliente altera, adiciona e exclui endereço sem editar os demais dados (RF-CLI-006, RNF-CLI-004, RN-CLI-003)', async ({
  page,
}) => {
  await abrirCliente(page, ANA, '/minha-conta/enderecos');
  await expect(page.getByRole('heading', { name: 'Endereços' })).toBeVisible();
  // Ana tem dois endereços no seed: Casa (Rua das Flores) e Trabalho (Avenida Paulista).
  await expect(page.getByRole('listitem')).toHaveCount(2);

  const casa = page.getByRole('listitem').filter({ hasText: 'Rua das Flores' });
  await casa.getByRole('button', { name: 'Alterar' }).click();
  await page.getByLabel('Nome curto').fill('Casa da Ana');
  await page.getByRole('textbox', { name: 'Logradouro', exact: true }).fill('Rua das Acácias');
  await page.getByRole('button', { name: 'Salvar endereço' }).click();
  const alterada = page.getByRole('listitem').filter({ hasText: 'Rua das Acácias' });
  await rolarAte(alterada);
  await expect(alterada).toContainText('Casa da Ana');

  await page.getByRole('button', { name: 'Adicionar endereço' }).click();
  await page.getByLabel('Nome curto').fill('Sítio');
  await page.getByRole('textbox', { name: 'Logradouro', exact: true }).fill('Estrada do Sol');
  await page.getByRole('textbox', { name: 'Número', exact: true }).fill('200');
  await page.getByLabel('Bairro').fill('Vila Nova');
  await page.getByLabel('CEP').fill('01310100');
  await page.getByLabel('Cidade').fill('São Paulo');
  await page.getByLabel('UF').selectOption('SP');
  await page.getByRole('button', { name: 'Salvar endereço' }).click();
  await expect(page.getByRole('listitem')).toHaveCount(3);
  await expect(page.getByRole('listitem').filter({ hasText: 'Estrada do Sol' })).toBeVisible();

  await page.getByRole('listitem').filter({ hasText: 'Estrada do Sol' }).getByRole('button', { name: 'Excluir' }).click();
  await expect(page.getByRole('listitem')).toHaveCount(2);
  await expect(page.getByRole('listitem').filter({ hasText: 'Estrada do Sol' })).toHaveCount(0);
});

test('12. último endereço não pode ser excluído (RN-CLI-001, RN-CLI-002, DEC-10)', async ({ page }) => {
  // O cliente com um único endereço é dado de apoio: a recusa, que é o cenário, acontece na tela.
  const csrf = await tokenCsrf(page);
  const resposta = await apiDaPagina(page).post('/api/clientes', {
    headers: { 'X-XSRF-TOKEN': csrf },
    data: {
      nome: 'Cliente de Um Endereço',
      genero: 'NAO_INFORMADO',
      dataNascimento: '1990-01-01',
      cpf: '11122233396',
      email: 'um.endereco@livraria.test',
      telefone: { tipo: 'CELULAR', ddd: '11', numero: '988889999' },
      senha: SENHA,
      confirmacaoSenha: SENHA,
      enderecos: [
        {
          nomeCurto: 'Único',
          tipoEndereco: 'CASA',
          tipoLogradouro: 'RUA',
          logradouro: 'Rua Única',
          numero: '1',
          bairro: 'Centro',
          cep: '01310100',
          cidade: 'São Paulo',
          uf: 'SP',
          observacoes: '',
        },
      ],
      cartoes: [],
    },
  });
  expect(resposta.status(), `POST /api/clientes falhou (${resposta.status()}): ${await resposta.text()}`).toBe(201);

  await abrirCliente(page, 'um.endereco@livraria.test', '/minha-conta/enderecos');
  await page.getByRole('listitem').filter({ hasText: 'Rua Única' }).getByRole('button', { name: 'Excluir' }).click();

  const alerta = page.getByRole('alert').filter({ hasText: 'RN-CLI-001' });
  await rolarAte(alerta);
  await expect(alerta).toContainText('Não é possível excluir o único endereço do cliente');
  await expect(page.getByRole('listitem').filter({ hasText: 'Rua Única' })).toBeVisible();
});

test('13. cliente altera somente a senha e mantém a sessão (RF-CLI-008, RNF-CLI-001, RNF-CLI-002)', async ({
  page,
}) => {
  await abrirCliente(page, ANA, '/minha-conta/senha');
  await page.getByLabel('Senha atual').fill(SENHA);
  await page.getByLabel(/^Nova senha/).fill('Nova@456');
  await page.getByLabel('Confirme a nova senha').fill('Nova@456');
  await page.getByRole('button', { name: 'Alterar senha' }).click();

  await expect(page.getByText('Senha alterada.')).toBeVisible();
  // A sessão atual continua válida: o cliente navega para Meus dados sem entrar de novo.
  await page.getByRole('link', { name: 'Meus dados' }).click();
  await expect(page.getByRole('heading', { name: 'Meus dados' })).toBeVisible();
  await expect(page.getByRole('textbox', { name: 'Nome', exact: true })).toHaveValue('Ana Souza');
});

test('14. cliente adiciona cartão, troca o preferencial e exclui o preferencial (RF-CLI-007, RN-CLI-004, RN-CLI-005)', async ({
  page,
}) => {
  await abrirCliente(page, ANA, '/minha-conta/cartoes');
  const cartaoInicial = page.getByRole('listitem').filter({ hasText: '1111' });
  await expect(cartaoInicial).toContainText('Preferencial');

  // 4012 8888 8888 1881 é um Visa de teste com dígito verificador válido (DEC-18).
  await page.getByRole('button', { name: 'Adicionar cartão' }).click();
  await page.getByLabel('Número do cartão').fill('4012888888881881');
  await page.getByLabel('Nome impresso').fill('ANA SOUZA');
  await page.getByLabel('Validade').fill('2031-06');
  await page.getByRole('button', { name: 'Salvar cartão' }).click();

  const novo = page.getByRole('listitem').filter({ hasText: '1881' });
  await rolarAte(novo);
  await expect(novo).toContainText('**** **** **** 1881');
  await expect(novo).not.toContainText('Preferencial');

  await novo.getByRole('button', { name: 'Tornar preferencial' }).click();
  await expect(page.getByRole('listitem').filter({ hasText: '1881' })).toContainText('Preferencial');
  await expect(page.getByRole('listitem').filter({ hasText: '1111' })).not.toContainText('Preferencial');

  // Excluir o preferencial promove o cartão restante (DEC-10).
  await page.getByRole('listitem').filter({ hasText: '1881' }).getByRole('button', { name: 'Excluir' }).click();
  await expect(page.getByRole('listitem').filter({ hasText: '1881' })).toHaveCount(0);
  await expect(page.getByRole('listitem').filter({ hasText: '1111' })).toContainText('Preferencial');
});

test('15. cliente inativa a própria conta com confirmação e a sessão termina (RF-CLI-003, DEC-10)', async ({
  page,
}) => {
  await abrirCliente(page, BRUNO, '/minha-conta/dados');
  await rolarAte(page.getByRole('heading', { name: 'Inativar conta' }));
  await page.getByRole('button', { name: 'Inativar minha conta' }).click();
  await page.getByRole('button', { name: 'Confirmar inativação' }).click();

  // A confirmação encerra a sessão e leva de volta à vitrine, sem o menu da conta.
  await expect(page.getByRole('banner').getByRole('link', { name: 'Entrar' })).toBeVisible();
  await expect(page.getByRole('banner').getByRole('button', { name: /opções da conta/i })).toHaveCount(0);
});

test('16. login em até 7 dias reativa a conta inativa (RF-CLI-003, DEC-10)', async ({ page, ajustarRelogio }) => {
  // Carla foi inativada em 20/10/2026; em 24/10 ainda está dentro dos 7 dias.
  await ajustarRelogio(RELOGIO_DA_INATIVACAO);
  await entrarComo(page, CARLA, SENHA);
  await expect(page.getByText('Olá, Carla Mendes.')).toBeVisible();
});

test('17. administrador distingue conta inativa de encerrada na consulta (RF-CLI-003, RF-CLI-004, DEC-10)', async ({
  page,
  ajustarRelogio,
}) => {
  await ajustarRelogio(RELOGIO_DA_INATIVACAO);
  await abrirAdmin(page);

  await page.getByRole('group', { name: 'Situação' }).getByRole('checkbox', { name: 'Inativa', exact: true }).check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page.getByText('1 cliente encontrado')).toBeVisible();
  await expect(page.getByRole('row', { name: /Carla Mendes/ })).toContainText('Inativa');

  await page.getByRole('group', { name: 'Situação' }).getByRole('checkbox', { name: 'Inativa', exact: true }).uncheck();
  await page.getByRole('group', { name: 'Situação' }).getByRole('checkbox', { name: 'Encerrada', exact: true }).check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page.getByText('1 cliente encontrado')).toBeVisible();
  await expect(page.getByRole('row', { name: /Diego Rocha/ })).toContainText('Encerrada');
});

test('18. conta encerrada não reativa: login pela tela é recusado (RF-CLI-003, DEC-10)', async ({
  page,
  ajustarRelogio,
}) => {
  // Diego foi inativado em 01/09/2026: passados os 7 dias, a conta não volta.
  await ajustarRelogio(RELOGIO_DA_INATIVACAO);
  await page.goto('/login');
  await page.getByLabel('E-mail ou telefone').fill(DIEGO);
  await page.getByLabel('Senha').fill(SENHA);
  await page.getByRole('button', { name: 'Entrar' }).click();

  const alerta = page.getByRole('alert');
  await rolarAte(alerta);
  await expect(alerta).toContainText('Login ou senha inválidos.');
});

test('19. encerrar não é excluir: dados viram livre para novo cadastro e histórico fica (RF-CLI-001, RF-CLI-003)', async ({
  page,
  ajustarRelogio,
}) => {
  await ajustarRelogio(RELOGIO_DA_INATIVACAO);

  // CPF, e-mail e telefone do Diego (seed 201_clientes.sql) pertencem a uma conta encerrada e podem ser reusados.
  await preencherCadastro(page, {
    nome: 'Diego Rocha Junior',
    cpf: '98765432100',
    email: DIEGO,
    ddd: '41',
    telefone: '988887004',
  });
  await enviarCadastro(page);
  await expect(page.getByText('Olá, Diego Rocha Junior.')).toBeVisible();

  // O Diego antigo continua na consulta: encerrar não apaga o cliente do histórico (DEC-10).
  await entrarNaApi(page, ADMIN.login, ADMIN.senha);
  await page.goto('/admin/clientes');
  await page.getByRole('group', { name: 'Situação' }).getByRole('checkbox', { name: 'Encerrada', exact: true }).check();
  await page.getByRole('button', { name: 'Filtrar' }).click();
  await expect(page.getByText('1 cliente encontrado')).toBeVisible();
  await expect(page.getByRole('row', { name: /Diego Rocha/ })).toContainText('Encerrada');
});
