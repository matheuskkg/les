import type { Page } from '@playwright/test';
import { entrarComo, expect, sair, test } from './fixtures';

// Jornada do assistente com o provedor fake, sem rede (RNF-REC-001, DEC-03). O profile e2e deixa os provedores reais
// sem chave, então só o fake atende: ele busca no catálogo pelas palavras da mensagem e trata "futebol" como fora do
// escopo, e o server troca a resposta pelo texto fixo.
const ANA = { login: 'ana.souza@livraria.test', senha: 'Senha@123' };
const ADMIN = { login: 'admin@livraria.test', senha: 'Admin@123' };

function painel(page: Page) {
  return page.getByRole('dialog', { name: 'Assistente da livraria' });
}

async function abrirAssistente(page: Page) {
  await page.getByRole('button', { name: 'Abrir o assistente da livraria' }).click();
  await expect(painel(page)).toBeVisible();
}

/** Mensagens da conversa; o anúncio em aria-live repete o texto da última resposta fora da lista. */
function mensagens(page: Page) {
  return painel(page).getByRole('list', { name: 'Assistente da livraria' });
}

async function perguntar(page: Page, texto: string) {
  await painel(page).getByLabel('Mensagem ao assistente').fill(texto);
  await painel(page).getByRole('button', { name: 'Enviar' }).click();
  await expect(painel(page).getByText('Digitando…')).toHaveCount(0);
}

test.describe('Jornada do assistente', () => {
  test('RNF-REC-001: item recomendado avaliado negativamente some da vitrine', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/');

    const recomendados = page.getByRole('region', { name: 'Recomendados para você' });
    await expect(recomendados).toBeVisible();
    const primeiro = recomendados.getByRole('heading', { level: 3 }).first();
    const titulo = (await primeiro.textContent())?.trim() ?? '';
    expect(titulo).not.toBe('');

    await recomendados.getByRole('button', { name: `Não gostei de ${titulo}` }).click();

    await expect(recomendados.getByRole('heading', { name: titulo, exact: true })).toHaveCount(0);
    await page.reload();
    await expect(page.getByRole('region', { name: 'Recomendados para você' })
      .getByRole('heading', { name: titulo, exact: true })).toHaveCount(0);
  });

  test('RNF-REC-001: visitante entra pelo widget, conversa e põe um livro citado no carrinho', async ({ page }) => {
    await page.goto('/livros/1');
    await abrirAssistente(page);
    await painel(page).getByRole('link', { name: 'Entrar' }).click();

    await expect(page).toHaveURL(/\/login\?destino=/);
    await page.getByLabel('E-mail ou telefone').fill(ANA.login);
    await page.getByLabel('Senha').fill(ANA.senha);
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page).toHaveURL(/\/livros\/1$/);

    // O painel continua aberto na volta: o estado fica guardado na aba.
    await expect(painel(page)).toBeVisible();
    await perguntar(page, 'Quero o Hobbit');

    await expect(mensagens(page).getByText('Encontrei estes livros para você.')).toBeVisible();
    await painel(page).getByRole('button', { name: 'Adicionar O Hobbit ao carrinho' }).click();
    await expect(page.getByText('Adicionado ao carrinho.')).toBeVisible();

    await page.goto('/carrinho');
    await expect(page.getByRole('main').getByText('O Hobbit').first()).toBeVisible();
  });

  test('RNF-REC-001: conversa retomada depois de sair e entrar; limpar começa outra', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await abrirAssistente(page);
    await perguntar(page, 'Tolkien');
    await expect(mensagens(page).getByText('Tolkien', { exact: true })).toBeVisible();

    await sair(page);
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/');
    // O painel aberto fica guardado na aba; se a troca de sessão o fechou, abre de novo.
    const abrir = page.getByRole('button', { name: 'Abrir o assistente da livraria' });
    await expect(painel(page).or(abrir)).toBeVisible();
    if (await abrir.isVisible()) {
      await abrirAssistente(page);
    }
    await expect(mensagens(page).getByText('Tolkien', { exact: true })).toBeVisible();
    await expect(mensagens(page).getByText('Encontrei estes livros para você.')).toBeVisible();

    await painel(page).getByRole('button', { name: 'Limpar conversa' }).click();

    await expect(painel(page).getByText('Tolkien', { exact: true })).toHaveCount(0);
    await expect(painel(page).getByText('Pergunte sobre livros, autores e gêneros da loja.', { exact: false }))
      .toBeVisible();
  });

  test('RNF-REC-001: bloqueia mensagem acima de 500 caracteres e recusa assunto fora do escopo', async ({ page }) => {
    await entrarComo(page, ANA.login, ANA.senha);
    await abrirAssistente(page);

    await painel(page).getByLabel('Mensagem ao assistente').fill('a'.repeat(501));
    await expect(painel(page).getByText('501/500')).toBeVisible();
    await expect(painel(page).getByRole('button', { name: 'Enviar' })).toBeDisabled();

    await perguntar(page, 'Quem ganhou o jogo de futebol ontem?');
    await expect(mensagens(page).getByText('Posso ajudar só com livros e compras da livraria.', { exact: false })).toBeVisible();
  });

  test('RNF-REC-001: admin reordena e desativa provedores e vê as chamadas nas métricas', async ({ page }) => {
    // Gera chamadas ao fake: a recomendação da vitrine e uma mensagem do chat.
    await entrarComo(page, ANA.login, ANA.senha);
    await page.goto('/');
    await expect(page.getByRole('region', { name: 'Recomendados para você' })).toBeVisible();
    await abrirAssistente(page);
    await perguntar(page, 'Hobbit');
    await painel(page).getByRole('button', { name: 'Fechar' }).click();
    await sair(page);

    await entrarComo(page, ADMIN.login, ADMIN.senha);
    await page.goto('/admin/assistente');

    const cadeia = page.getByRole('table', { name: 'Cadeia de provedores de IA' });
    for (let vez = 0; vez < 3; vez++) {
      await cadeia.getByRole('button', { name: 'Subir fake' }).click();
    }
    await cadeia.getByRole('checkbox', { name: 'Ativo: gemini' }).uncheck();
    await page.getByRole('button', { name: 'Salvar cadeia' }).click();
    await expect(page.getByText('Cadeia salva')).toBeVisible();

    await page.reload();
    const linhas = page.getByRole('table', { name: 'Cadeia de provedores de IA' }).getByRole('row');
    await expect(linhas.nth(1)).toContainText('fake');
    await expect(page.getByRole('checkbox', { name: 'Ativo: gemini' })).not.toBeChecked();

    const fake = page.getByRole('table', { name: 'Métricas por provedor' }).getByRole('row')
      .filter({ hasText: /^fake/ });
    await expect(fake.getByRole('cell').nth(1)).toHaveText('2');
    // O fake não faz chamadas HTTP, então a coluna de chamadas por mensagem fica vazia.
    await expect(fake.getByRole('cell').nth(2)).toHaveText('—');
    await expect(fake.getByRole('cell').nth(3)).toHaveText('100%');
  });
});
