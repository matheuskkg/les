import assert from 'node:assert/strict';
import { readdirSync, readFileSync } from 'node:fs';
import { dirname, join, normalize, relative } from 'node:path';
import { test } from 'node:test';

// Regras de dependência do DEC-17, no papel que o ArchUnit tem no server.

const SRC = dirname(new URL(import.meta.url).pathname);

interface Importacao {
  arquivo: string;
  alvo: string;
  soTipo: boolean;
}

function arquivos(pasta: string): string[] {
  return readdirSync(pasta, { withFileTypes: true }).flatMap((entrada) => {
    const caminho = join(pasta, entrada.name);
    if (entrada.isDirectory()) {
      return arquivos(caminho);
    }
    return /\.tsx?$/.test(entrada.name) && !entrada.name.endsWith('.d.ts') ? [caminho] : [];
  });
}

function importacoes(): Importacao[] {
  const padrao = /^import\s+(type\s+)?[^'"]*?from\s+'([^']+)'|^import\s+'([^']+)'/gm;
  return arquivos(SRC).flatMap((caminho) => {
    const arquivo = relative(SRC, caminho);
    const texto = readFileSync(caminho, 'utf8');
    return [...texto.matchAll(padrao)].map((encontrado) => {
      const especificador = encontrado[2] ?? encontrado[3];
      assert.ok(especificador !== undefined);
      const alvo = especificador.startsWith('.')
        ? normalize(join(dirname(arquivo), especificador)).replace(/\.tsx?$/, '')
        : especificador;
      return { arquivo, alvo, soTipo: encontrado[1] !== undefined };
    });
  });
}

const todas = importacoes();

function violacoes(regra: (importacao: Importacao) => boolean): string[] {
  return todas.filter(regra).map(({ arquivo, alvo }) => `${arquivo} -> ${alvo}`);
}

const moduloDe = (caminho: string) => /^modulos\/([^/]+)\//.exec(caminho)?.[1];

test('ui/ e compartilhado/ não dependem de módulos nem do layout', () => {
  assert.deepEqual(
    violacoes(({ arquivo, alvo }) => /^(ui|compartilhado)\//.test(arquivo) && /^(modulos|layout)\//.test(alvo)),
    [],
  );
});

test('ui/ não chama a API: recebe dados por props', () => {
  assert.deepEqual(
    violacoes(({ arquivo, alvo, soTipo }) => arquivo.startsWith('ui/') && alvo === 'api/cliente' && !soTipo),
    [],
  );
});

test('um módulo só usa outro pelo index.ts ou pelas regras dele', () => {
  assert.deepEqual(
    violacoes(({ arquivo, alvo }) => {
      const origem = moduloDe(arquivo);
      const destino = moduloDe(`${alvo}/`);
      if (!origem || !destino || origem === destino) {
        return false;
      }
      return alvo !== `modulos/${destino}` && !alvo.startsWith(`modulos/${destino}/regras/`);
    }),
    [],
  );
});

test('módulos não dependem do layout nem da composição das rotas', () => {
  assert.deepEqual(
    violacoes(({ arquivo, alvo }) => arquivo.startsWith('modulos/') && /^(layout\/|app$|main$)/.test(alvo)),
    [],
  );
});

test('regras são puras: sem Preact, sem chamadas à API e sem telas', () => {
  assert.deepEqual(
    violacoes(({ arquivo, alvo, soTipo }) => {
      if (!/\/regras\//.test(arquivo) || arquivo.endsWith('.test.ts')) {
        return false;
      }
      if (alvo.startsWith('preact')) {
        return true;
      }
      if (alvo === 'api/cliente' && !soTipo) {
        return true;
      }
      return /\/(paginas|componentes)\//.test(`${alvo}/`) || alvo.startsWith('ui/');
    }),
    [],
  );
});
