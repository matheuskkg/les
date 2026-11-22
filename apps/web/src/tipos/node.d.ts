/**
 * Declarações mínimas do Node usadas pelos testes unitários (node --test).
 *
 * Os testes são tipados pelo tsconfig, mas a lista de dependências do web é fechada (DEC-01):
 * em vez de adicionar @types/node, declaramos só o que os testes consomem. Se um teste passar
 * a usar outra API do Node, amplie este arquivo.
 */

declare module 'node:test' {
  type CorpoDoTeste = (contexto?: unknown) => void | Promise<void>;

  function test(nome: string, corpo: CorpoDoTeste): void;

  export default test;
  export { test };
}

declare module 'node:assert/strict' {
  interface Assert {
    equal(actual: unknown, expected: unknown, mensagem?: string): void;
    deepEqual(actual: unknown, expected: unknown, mensagem?: string): void;
    ok(value: unknown, mensagem?: string): asserts value;
  }

  const assert: Assert;
  export default assert;
}

declare module 'node:fs' {
  interface Dirent {
    name: string;
    isDirectory(): boolean;
  }

  export function readdirSync(caminho: string): string[];
  export function readdirSync(caminho: string, opcoes: { withFileTypes: true }): Dirent[];
  export function readFileSync(caminho: string, codificacao: 'utf8'): string;
}

declare module 'node:path' {
  export function dirname(caminho: string): string;
  export function join(...partes: string[]): string;
  export function normalize(caminho: string): string;
  export function relative(de: string, para: string): string;
}
