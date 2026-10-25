import { useEffect, useRef, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import type { components } from '../../../api/esquema';
import { formatarDataHora } from '../../../compartilhado/formatacao';
import { Botao } from '../../../ui/Botao';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import { cadeiaAlterada, corpoDaCadeia, moverProvedor, rotuloDoResultado } from '../regras/assistenteDoAdmin';

type ProvedorIa = components['schemas']['ProvedorIaDto'];

interface Props {
  gravada: ProvedorIa[];
  aoSalvar: (gravada: ProvedorIa[]) => void;
}

/**
 * Cadeia de provedores de IA (RNF-REC-001, DEC-03): a ordem de preferência, a ativação, se há chave configurada e a
 * última falha. "Subir" e "Descer" reordenam e a caixa "Ativo" tira o provedor da cadeia; nada muda até "Salvar
 * cadeia". Um provedor ativo sem chave fica fora da cadeia até a chave ser configurada no server.
 */
export function CadeiaDeProvedoresIa({ gravada, aoSalvar }: Props) {
  const [editada, definirEditada] = useState<ProvedorIa[]>(gravada);
  const [salvando, definirSalvando] = useState(false);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [salva, definirSalva] = useState(false);
  const focoPendente = useRef<string | null>(null);

  useEffect(() => definirEditada(gravada), [gravada]);

  // Mover a linha recria a ordem no DOM; o foco volta ao botão usado, ou ao outro quando ele fica desabilitado.
  useEffect(() => {
    const pendente = focoPendente.current;
    focoPendente.current = null;
    if (pendente === null) {
      return;
    }
    const [acao, id] = pendente.split(':');
    const botao = document.getElementById(`provedor-${acao}-${id}`) as HTMLButtonElement | null;
    const outro = document.getElementById(`provedor-${acao === 'subir' ? 'descer' : 'subir'}-${id}`);
    (botao && !botao.disabled ? botao : outro)?.focus();
  }, [editada]);

  const alterada = cadeiaAlterada(gravada, editada);

  /** Procura a posição pelo id na lista atual, para dois cliques seguidos não usarem a posição do render anterior. */
  function mover(id: number, deslocamento: -1 | 1) {
    focoPendente.current = `${deslocamento < 0 ? 'subir' : 'descer'}:${id}`;
    definirSalva(false);
    definirEditada((atual) =>
      moverProvedor(atual, atual.findIndex((provedor) => provedor.id === id), deslocamento),
    );
  }

  function alternar(id: number) {
    definirSalva(false);
    definirEditada((atual) => atual.map((provedor) => (provedor.id === id ? { ...provedor, ativo: !provedor.ativo } : provedor)));
  }

  async function salvar() {
    definirSalvando(true);
    definirProblema(null);
    definirSalva(false);
    try {
      const { data, error, response } = await cliente.PUT('/api/admin/ia/provedores', { body: corpoDaCadeia(editada) });
      if (!response.ok || !data) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirSalva(true);
      aoSalvar(data);
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível salvar a cadeia. Tente novamente.' });
    } finally {
      definirSalvando(false);
    }
  }

  return (
    <div class="cartao">
      <h2>Cadeia de provedores</h2>
      <p class="pagina__introducao">
        O primeiro provedor ativo, com chave e fora da espera de cota atende; se falhar, a cadeia passa ao próximo.
      </p>
      <div class="tabela-rolavel">
        <table class="tabela" aria-label="Cadeia de provedores de IA">
          <thead>
            <tr>
              <th scope="col" class="tabela__numero">Ordem</th>
              <th scope="col">Provedor</th>
              <th scope="col">Chave</th>
              <th scope="col">Última falha</th>
              <th scope="col">Ativo</th>
              <th scope="col">Mover</th>
            </tr>
          </thead>
          <tbody>
            {editada.map((provedor, indice) => (
              <tr key={provedor.id}>
                <td class="tabela__numero">{indice + 1}</td>
                <td>
                  <strong>{provedor.nome}</strong>
                  {provedor.modelo && <span class="cadeia__modelo">{provedor.modelo}</span>}
                </td>
                <td>{provedor.chaveConfigurada ? 'Configurada' : 'Sem chave'}</td>
                <td>
                  {provedor.ultimaFalha
                    ? `${rotuloDoResultado(provedor.ultimaFalha.resultado)}${
                        provedor.ultimaFalha.codigoHttp ? ` (${provedor.ultimaFalha.codigoHttp})` : ''
                      } em ${formatarDataHora(provedor.ultimaFalha.em)}`
                    : '—'}
                </td>
                <td>
                  <input
                    type="checkbox"
                    class="cadeia__ativo"
                    checked={provedor.ativo}
                    aria-label={`Ativo: ${provedor.nome}`}
                    onChange={() => alternar(provedor.id)}
                  />
                </td>
                <td>
                  <div class="tabela__acoes">
                    <Botao id={`provedor-subir-${provedor.id}`} variante="discreto" disabled={indice === 0}
                      aria-label={`Subir ${provedor.nome}`} onClick={() => mover(provedor.id, -1)}>
                      Subir
                    </Botao>
                    <Botao id={`provedor-descer-${provedor.id}`} variante="discreto"
                      disabled={indice === editada.length - 1}
                      aria-label={`Descer ${provedor.nome}`} onClick={() => mover(provedor.id, 1)}>
                      Descer
                    </Botao>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <Violacoes violacoes={problema?.violacoes} />
      {!problema?.violacoes?.length && <Erro problema={problema} />}
      {salva && (
        <p class="aviso aviso--sucesso" role="status">
          Cadeia salva; a próxima chamada já usa a nova ordem.
        </p>
      )}
      <div class="acoes">
        <Botao disabled={!alterada || salvando} onClick={salvar}>
          {salvando ? 'Salvando…' : 'Salvar cadeia'}
        </Botao>
        <Botao variante="secundario" disabled={!alterada || salvando} onClick={() => definirEditada(gravada)}>
          Desfazer
        </Botao>
      </div>
    </div>
  );
}
