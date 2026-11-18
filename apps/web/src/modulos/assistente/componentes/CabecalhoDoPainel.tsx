import type { ComponentChildren } from 'preact';
import { Botao } from '../../../ui/Botao';
import { IconeFechar } from '../../../ui/icones';

interface Props {
  tituloId: string;
  aoFechar: () => void;
  /** Ações extras ao lado de "Fechar", como "Limpar conversa". */
  children?: ComponentChildren;
}

/** Título do painel do assistente, que nomeia o `dialog`, e as ações do cabeçalho. */
export function CabecalhoDoPainel({ tituloId, aoFechar, children }: Props) {
  return (
    <div class="assistente__topo">
      <h2 id={tituloId}>Assistente da livraria</h2>
      <div class="assistente__acoes">
        {children}
        <Botao variante="discreto" aria-label="Fechar" onClick={aoFechar}>
          <IconeFechar tamanho="1.25rem" />
        </Botao>
      </div>
    </div>
  );
}
