import type { components } from '../../../api/esquema';

type ProvedorIa = components['schemas']['ProvedorIaDto'];
type Resultado = components['schemas']['UltimaFalhaDto']['resultado'];

/** Dias do período padrão das métricas: os 30 últimos, contando hoje (o server aceita até 366, DEC-03). */
export const DIAS_DO_PERIODO_PADRAO = 30;

/** Período das métricas na URL, em dias `AAAA-MM-DD`, com o inicial e o final inclusive (RNF-REC-001). */
export interface PeriodoDasMetricas {
  inicio: string;
  fim: string;
}

const FORMATO_DO_DIA = /^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$/;

/** Dia `AAAA-MM-DD` da data, no fuso do navegador. */
export function diaDe(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${data.getFullYear()}-${mes}-${dia}`;
}

/** Soma dias a um `AAAA-MM-DD`, pelo calendário em UTC para não depender do horário de verão. */
export function somarDias(dia: string, quantidade: number): string {
  const [ano = 0, mes = 1, numero = 1] = dia.split('-').map(Number);
  const data = new Date(Date.UTC(ano, mes - 1, numero + quantidade));
  return data.toISOString().slice(0, 10);
}

function lerDia(valor: string | null): string | null {
  const texto = valor?.trim() ?? '';
  return FORMATO_DO_DIA.test(texto) ? texto : null;
}

/**
 * Lê o período da query string; dia ausente ou malformado volta ao período padrão, que termina em `hoje`. Um fim
 * anterior ao início fica como está, para o server responder com a regra.
 */
export function lerPeriodoDasMetricas(busca: string, hoje: Date): PeriodoDasMetricas {
  const parametros = new URLSearchParams(busca);
  const fim = lerDia(parametros.get('fim')) ?? diaDe(hoje);
  const inicio = lerDia(parametros.get('inicio')) ?? somarDias(fim, 1 - DIAS_DO_PERIODO_PADRAO);
  return { inicio, fim };
}

/** Caminho da tela para o período. */
export function urlDoAssistente(periodo: PeriodoDasMetricas): string {
  return `/admin/assistente?${new URLSearchParams({ inicio: periodo.inicio, fim: periodo.fim })}`;
}

/** Troca o provedor de posição com o vizinho (`-1` sobe, `1` desce); nas pontas, a lista não muda. */
export function moverProvedor<T>(lista: readonly T[], indice: number, deslocamento: -1 | 1): T[] {
  const destino = indice + deslocamento;
  const nova = [...lista];
  const atual = nova[indice];
  const vizinho = nova[destino];
  if (atual === undefined || vizinho === undefined) {
    return nova;
  }
  nova[indice] = vizinho;
  nova[destino] = atual;
  return nova;
}

/** Se a ordem ou a ativação diferem da cadeia gravada. */
export function cadeiaAlterada(gravada: readonly ProvedorIa[], editada: readonly ProvedorIa[]): boolean {
  return (
    gravada.length !== editada.length ||
    gravada.some((provedor, indice) => {
      const outro = editada[indice];
      return outro === undefined || outro.id !== provedor.id || outro.ativo !== provedor.ativo;
    })
  );
}

/** Corpo do `PUT /api/admin/ia/provedores`: a ordem é a posição na lista. */
export function corpoDaCadeia(editada: readonly ProvedorIa[]) {
  return { provedores: editada.map((provedor) => ({ id: provedor.id, ativo: provedor.ativo })) };
}

/** Taxa de 0 a 1 como porcentagem pt-BR com até uma casa ("66,7%"); sem taxa, travessão. */
export function formatarTaxa(taxa: number | undefined): string {
  if (taxa === undefined) {
    return '—';
  }
  const porcentagem = Math.round(taxa * 1000) / 10;
  return `${String(porcentagem).replace('.', ',')}%`;
}

/** Inteiro com separador de milhar pt-BR ("12.345"). */
export function formatarInteiro(valor: number): string {
  return String(Math.trunc(valor)).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}

/** Latência em milissegundos ("1.234 ms"); sem medida, travessão. */
export function formatarLatencia(ms: number | undefined): string {
  return ms === undefined ? '—' : `${formatarInteiro(ms)} ms`;
}

/** Rótulo do dia no eixo X do gráfico: `24/10`. */
export function rotuloDoDia(dia: string): string {
  const [, mes, numero] = dia.split('-');
  return `${numero}/${mes}`;
}

const ROTULOS_DOS_RESULTADOS: Record<Resultado, string> = {
  SUCESSO: 'Sucesso',
  COTA_ESGOTADA: 'Cota esgotada',
  ERRO_HTTP: 'Erro HTTP',
  TEMPO_ESGOTADO: 'Tempo esgotado',
  ERRO_DE_REDE: 'Erro de rede',
  RESPOSTA_INVALIDA: 'Resposta inválida',
};

/** Resultado de uma chamada em português. */
export function rotuloDoResultado(resultado: Resultado): string {
  return ROTULOS_DOS_RESULTADOS[resultado];
}

/** Cores das séries do gráfico, nos dois primeiros slots da paleta categórica validada (dataviz). */
export const COR_DAS_CHAMADAS = '#2a78d6';
export const COR_DAS_FALHAS = '#eb6834';
