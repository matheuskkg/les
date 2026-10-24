import type { components } from './api/esquema';
import { formatarCep, montarEndereco, type EnderecoDoFormulario } from './cadastroDeCliente.ts';

type EscolhaDaApi = components['schemas']['EscolhaDeEnderecoDto'];
type EnderecoCadastrado = components['schemas']['EnderecoDaResposta'];

/** Endereço escolhido na compra (RF-VEN-005, DEC-10): um dos cadastrados, pelo id, ou um novo, informado na tela. */
export type EscolhaDeEndereco = { enderecoId: number } | { novo: EnderecoDoFormulario };

export function escolhaParaApi(escolha: EscolhaDeEndereco): EscolhaDaApi {
  return 'enderecoId' in escolha ? { enderecoId: escolha.enderecoId } : { novo: montarEndereco(escolha.novo) };
}

/** Escolha inicial: o primeiro endereço cadastrado, ou nenhuma se não houver. */
export function escolhaInicial(enderecos: ReadonlyArray<EnderecoCadastrado>): EscolhaDeEndereco | null {
  const primeiro = enderecos[0];
  return primeiro ? { enderecoId: primeiro.id } : null;
}

export function ehCadastrado(escolha: EscolhaDeEndereco | null, enderecoId: number): boolean {
  return escolha !== null && 'enderecoId' in escolha && escolha.enderecoId === enderecoId;
}

/** Endereço em uma linha: "Rua X, 10 — Bairro, Cidade/UF, CEP 00000-000". */
export function resumoDoEndereco(
  endereco: Pick<EnderecoDoFormulario, 'logradouro' | 'numero' | 'bairro' | 'cidade' | 'uf' | 'cep'>,
): string {
  return `${endereco.logradouro}, ${endereco.numero} — ${endereco.bairro}, ${endereco.cidade}/${endereco.uf}, CEP ${formatarCep(endereco.cep)}`;
}
