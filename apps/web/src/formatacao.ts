/** Formatação de valores vindos da API, sem conta de dinheiro com number (DEC-07). */

const vazio = '';

/**
 * Formata um decimal em string (padrão da API: "1234.56") como "R$ 1.234,56".
 * Não converte para number em nenhum momento; só manipula os dígitos da string.
 */
export function formatarDinheiro(valor: string): string {
  const texto = valor.trim();
  if (texto === '' || !/^-?\d+(?:\.\d*)?$/.test(texto)) {
    return vazio;
  }

  const negativo = texto.startsWith('-');
  const semSinal = negativo ? texto.slice(1) : texto;
  const [inteiroBruto = '0', fracionarioBruto = ''] = semSinal.split('.');
  const inteiro = inteiroBruto.replace(/^0+(?=\d)/, '') || '0';
  const centavos = (fracionarioBruto + '00').slice(0, 2);
  const comMilhares = inteiro.replace(/\B(?=(\d{3})+(?!\d))/g, '.');

  return `${negativo ? '-' : ''}R$ ${comMilhares},${centavos}`;
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
