/**
 * Identificação da bandeira pelo número, só para exibição (RN-CLI-005, DEC-18).
 *
 * Espelha os `prefixos` da tabela `bandeira` do seed: a regra de verdade fica no
 * server, que também confere o tamanho por bandeira e o dígito verificador.
 */
const BANDEIRAS: ReadonlyArray<{ nome: string; prefixos: ReadonlyArray<string> }> = [
  { nome: 'Visa', prefixos: ['4'] },
  { nome: 'Mastercard', prefixos: ['51-55', '2221-2720'] },
  {
    nome: 'Elo',
    prefixos: [
      '401178',
      '401179',
      '431274',
      '438935',
      '451416',
      '457393',
      '457631',
      '457632',
      '504175',
      '506699-506778',
      '509000-509999',
      '627780',
      '636297',
      '636368',
      '650031-650033',
      '650035-650051',
      '650405-650439',
      '650485-650538',
      '650541-650598',
      '650700-650718',
      '650720-650727',
      '650901-650978',
      '651652-651679',
      '655000-655019',
      '655021-655058',
    ],
  },
  { nome: 'American Express', prefixos: ['34', '37'] },
  { nome: 'Hipercard', prefixos: ['606282', '3841'] },
];

/** Quantos dígitos casam com o prefixo puro ou com a faixa inclusiva `ini-fim`; 0 se não casa. */
function casarPrefixo(prefixo: string, digitos: string): number {
  const [inicio, fim] = prefixo.split('-');
  if (inicio === undefined) {
    return 0;
  }
  if (fim === undefined) {
    return digitos.startsWith(inicio) ? inicio.length : 0;
  }
  if (digitos.length < inicio.length) {
    return 0;
  }
  const valor = Number(digitos.slice(0, inicio.length));
  return valor >= Number(inicio) && valor <= Number(fim) ? inicio.length : 0;
}

/**
 * Bandeira do número do cartão (aceita número com máscara) ou `null` sem
 * correspondência. O prefixo mais longo vence: `438935` é Elo antes do `4` da
 * Visa.
 */
export function identificarBandeira(numero: string): string | null {
  const digitos = numero.replace(/\D/g, '');
  let encontrada: string | null = null;
  let casados = 0;
  for (const { nome, prefixos } of BANDEIRAS) {
    for (const prefixo of prefixos) {
      const comprimento = casarPrefixo(prefixo, digitos);
      if (comprimento > casados) {
        casados = comprimento;
        encontrada = nome;
      }
    }
  }
  return encontrada;
}

/** Texto de ajuda exibido junto ao número do cartão, ou `undefined` sem bandeira identificada. */
export function ajudaDaBandeira(numero: string): string | undefined {
  const bandeira = identificarBandeira(numero);
  return bandeira === null ? undefined : `Bandeira: ${bandeira}`;
}
