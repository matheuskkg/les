/**
 * Clique no rótulo de uma seleção: além de focá-la, abre as opções, o que o navegador não faz sozinho. Sem suporte a
 * `showPicker` (ou sem o gesto do usuário), fica só o foco.
 */
export function abrirOpcoesPeloRotulo(evento: MouseEvent, id: string) {
  const selecao = document.getElementById(id);
  if (!(selecao instanceof HTMLSelectElement) || selecao.disabled) {
    return;
  }
  evento.preventDefault();
  selecao.focus();
  try {
    selecao.showPicker();
  } catch {
    // Mantém o foco dado acima.
  }
}
