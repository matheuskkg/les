import type { Page } from '@playwright/test';
import { adicionarDomCasmurro, entrarComo, expect, test } from './fixtures';

// Prazos do VEN-02 e VEN-03: carrinho de 30 min com aviso 5 min antes; checkout de 60 min (DEC-12).
const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };

function daquiA(minutos: number): string {
  return new Date(Date.now() + minutos * 60_000).toISOString();
}

function contador(page: Page, naoLidas: number) {
  const nome = naoLidas > 0 ? `Notificações: ${naoLidas} não lidas` : 'Notificações';
  return page.getByRole('banner').getByRole('link', { name: nome, exact: true });
}

test.describe('Expiração e notificações com relógio controlado (E2E-05)', () => {
  test('carrinho avisa, expira por SSE sem recarregar e os avisos são marcados como lidos (RN-VEN-014, DEC-13)', async ({
    page,
    ajustarRelogio,
  }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await adicionarDomCasmurro(page);
    await page.goto('/carrinho');
    await expect(contador(page, 0)).toBeVisible();

    // CARRINHO_EXPIRANDO: o contador sobe pelo stream, sem recarregar a página (DEC-13).
    await ajustarRelogio(daquiA(26));
    await expect(contador(page, 1)).toBeVisible();
    await expect(page.locator('.item-da-conta:not(.item-removido)', { hasText: 'Dom Casmurro' })).toBeVisible();

    // CARRINHO_EXPIRADO: os itens vão para os removidos e podem ser adicionados de novo (RN-VEN-014).
    await ajustarRelogio(daquiA(31));
    await expect(contador(page, 2)).toBeVisible();
    const removido = page.locator('.item-removido', { hasText: 'Dom Casmurro' });
    await expect(removido).toBeVisible();
    await removido.getByRole('button', { name: 'Adicionar de novo' }).click();
    await expect(page.locator('.item-da-conta:not(.item-removido)', { hasText: 'Dom Casmurro' })).toBeVisible();

    await contador(page, 2).click();
    await expect(page.getByText(/está perto de expirar/)).toBeVisible();
    await expect(page.getByText(/os itens do seu carrinho foram removidos/)).toBeVisible();
    await page.getByRole('button', { name: 'Marcar como lida' }).first().click();
    await expect(contador(page, 1)).toBeVisible();
    await page.getByRole('button', { name: 'Marcar todas como lidas' }).click();
    await expect(contador(page, 0)).toBeVisible();
  });

  test('checkout vencido leva os itens aos removidos e notifica CHECKOUT_EXPIRADO (RN-VEN-014, RNF-VEN-001)', async ({
    page,
    ajustarRelogio,
  }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await adicionarDomCasmurro(page);
    await page.goto('/carrinho');
    await page.getByRole('button', { name: 'Ir para pagamento' }).click();
    await expect(page).toHaveURL(/\/checkout$/);
    await expect(page.locator('.item-da-conta', { hasText: 'Dom Casmurro' })).toBeVisible();

    await ajustarRelogio(daquiA(61));

    await expect(page.getByText('Nenhuma compra em andamento.')).toBeVisible();
    await page.getByRole('link', { name: 'Ir para o carrinho' }).click();
    await expect(page.locator('.item-removido', { hasText: 'Dom Casmurro' })).toBeVisible();

    await page.getByRole('banner').getByRole('link', { name: /Notificações/ }).click();
    await expect(page.getByText(/O prazo para pagar acabou/)).toBeVisible();
  });
});
