import { type Page } from '@playwright/test';
import { entrarComo, expect, test } from './fixtures';

const NOVA = {
  nome: 'Fernanda Lima',
  cpf: '14725836982',
  email: 'fernanda.lima@livraria.test',
  telefone: '(11) 97654-3210',
  senha: 'Senha@123',
  novaSenha: 'Outra@Senha7',
};

async function cadastrar(page: Page): Promise<void> {
  await page.goto('/cadastro');
  await page.locator('#nome').fill(NOVA.nome);
  await page.locator('#genero').selectOption('FEMININO');
  await page.locator('#dataNascimento').fill('1995-04-12');
  await page.locator('#cpf').fill(NOVA.cpf);
  await page.locator('#email').fill(NOVA.email);
  await page.locator('#telefone-tipo').selectOption('CELULAR');
  await page.locator('#ddd').fill('11');
  await page.locator('#telefone-numero').fill('976543210');
  await page.locator('#senha').fill(NOVA.senha);
  await page.locator('#confirmacaoSenha').fill(NOVA.senha);
  await page.locator('#endereco-0-nome-curto').fill('Casa');
  await page.locator('#endereco-0-tipo').selectOption('CASA');
  await page.locator('#endereco-0-tipo-logradouro').selectOption('RUA');
  await page.locator('#endereco-0-logradouro').fill('Rua das Flores');
  await page.locator('#endereco-0-numero').fill('42');
  await page.locator('#endereco-0-bairro').fill('Centro');
  await page.locator('#endereco-0-cep').fill('01001000');
  await page.locator('#endereco-0-cidade').fill('São Paulo');
  await page.locator('#endereco-0-uf').selectOption('SP');
  await page.getByRole('button', { name: 'Criar conta' }).click();
  await expect(page).toHaveURL(/\/minha-conta/);
  await expect(page.getByRole('banner').getByRole('button', { name: 'Sair' })).toBeVisible();
}

async function inativarConta(page: Page): Promise<void> {
  await page.goto('/minha-conta/dados');
  await page.getByRole('button', { name: 'Inativar minha conta' }).click();
  await page.getByRole('button', { name: 'Confirmar inativação' }).click();
  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole('banner').getByRole('link', { name: 'Entrar', exact: true })).toBeVisible();
}

async function tentarEntrar(page: Page, login: string, senha: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('E-mail ou telefone').fill(login);
  await page.getByLabel('Senha').fill(senha);
  await page.getByRole('button', { name: 'Entrar' }).click();
}

test.describe('Jornada do cliente (E2E-02)', () => {
  test('do cadastro ao encerramento da conta e ao novo cadastro com o mesmo CPF', async ({ page, ajustarRelogio }) => {
    test.setTimeout(90_000);

    // RF-CLI-001: cadastro com endereço; a conta já entra logada.
    await cadastrar(page);

    // DEC-10: login por e-mail.
    await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
    await entrarComo(page, NOVA.email, NOVA.senha);

    // RF-CLI-002: alteração dos dados cadastrais.
    await page.goto('/minha-conta/dados');
    await page.getByLabel('Nome', { exact: false }).first().fill('Fernanda Lima Rocha');
    await page.getByRole('button', { name: 'Salvar alterações' }).click();
    await expect(page.getByText('Dados atualizados.')).toBeVisible();

    // RF-CLI-005: novo endereço.
    await page.goto('/minha-conta/enderecos');
    await page.getByRole('button', { name: 'Adicionar endereço' }).click();
    await page.locator('#endereco-nome').fill('Trabalho');
    await page.locator('#endereco-tipo').selectOption('TRABALHO');
    await page.locator('#endereco-logradouro-tipo').selectOption('AVENIDA');
    await page.locator('#endereco-logradouro').fill('Avenida Paulista');
    await page.locator('#endereco-numero').fill('1000');
    await page.locator('#endereco-bairro').fill('Bela Vista');
    await page.locator('#endereco-cep').fill('01310100');
    await page.locator('#endereco-cidade').fill('São Paulo');
    await page.locator('#endereco-uf').selectOption('SP');
    await page.getByRole('button', { name: 'Salvar endereço' }).click();
    await expect(page.locator('.item-da-conta', { hasText: 'Trabalho' })).toBeVisible();

    // RF-CLI-006 / RN-CLI-004: cartão vencido recusado; o primeiro válido já é o preferencial.
    await page.goto('/minha-conta/cartoes');
    await page.getByRole('button', { name: 'Adicionar cartão' }).click();
    await page.locator('#cartao-numero').fill('4111 1111 1111 1111');
    await page.locator('#cartao-nome').fill('FERNANDA LIMA');
    await page.locator('#cartao-bandeira').selectOption('1');
    await page.locator('#cartao-validade').fill('2025-01');
    await page.getByRole('button', { name: 'Salvar cartão' }).click();
    await expect(page.getByText('A validade não pode estar vencida.')).toBeVisible();

    await page.locator('#cartao-validade').fill('2030-01');
    await page.getByRole('button', { name: 'Salvar cartão' }).click();
    const cartao = page.locator('.item-da-conta', { hasText: '1111' });
    await expect(cartao).toContainText('Preferencial');

    // RF-CLI-007: troca de senha.
    await page.goto('/minha-conta/senha');
    await page.locator('#senha-atual').fill(NOVA.senha);
    await page.locator('#senha-nova').fill(NOVA.novaSenha);
    await page.locator('#senha-confirmacao').fill(NOVA.novaSenha);
    await page.getByRole('button', { name: 'Alterar senha' }).click();
    await expect(page.getByText('Senha alterada.')).toBeVisible();

    // DEC-10: login por telefone com a nova senha.
    await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
    await entrarComo(page, NOVA.telefone, NOVA.novaSenha);

    // RF-CLI-004: o administrador encontra a cliente pelo CPF.
    await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();
    await entrarComo(page, 'admin@livraria.test', 'Admin@123');
    await page.goto('/admin/clientes');
    await page.getByLabel('CPF', { exact: true }).fill(NOVA.cpf);
    await page.getByRole('button', { name: 'Filtrar' }).click();
    await expect(page.getByText('1 cliente encontrado')).toBeVisible();
    await page.getByRole('link', { name: 'Fernanda Lima Rocha' }).click();
    await expect(page.getByRole('heading', { name: 'Fernanda Lima Rocha' })).toBeVisible();
    await expect(page.getByText('147.258.369-82')).toBeVisible();
    await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();

    // RF-CLI-003 / DEC-10: inativar e entrar de novo reativa a conta.
    await entrarComo(page, NOVA.email, NOVA.novaSenha);
    await inativarConta(page);
    await entrarComo(page, NOVA.email, NOVA.novaSenha);

    // Depois de 7 dias inativa, a conta fica encerrada e o login é recusado como desconhecido.
    await inativarConta(page);
    await ajustarRelogio(new Date(Date.now() + 8 * 24 * 60 * 60 * 1000).toISOString());
    await tentarEntrar(page, NOVA.email, NOVA.novaSenha);
    await expect(page.getByRole('alert')).toHaveText('Login ou senha inválidos.');

    // Com a conta encerrada, o mesmo CPF pode ser cadastrado de novo.
    await cadastrar(page);
  });
});
