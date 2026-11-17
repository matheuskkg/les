/**
 * Declaração mínima de `node:fs/promises` para ler o CSV baixado no teste de exportação. A lista fechada de
 * dependências do DEC-01 não inclui `@types/node`; assim como o `process` do `playwright.config.ts`, só o que o
 * teste usa é declarado.
 */
declare module 'node:fs/promises' {
  export function readFile(caminho: string, codificacao: 'utf8'): Promise<string>;
}
