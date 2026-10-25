import { useEffect, useRef, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import { useCarregamento } from '../../../compartilhado/useCarregamento';
import { Botao } from '../../../ui/Botao';
import { BotoesDeAvaliacao } from '../../../ui/BotoesDeAvaliacao';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import {
  SUGESTOES,
  TAMANHO_MAXIMO,
  aplicarAvaliacao,
  contarCaracteres,
  enviaAoTeclar,
  mensagemProvisoria,
  podeEnviar,
  type AvaliacaoDaResposta,
  type Mensagem,
} from '../regras/conversa';
import { CabecalhoDoPainel } from './CabecalhoDoPainel';
import { LivroCitado } from './LivroCitado';

const FALHA_DE_CONEXAO: ProblemaDetalhe = {
  status: 0,
  detail: 'Não foi possível conectar ao servidor. Tente novamente.',
};

/**
 * Conversa do cliente com o assistente (RNF-REC-001, DEC-03): carrega a conversa ativa, mostra a mensagem do cliente
 * na hora e um indicador de "digitando" até a resposta, com uma requisição por mensagem. O 503 oferece "tentar de
 * novo" com o mesmo texto; o 422 mostra as violações e devolve o texto ao campo.
 */
export function PainelDoAssistente({ tituloId, aoFechar }: { tituloId: string; aoFechar: () => void }) {
  const conversa = useCarregamento((signal) => cliente.GET('/api/me/assistente/conversa', { signal }), []);
  const [mensagens, definirMensagens] = useState<Mensagem[]>([]);
  const [texto, definirTexto] = useState('');
  const [aguardando, definirAguardando] = useState(false);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [pendente, definirPendente] = useState<string | null>(null);
  const [anuncio, definirAnuncio] = useState('');
  const [avaliando, definirAvaliando] = useState<ReadonlySet<number>>(new Set());
  const provisorias = useRef(0);
  // Trava síncrona: dois cliques no mesmo quadro não podem enviar duas vezes.
  const enviando = useRef(false);
  const carregada = useRef(false);
  const lista = useRef<HTMLOListElement>(null);
  const campo = useRef<HTMLTextAreaElement>(null);

  // A conversa do server preenche a tela uma vez; depois, só o painel a altera, e uma falha não apaga nada.
  useEffect(() => {
    if (conversa.dados && !carregada.current) {
      carregada.current = true;
      definirMensagens(conversa.dados);
    }
  }, [conversa.dados]);

  useEffect(() => {
    lista.current?.lastElementChild?.scrollIntoView({ block: 'end' });
  }, [mensagens, aguardando]);

  useEffect(() => campo.current?.focus(), []);

  async function enviar(mensagem: string, repetindo: boolean) {
    if (enviando.current || !podeEnviar(mensagem, aguardando)) {
      return;
    }
    enviando.current = true;
    definirAguardando(true);
    definirProblema(null);
    definirPendente(null);
    let provisoria: Mensagem | null = null;
    if (!repetindo) {
      provisorias.current += 1;
      const nova = mensagemProvisoria(mensagem, provisorias.current, new Date());
      provisoria = nova;
      definirMensagens((atuais) => [...atuais, nova]);
      definirTexto('');
    }
    try {
      const { data, error, response } = await cliente.POST('/api/me/assistente/mensagens', {
        body: { texto: mensagem.trim() },
      });
      if (response.ok && data) {
        definirMensagens((atuais) => [...atuais, data]);
        definirAnuncio(data.texto);
      } else {
        falhar(interpretarProblema(error, response.status), mensagem, provisoria);
      }
    } catch {
      falhar(FALHA_DE_CONEXAO, mensagem, provisoria);
    } finally {
      enviando.current = false;
      definirAguardando(false);
    }
  }

  function falhar(falha: ProblemaDetalhe, mensagem: string, provisoria: Mensagem | null) {
    definirProblema(falha);
    if (falha.status === 422 && provisoria !== null) {
      // Mensagem nova recusada antes da chamada: nada foi gravado, então ela sai da conversa e volta ao campo.
      definirMensagens((atuais) => atuais.filter((atual) => atual !== provisoria));
      definirTexto(mensagem);
    } else {
      // A mensagem pode ter ficado gravada (503) ou não (sem conexão). O server não grava de novo o reenvio da
      // última mensagem sem resposta, então "tentar de novo" é seguro nos dois casos.
      definirPendente(mensagem);
    }
  }

  async function avaliar(mensagem: Mensagem, avaliacao: AvaliacaoDaResposta) {
    definirAvaliando((atuais) => new Set(atuais).add(mensagem.id));
    try {
      const { response } = await cliente.PUT('/api/me/assistente/mensagens/{id}/avaliacao', {
        params: { path: { id: mensagem.id } },
        body: { avaliacao },
      });
      if (response.ok) {
        definirMensagens((atuais) => aplicarAvaliacao(atuais, mensagem.id, avaliacao));
      }
    } catch {
      // Sem conexão: a avaliação não muda, e o cliente pode tentar de novo.
    } finally {
      definirAvaliando((atuais) => {
        const restantes = new Set(atuais);
        restantes.delete(mensagem.id);
        return restantes;
      });
    }
  }

  async function limpar() {
    try {
      const { response } = await cliente.DELETE('/api/me/assistente/conversa');
      if (response.ok) {
        definirMensagens([]);
        definirProblema(null);
        definirPendente(null);
      } else {
        definirProblema(interpretarProblema(undefined, response.status));
      }
    } catch {
      definirProblema(FALHA_DE_CONEXAO);
    }
  }

  const caracteres = contarCaracteres(texto);
  const carregandoConversa = conversa.carregando && conversa.dados === null;

  return (
    <>
      <CabecalhoDoPainel tituloId={tituloId} aoFechar={aoFechar}>
        {mensagens.length > 0 && (
          <Botao variante="discreto" disabled={aguardando} onClick={() => void limpar()}>
            Limpar conversa
          </Botao>
        )}
      </CabecalhoDoPainel>
      <div class="assistente__corpo">
        <Erro problema={conversa.problema} />
        {carregandoConversa ? (
          <p class="aviso" role="status">
            Carregando a conversa…
          </p>
        ) : mensagens.length === 0 && !aguardando ? (
          <div class="assistente__sugestoes">
            <p>Pergunte sobre livros, autores e gêneros da loja. Por exemplo:</p>
            {SUGESTOES.map((sugestao) => (
              <Botao key={sugestao} variante="secundario" onClick={() => void enviar(sugestao, false)}>
                {sugestao}
              </Botao>
            ))}
          </div>
        ) : (
          <ol class="assistente__mensagens" ref={lista} aria-labelledby={tituloId}>
            {mensagens.map((mensagem) => (
              <li
                key={mensagem.id}
                class={`assistente__mensagem assistente__mensagem--${mensagem.papel === 'CLIENTE' ? 'cliente' : 'assistente'}`}
              >
                <span class="visualmente-oculto">{mensagem.papel === 'CLIENTE' ? 'Você:' : 'Assistente:'}</span>
                <p class="assistente__texto">{mensagem.texto}</p>
                {mensagem.livros.length > 0 && (
                  <ul class="assistente__livros">
                    {mensagem.livros.map((livro) => (
                      <li key={livro.id}>
                        <LivroCitado livro={livro} />
                      </li>
                    ))}
                  </ul>
                )}
                {mensagem.papel === 'ASSISTENTE' && (
                  <BotoesDeAvaliacao
                    avaliacao={mensagem.avaliacao}
                    sobre="esta resposta"
                    desabilitado={avaliando.has(mensagem.id)}
                    aoAvaliar={(avaliacao) => void avaliar(mensagem, avaliacao)}
                  />
                )}
              </li>
            ))}
            {aguardando && (
              <li class="assistente__mensagem assistente__mensagem--assistente assistente__digitando" role="status">
                <span>Digitando…</span>
              </li>
            )}
          </ol>
        )}
        <p class="visualmente-oculto" aria-live="polite">
          {anuncio}
        </p>
      </div>

      {problema && (
        <div class="assistente__problema">
          {problema.violacoes && problema.violacoes.length > 0 ? (
            <Violacoes violacoes={problema.violacoes} />
          ) : (
            <Erro problema={problema} />
          )}
          {pendente !== null && (
            <Botao variante="secundario" disabled={aguardando} onClick={() => void enviar(pendente, true)}>
              Tentar de novo
            </Botao>
          )}
        </div>
      )}

      <form
        class="assistente__envio"
        onSubmit={(evento) => {
          evento.preventDefault();
          void enviar(texto, false);
        }}
      >
        <label class="visualmente-oculto" for="mensagem-ao-assistente">
          Mensagem ao assistente
        </label>
        <textarea
          id="mensagem-ao-assistente"
          ref={campo}
          rows={2}
          value={texto}
          placeholder="Escreva sua pergunta…"
          aria-describedby="contador-do-assistente"
          onInput={(evento) => definirTexto(evento.currentTarget.value)}
          onKeyDown={(evento) => {
            if (enviaAoTeclar(evento)) {
              evento.preventDefault();
              if (!carregandoConversa) {
                void enviar(texto, false);
              }
            }
          }}
        />
        <div class="assistente__acoes-do-envio">
          <span
            id="contador-do-assistente"
            class={`assistente__contador${caracteres > TAMANHO_MAXIMO ? ' assistente__contador--excedido' : ''}`}
          >
            {caracteres}/{TAMANHO_MAXIMO}
          </span>
          <Botao type="submit" disabled={carregandoConversa || !podeEnviar(texto, aguardando)}>
            {aguardando ? 'Aguardando…' : 'Enviar'}
          </Botao>
        </div>
      </form>
    </>
  );
}
