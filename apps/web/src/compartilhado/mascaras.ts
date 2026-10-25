/** Máscaras de entrada dos formulários: cada uma limita o valor ao tamanho e formata enquanto o usuário digita. */
export type Mascara = 'cpf' | 'cep' | 'ddd' | 'telefone' | 'numeroDoCartao' | 'cvv';

const SEPARADORES = new Set(['-', '.', ' ']);

function digitosAte(valor: string, limite: number): string {
  return valor.replace(/\D/g, '').slice(0, limite);
}

function contarDigitos(valor: string, ate: number): number {
  return valor.slice(0, ate).replace(/\D/g, '').length;
}

/** CPF no formato "000.000.000-00", montado conforme os dígitos chegam. */
function mascararCpf(valor: string): string {
  const digitos = digitosAte(valor, 11);
  const partes = [digitos.slice(0, 3), digitos.slice(3, 6), digitos.slice(6, 9)];
  let formatado = partes.filter((parte) => parte !== '').join('.');
  const verificadores = digitos.slice(9, 11);
  if (verificadores !== '') {
    formatado += `-${verificadores}`;
  }
  return formatado;
}

/** CEP no formato "00000-000". */
function mascararCep(valor: string): string {
  const digitos = digitosAte(valor, 8);
  return digitos.length > 5 ? `${digitos.slice(0, 5)}-${digitos.slice(5)}` : digitos;
}

/**
 * Telefone com 8 dígitos ("0000-0000") ou 9 dígitos ("00000-0000").
 * Quando o valor chega de uma vez (colagem/autopreenchimento), ele traz o DDD: a máscara fica
 * com os últimos dígitos do número (10 dígitos → DDD + 8; 11 → DDD + 9).
 */
function mascararTelefone(valor: string, valorAnterior?: string): string {
  let digitos = valor.replace(/\D/g, '');
  const chegouDeUmaVez = valorAnterior !== undefined && valor.length > valorAnterior.length + 1;
  if (chegouDeUmaVez && digitos.length > 9) {
    digitos = digitos.length === 10 ? digitos.slice(-8) : digitos.slice(-9);
  }
  digitos = digitos.slice(0, 9);
  if (digitos.length > 8) {
    return `${digitos.slice(0, 5)}-${digitos.slice(5)}`;
  }
  return digitos.length > 4 ? `${digitos.slice(0, 4)}-${digitos.slice(4)}` : digitos;
}

/** Número do cartão: American Express (34/37) em 4-6-5; os demais em grupos de 4 dígitos. */
function mascararNumeroDoCartao(valor: string): string {
  const todos = digitosAte(valor, 19);
  if (/^3[47]/.test(todos)) {
    const digitos = todos.slice(0, 15);
    return [digitos.slice(0, 4), digitos.slice(4, 10), digitos.slice(10, 15)]
      .filter((parte) => parte !== '')
      .join(' ');
  }
  return todos.replace(/(\d{4})(?=\d)/g, '$1 ');
}

const MASCARAS: Record<Mascara, (valor: string, valorAnterior?: string) => string> = {
  cpf: mascararCpf,
  cep: mascararCep,
  ddd: (valor) => digitosAte(valor, 2),
  telefone: mascararTelefone,
  numeroDoCartao: mascararNumeroDoCartao,
  cvv: (valor) => digitosAte(valor, 4),
};

/**
 * Aplica a máscara ao valor digitado; o resultado já está limitado ao tamanho da máscara.
 * `valorAnterior` é o valor mascarado antes da edição, usado para distinguir digitação de
 * colagem/autopreenchimento no telefone.
 */
export function aplicarMascara(mascara: Mascara, valor: string, valorAnterior?: string): string {
  return MASCARAS[mascara](valor, valorAnterior);
}

/**
 * Posição do caret no valor mascarado: conta os dígitos antes do caret no valor digitado e
 * reposiciona depois da mesma quantidade de dígitos, para o cursor não pular para o fim.
 */
export function posicaoDoCaret(
  valorDigitado: string,
  caretDigitado: number,
  valorMascarado: string,
): number {
  const ate = Math.max(0, Math.min(caretDigitado, valorDigitado.length));
  const digitosAntes = contarDigitos(valorDigitado, ate);
  if (digitosAntes === 0) {
    return 0;
  }

  let vistos = 0;
  for (let indice = 0; indice < valorMascarado.length; indice += 1) {
    if (!/\d/.test(valorMascarado.charAt(indice))) {
      continue;
    }
    vistos += 1;
    if (vistos === digitosAntes) {
      return indice + 1;
    }
  }
  return valorMascarado.length;
}

/**
 * Corrige o backspace logo após um separador ("-", "." ou " "): o navegador apaga o separador,
 * a máscara o recoloca e o cursor fica preso. Nesse caso, apaga o dígito anterior ao separador.
 * Devolve `null` quando o backspace não caiu logo após um separador.
 */
export function apagarDigitoAntesDoSeparador(
  valorAnterior: string,
  valorDigitado: string,
  caretDigitado: number,
): { valor: string; caret: number } | null {
  if (valorDigitado.length !== valorAnterior.length - 1) {
    return null;
  }
  if (!SEPARADORES.has(valorAnterior.charAt(caretDigitado))) {
    return null;
  }

  let indiceDoDigito = caretDigitado - 1;
  while (indiceDoDigito >= 0 && !/\d/.test(valorAnterior.charAt(indiceDoDigito))) {
    indiceDoDigito -= 1;
  }
  if (indiceDoDigito < 0) {
    return null;
  }

  return {
    valor: valorAnterior.slice(0, indiceDoDigito) + valorAnterior.slice(indiceDoDigito + 1),
    caret: indiceDoDigito,
  };
}
