import { useEffect, useRef, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { BotaoLink } from '../../../ui/Botao';
import { IconeBalao } from '../../../ui/icones';
import { useSessao } from '../../sessao';
import { assistenteNaRota, lerAberto, urlDoConvite } from '../regras/conversa';
import { CabecalhoDoPainel } from './CabecalhoDoPainel';
import { PainelDoAssistente } from './PainelDoAssistente';

const CHAVE_DO_ESTADO = 'assistente-aberto';
const TITULO = 'titulo-do-assistente';

function lerEstadoGuardado(): boolean {
  try {
    return lerAberto(sessionStorage.getItem(CHAVE_DO_ESTADO));
  } catch {
    return false;
  }
}

function guardarEstado(aberto: boolean) {
  try {
    sessionStorage.setItem(CHAVE_DO_ESTADO, aberto ? '1' : '0');
  } catch {
    // Sem armazenamento da aba, o painel só não lembra o estado ao recarregar.
  }
}

/**
 * Widget do assistente (RNF-REC-001, DEC-03): botão flutuante no canto inferior direito, em toda a loja e fora das
 * telas do administrador, que abre um painel não modal. Aberto ou fechado, o estado segue na navegação e fica
 * guardado na aba. O visitante recebe o convite para entrar e volta à mesma página depois do login. Esc fecha o
 * painel e devolve o foco ao botão.
 */
export function Assistente() {
  const { usuario, carregando } = useSessao();
  const { path, url } = useLocation();
  const [aberto, definirAberto] = useState(lerEstadoGuardado);
  const botao = useRef<HTMLButtonElement>(null);
  const painel = useRef<HTMLDivElement>(null);

  function fechar() {
    definirAberto(false);
    // O botão volta a existir no próximo render; o foco vai para ele em seguida.
    requestAnimationFrame(() => botao.current?.focus());
  }

  useEffect(() => guardarEstado(aberto), [aberto]);

  // Esc fecha com o foco no painel ou solto na página (um botão desabilitado durante a ação solta o foco).
  useEffect(() => {
    if (!aberto) {
      return;
    }
    const aoTeclar = (evento: KeyboardEvent) => {
      const foco = document.activeElement;
      // Foco solto só conta quando não há outro painel modal aberto (a gaveta do carrinho trata o próprio Esc).
      const focoSolto = (foco === document.body || foco === null) && !document.querySelector('[aria-modal="true"]');
      const noPainel = focoSolto || painel.current?.contains(foco) === true;
      if (evento.key === 'Escape' && noPainel) {
        fechar();
      }
    };
    document.addEventListener('keydown', aoTeclar);
    return () => document.removeEventListener('keydown', aoTeclar);
  }, [aberto]);

  // Para o cliente, o painel leva o foco ao campo da mensagem; para o visitante, ao próprio painel.
  useEffect(() => {
    if (aberto && usuario === null) {
      painel.current?.focus();
    }
  }, [aberto, usuario]);

  if (carregando || usuario?.papel === 'ADMIN' || !assistenteNaRota(path)) {
    return null;
  }

  if (!aberto) {
    return (
      <button
        type="button"
        class="assistente__botao"
        ref={botao}
        aria-label="Abrir o assistente da livraria"
        onClick={() => definirAberto(true)}
      >
        <IconeBalao tamanho="1.35rem" />
        <span class="assistente__botao-rotulo">Assistente</span>
      </button>
    );
  }

  return (
    <div
      class="assistente__painel"
      ref={painel}
      tabIndex={-1}
      role="dialog"
      aria-modal="false"
      aria-labelledby={TITULO}
    >
      {usuario === null ? (
        <>
          <CabecalhoDoPainel tituloId={TITULO} aoFechar={fechar} />
          <div class="assistente__corpo assistente__convite">
            <p>Entre na sua conta para conversar com o assistente e receber indicações de livros.</p>
            <BotaoLink href={urlDoConvite(path, url)}>Entrar</BotaoLink>
          </div>
        </>
      ) : (
        <PainelDoAssistente tituloId={TITULO} aoFechar={fechar} />
      )}
    </div>
  );
}
