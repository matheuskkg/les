import { adicionarLivro, abrirCarrinho, entrarComoCliente, expect, rolarAte, test } from './fixtures';

/**
 * Roteiro de apresentação do carrinho (atividades de venda: RF0031/RF0032/RN0031 e RN0044/RNF0042/RNF0045).
 * No DRS atual: RF-VEN-001/002, RN-VEN-001, RN-VEN-014/015 e RNF-VEN-001.
 *
 * A cliente do seed entra pela API porque o login não é o cenário. Tudo que o roteiro quer provar — inclusão,
 * quantidade, remoção, limite de estoque e expiração — acontece na tela. A reserva expira quando o relógio é
 * ajustado (a tarefa de expiração é disparada por POST /api/teste/relogio no profile e2e).
 */

test('1. cliente adiciona dois livros e vê o carrinho com mais de um item (RF-VEN-001 / RF0031)', async ({ page }) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1); // Dom Casmurro, R$ 47,88
  await adicionarLivro(page, 5); // O Hobbit, R$ 42,90

  const gaveta = await abrirCarrinho(page);
  await expect(gaveta.locator('.item-do-carrinho')).toHaveCount(2);
  await expect(gaveta).toContainText('Dom Casmurro');
  await expect(gaveta).toContainText('O Hobbit');
  await expect(gaveta).toContainText('R$ 90,78');

  await gaveta.getByRole('link', { name: 'Ver carrinho completo' }).click();
  await expect(page.getByRole('heading', { name: 'Carrinho' })).toBeVisible();
  await expect(page.locator('.carrinho__itens .item-do-carrinho')).toHaveCount(2);
  const resumo = page.locator('.carrinho__resumo');
  await rolarAte(resumo);
  await expect(resumo).toContainText('Subtotal (2 itens)');
  await expect(resumo).toContainText('R$ 90,78');
});

test('2. cliente altera a quantidade ao adicionar e na visualização do carrinho (RF-VEN-002 / RF0032)', async ({
  page,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1, 2); // quantidade informada antes de adicionar
  await adicionarLivro(page, 5);

  await page.goto('/carrinho');
  const linha = page.locator('.item-do-carrinho').filter({ hasText: 'Dom Casmurro' });
  const quantidade = linha.getByLabel('Quantidade');
  await quantidade.fill('3'); // quantidade alterada na visualização
  await quantidade.press('Enter');
  await expect(linha).toContainText('R$ 143,64'); // 3 × R$ 47,88

  const resumo = page.locator('.carrinho__resumo');
  await rolarAte(resumo);
  await expect(resumo).toContainText('Subtotal (4 itens)');
  await expect(resumo).toContainText('R$ 186,54'); // 143,64 + 42,90
});

test('3. cliente exclui um item e vê o subtotal recalculado (RF-VEN-001 / RF0031)', async ({ page }) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1);
  await adicionarLivro(page, 5);

  await page.goto('/carrinho');
  await page
    .locator('.item-do-carrinho')
    .filter({ hasText: 'O Hobbit' })
    .getByRole('button', { name: 'Remover' })
    .click();

  await expect(page.locator('.carrinho__itens .item-do-carrinho')).toHaveCount(1);
  const resumo = page.locator('.carrinho__resumo');
  await rolarAte(resumo);
  await expect(resumo).toContainText('R$ 47,88');
});

test('4. quantidade acima do estoque é recusada (RN-VEN-001 / RN0031)', async ({ page }) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 7); // O Silmarillion: 6 unidades no seed

  await page.goto('/carrinho');
  const quantidade = page.locator('.item-do-carrinho').filter({ hasText: 'O Silmarillion' }).getByLabel('Quantidade');
  await quantidade.fill('7');
  await quantidade.press('Enter');

  const alerta = page.getByText(/Estoque insuficiente/);
  await rolarAte(alerta);
  await expect(alerta).toContainText('Estoque insuficiente para o livro 7. Disponível: 6.');
  await expect(page.locator('.carrinho__itens .item-do-carrinho')).toHaveCount(1);
});

test('5. expirado o bloqueio, os itens saem do carrinho e voltam por "Adicionar de novo" (RN-VEN-014/015, RNF-VEN-001 / RN0044, RNF0042, RNF0045)', async ({
  page,
  ajustarRelogio,
}) => {
  await entrarComoCliente(page);
  await adicionarLivro(page, 1, 2); // duas unidades do mesmo produto: as duas saem juntas (RN-VEN-015)
  await page.goto('/carrinho');
  await expect(page.locator('.carrinho__itens .item-do-carrinho')).toHaveCount(1);

  // Prazo de bloqueio padrão: 30 minutos. O ajuste do relógio dispara a tarefa de expiração.
  await ajustarRelogio(new Date(Date.now() + 31 * 60_000).toISOString());

  const removidos = page.getByText('Removidos por expiração');
  await rolarAte(removidos);
  await expect(removidos).toBeVisible();
  const removido = page.locator('.item-removido').filter({ hasText: 'Dom Casmurro' });
  await expect(removido).toContainText('2 unidades');
  await expect(removido.getByRole('button', { name: 'Comprar' })).toBeDisabled();
  await expect(page.locator('.carrinho__itens .item-do-carrinho')).toHaveCount(0);

  await removido.getByRole('button', { name: 'Adicionar de novo' }).click();
  await expect(page.locator('.item-removido')).toHaveCount(0);
  await expect(page.locator('.carrinho__itens .item-do-carrinho')).toContainText('Dom Casmurro');
});
