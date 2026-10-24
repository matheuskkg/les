import { expect, test as base, type Page } from '@playwright/test';

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

/** Entra pela tela /login e espera o cabeçalho mostrar a sessão. */
export async function entrarComo(page: Page, login: string, senha: string): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('E-mail ou telefone').fill(login);
  await page.getByLabel('Senha').fill(senha);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page.getByRole('banner').getByRole('button', { name: 'Sair' })).toBeVisible();
}
