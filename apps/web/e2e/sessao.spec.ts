import { entrarComo, expect, test } from './fixtures';

const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };

test.describe('Sessão', () => {
  test('administrador entra e o cabeçalho mostra a sessão', async ({ page }) => {
    await entrarComo(page, ADMIN.login, ADMIN.senha);

    const cabecalho = page.getByRole('banner');
    await expect(cabecalho.locator('.sessao__nome')).toHaveText('Administração');
    await expect(cabecalho.getByRole('link', { name: 'Administração' })).toBeVisible();
    await expect(cabecalho.getByRole('button', { name: 'Sair' })).toBeVisible();
  });

  test('sair volta ao cabeçalho anônimo', async ({ page }) => {
    await entrarComo(page, ADMIN.login, ADMIN.senha);

    await page.getByRole('banner').getByRole('button', { name: 'Sair' }).click();

    const cabecalho = page.getByRole('banner');
    await expect(cabecalho.getByRole('link', { name: 'Entrar', exact: true })).toBeVisible();
    await expect(cabecalho.getByRole('link', { name: 'Criar conta' })).toBeVisible();
    await expect(cabecalho.locator('.sessao__nome')).toHaveCount(0);
  });

  test('senha errada mostra mensagem de credenciais inválidas', async ({ page }) => {
    await page.goto('/login');

    await expect(page.getByLabel('E-mail ou telefone')).toHaveAccessibleDescription(
      'Você pode entrar com o e-mail ou o telefone do cadastro.',
    );
    await page.getByLabel('E-mail ou telefone').fill(ADMIN.login);
    await page.getByLabel('Senha').fill('senha-errada');
    await page.getByRole('button', { name: 'Entrar' }).click();

    await expect(page.getByRole('alert')).toHaveText('Login ou senha inválidos.');
    await expect(
      page.getByRole('banner').getByRole('link', { name: 'Entrar', exact: true }),
    ).toBeVisible();
  });
});
