import { useEffect, useRef, useState } from 'preact/hooks';
import { iniciais } from '../compartilhado/iniciais';
import type { Usuario } from '../modulos/sessao';
import { IconeSeta } from '../ui/icones';
import { rotuloDoMenuDoUsuario } from './regras/rotuloDoMenuDoUsuario';

const ID_DAS_OPCOES = 'menu-do-usuario-opcoes';

interface Props {
  usuario: Usuario;
  /** A rota atual é a área do cliente (ALT-08): destaca o gatilho que a representa. */
  ativo: boolean;
  aoSair: () => void;
}

/**
 * Gatilho com avatar (iniciais), nome e seta do usuário que abre o menu da conta (ALT-16). "Minha conta" e "Sair"
 * saem da navegação e viram as opções do menu; o administrador não tem a área do cliente, então vê só "Sair".
 */
export function MenuDoUsuario({ usuario, ativo, aoSair }: Props) {
  const [aberto, definirAberto] = useState(false);
  const conteiner = useRef<HTMLDivElement>(null);
  const gatilho = useRef<HTMLButtonElement>(null);
  const painel = useRef<HTMLDivElement>(null);

  function fechar() {
    definirAberto(false);
  }

  function selecionarSair() {
    definirAberto(false);
    aoSair();
  }

  function aoTecladoNoGatilho(evento: KeyboardEvent) {
    if (evento.key === 'ArrowDown' || evento.key === 'ArrowUp') {
      evento.preventDefault();
      definirAberto(true);
    }
  }

  useEffect(() => {
    if (!aberto) {
      return;
    }

    const opcoes = () => Array.from(painel.current?.querySelectorAll<HTMLElement>('[role="menuitem"]') ?? []);
    opcoes()[0]?.focus();

    function aoClicarFora(evento: PointerEvent) {
      if (!conteiner.current?.contains(evento.target as Node)) {
        definirAberto(false);
      }
    }

    function aoTeclado(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        evento.preventDefault();
        definirAberto(false);
        gatilho.current?.focus();
        return;
      }
      if (!['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(evento.key)) {
        return;
      }
      const itens = opcoes();
      if (itens.length === 0) {
        return;
      }
      evento.preventDefault();
      const atual = itens.indexOf(document.activeElement as HTMLElement);
      let proximo: number;
      if (evento.key === 'Home') {
        proximo = 0;
      } else if (evento.key === 'End') {
        proximo = itens.length - 1;
      } else {
        const passo = evento.key === 'ArrowDown' ? 1 : -1;
        proximo = (atual + passo + itens.length) % itens.length;
      }
      itens[proximo]?.focus();
    }

    function aoFocarFora(evento: FocusEvent) {
      if (!conteiner.current?.contains(evento.target as Node)) {
        definirAberto(false);
      }
    }

    document.addEventListener('pointerdown', aoClicarFora);
    document.addEventListener('keydown', aoTeclado);
    document.addEventListener('focusin', aoFocarFora);
    return () => {
      document.removeEventListener('pointerdown', aoClicarFora);
      document.removeEventListener('keydown', aoTeclado);
      document.removeEventListener('focusin', aoFocarFora);
    };
  }, [aberto]);

  return (
    <div class="menu-do-usuario" ref={conteiner}>
      <button
        ref={gatilho}
        type="button"
        class={`menu-do-usuario__gatilho${ativo ? ' navegacao__link--ativo' : ''}`}
        aria-label={rotuloDoMenuDoUsuario(usuario.nome)}
        aria-haspopup="menu"
        aria-expanded={aberto}
        aria-controls={aberto ? ID_DAS_OPCOES : undefined}
        aria-current={ativo ? 'page' : undefined}
        onClick={() => definirAberto((estado) => !estado)}
        onKeyDown={aoTecladoNoGatilho}
      >
        <span class="sessao__usuario" title={usuario.email}>
          <span class="avatar" aria-hidden="true">
            {iniciais(usuario.nome)}
          </span>
          <span class="sessao__nome">{usuario.nome}</span>
        </span>
        <IconeSeta class="menu-do-usuario__seta" />
      </button>
      {aberto && (
        <div class="menu-do-usuario__painel" id={ID_DAS_OPCOES} role="menu" aria-label="Opções da conta" ref={painel}>
          {usuario.papel === 'CLIENTE' && (
            <a class="menu-do-usuario__opcao" role="menuitem" href="/minha-conta" onClick={fechar}>
              Minha conta
            </a>
          )}
          <button type="button" class="menu-do-usuario__opcao" role="menuitem" onClick={selecionarSair}>
            Sair
          </button>
        </div>
      )}
    </div>
  );
}
