/** Formatação de valores vindos da API, sem conta de dinheiro com number (DEC-07). */

function avancarDigito(digito: string): string {
  return String.fromCharCode(digito.charCodeAt(0) + 1);
}

/** Soma um centavo em "inteiro" + "centavos", com carry dígito a dígito. */
function somarCentavo(
  inteiro: string,
  centavos: string,
): { inteiro: string; centavos: string } {
  const dezena = centavos.charAt(0);
  const unidade = centavos.charAt(1);

  if (unidade !== '9') {
    return { inteiro, centavos: dezena + avancarDigito(unidade) };
  }
  if (dezena !== '9') {
    return { inteiro, centavos: avancarDigito(dezena) + '0' };
  }

  const digitos = inteiro.split('');
  let posicao = digitos.length - 1;
  while (posicao >= 0 && digitos[posicao] === '9') {
    digitos[posicao] = '0';
    posicao -= 1;
  }
  if (posicao < 0) {
    digitos.unshift('1');
  } else {
    digitos[posicao] = avancarDigito(digitos[posicao] ?? '0');
  }
  return { inteiro: digitos.join(''), centavos: '00' };
}

/**
 * Formata um decimal em string (padrão da API: "1234.56") como "R$ 1.234,56".
 * Não converte para number em nenhum momento; só manipula os dígitos da string.
 */
export function formatarDinheiro(valor: string): string {
  const texto = valor.trim();
  if (texto === '' || !/^-?\d+(?:\.\d*)?$/.test(texto)) {
    return valor;
  }

  const negativo = texto.startsWith('-');
  const semSinal = negativo ? texto.slice(1) : texto;
  const [inteiroBruto = '0', fracionarioBruto = ''] = semSinal.split('.');
  const inteiroNormalizado = inteiroBruto.replace(/^0+(?=\d)/, '') || '0';

  let inteiro = inteiroNormalizado;
  let centavos = (fracionarioBruto + '00').slice(0, 2);

  // DEC-15.1: arredondamento HALF_UP na terceira casa, dígito a dígito (sem number).
  if (fracionarioBruto.length > 2 && (fracionarioBruto.charAt(2) || '0') >= '5') {
    ({ inteiro, centavos } = somarCentavo(inteiro, centavos));
  }

  const comMilhares = inteiro.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${negativo ? '-' : ''}R$ ${comMilhares},${centavos}`;
}

/** Centavos inteiros como "R$ 1.234,56", sem passar por ponto flutuante. */
export function formatarCentavos(centavos: number): string {
  const absoluto = Math.abs(Math.trunc(centavos));
  const reais = String(Math.floor(absoluto / 100)).replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${centavos < 0 ? '-' : ''}R$ ${reais},${String(absoluto % 100).padStart(2, '0')}`;
}

/**
 * Decimal da API (número ou string, como "21.5") com vírgula pt-BR ("21,5").
 * Usado em medidas e peso, que não têm duas casas fixas como o dinheiro.
 */
export function formatarDecimal(valor: number | string): string {
  if (typeof valor === 'number') {
    return Number.isFinite(valor) ? String(valor).replace('.', ',') : String(valor);
  }

  const texto = valor.trim();
  return texto.replace('.', ',');
}

/** Data ISO ("2026-10-21") como "21/10/2026", sem passar por Date e sem fuso. */
export function formatarData(iso: string): string {
  const partes = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso.trim());
  if (!partes) {
    return iso;
  }

  const [, ano, mes, dia] = partes;
  if (!ano || !mes || !dia) {
    return iso;
  }
  return `${dia}/${mes}/${ano}`;
}

/** Instant ISO ("2026-10-21T14:30:00Z") como "21/10/2026 11:30" no fuso local. */
export function formatarDataHora(iso: string): string {
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) {
    return iso;
  }

  const partes = new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  }).formatToParts(data);

  const valorDe = (tipo: Intl.DateTimeFormatPartTypes): string =>
    partes.find((parte) => parte.type === tipo)?.value ?? '';

  return `${valorDe('day')}/${valorDe('month')}/${valorDe('year')} ${valorDe('hour')}:${valorDe(
    'minute',
  )}`;
}

/** Nome de enum da API ("EM_PROCESSAMENTO") como texto do DRS ("EM PROCESSAMENTO"). */
export function textoDoStatus(status: string): string {
  return status.replaceAll('_', ' ').trim().toUpperCase();
}

/** CPF só com dígitos exibido como 000.000.000-00; outro formato volta como veio. */
export function formatarCpf(cpf: string): string {
  const digitos = cpf.replace(/\D/g, '');
  if (digitos.length !== 11) {
    return cpf;
  }
  return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
}

/** CEP só com dígitos exibido como 00000-000; outro formato volta como veio. */
export function formatarCep(cep: string): string {
  const digitos = cep.replace(/\D/g, '');
  if (digitos.length !== 8) {
    return cep;
  }
  return `${digitos.slice(0, 5)}-${digitos.slice(5)}`;
}

export type TomDoStatus = 'neutro' | 'positivo' | 'atencao' | 'critico';

const TONS: Record<string, TomDoStatus> = {
  EM_PROCESSAMENTO: 'neutro',
  APROVADA: 'positivo',
  REPROVADA: 'critico',
  EM_TRANSPORTE: 'atencao',
  ENTREGUE: 'positivo',
  EM_TROCA: 'atencao',
  TROCA_AUTORIZADA: 'atencao',
  TROCA_RECUSADA: 'critico',
  TROCADO: 'positivo',
};

/** Tom visual de um status de pedido ou de troca: o que pede ação fica em atenção, o que falhou em crítico. */
export function tomDoStatus(status: string): TomDoStatus {
  return TONS[status.trim().toUpperCase().replaceAll(' ', '_')] ?? 'neutro';
}
