import { entrarComo, expect, test } from './fixtures';

const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

test.describe('Minha conta', () => {
  test('cliente altera o nome nos dados cadastrais', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/minha-conta/dados');

    const nome = page.getByLabel('Nome', { exact: false }).first();
    await expect(nome).toHaveValue('Ana Souza');
    await nome.fill('Ana Souza Lima');
    await page.getByRole('button', { name: 'Salvar alterações' }).click();

    await expect(page.getByText('Dados atualizados.')).toBeVisible();
    await page.reload();
    await expect(page.getByLabel('Nome', { exact: false }).first()).toHaveValue('Ana Souza Lima');
  });

  test('cliente troca a senha e continua logado', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/minha-conta/senha');

    await page.locator('#senha-atual').fill(ANA.senha);
    await page.locator('#senha-nova').fill('Nova@Senha9');
    await page.locator('#senha-confirmacao').fill('Nova@Senha9');
    await page.getByRole('button', { name: 'Alterar senha' }).click();

    await expect(page.getByText('Senha alterada.')).toBeVisible();
    await expect(page.getByRole('banner').getByRole('button', { name: 'Sair' })).toBeVisible();
  });

  test('cliente inativa a conta e volta à vitrine sem sessão', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/minha-conta/dados');

    await page.getByRole('button', { name: 'Inativar minha conta' }).click();
    await page.getByRole('button', { name: 'Confirmar inativação' }).click();

    await expect(page).toHaveURL(/\/$/);
    await expect(page.getByRole('banner').getByRole('link', { name: 'Entrar' })).toBeVisible();

    await entrarComo(page, ANA.login, ANA.senha);
  });
});
