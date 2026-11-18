/** Itens da navegação principal do cabeçalho (ALT-08). */
export type ItemDaNavbar = 'vitrine' | 'carrinho' | 'minha-conta' | 'administracao' | 'notificacoes';

/**
 * Item da navegação principal que representa a rota atual, ou `null` quando a rota não tem item correspondente
 * (login, cadastro, página não encontrada etc.).
 *
 * A página de notificações vence a área do cliente/administração na precedência: ela fica dentro desses prefixos
 * (`/minha-conta/notificacoes` e `/admin/notificacoes`), mas é um item próprio da navegação (ALT-08).
 */
export function itemAtivoDaNavbar(path: string, caminhoDasNotificacoes: string): ItemDaNavbar | null {
  if (caminhoDasNotificacoes !== '' && path === caminhoDasNotificacoes) {
    return 'notificacoes';
  }
  if (path === '/' || path === '/livros' || path.startsWith('/livros/')) {
    return 'vitrine';
  }
  if (path === '/carrinho' || path === '/checkout') {
    return 'carrinho';
  }
  if (path === '/minha-conta' || path.startsWith('/minha-conta/')) {
    return 'minha-conta';
  }
  if (path === '/admin' || path.startsWith('/admin/')) {
    return 'administracao';
  }
  return null;
}
