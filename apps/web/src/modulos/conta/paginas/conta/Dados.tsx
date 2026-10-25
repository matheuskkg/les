import { useEffect, useState } from 'preact/hooks';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../../api/cliente';
import type { components } from '../../../../api/esquema';
import { CampoDeFormulario } from '../../../../ui/CampoDeFormulario';
import { CampoDeSelecao } from '../../../../ui/CampoDeSelecao';
import { Erro } from '../../../../ui/Erro';
import { Violacoes } from '../../../../ui/Violacoes';
import { GENEROS, TIPOS_DE_TELEFONE, mascararCpf, somenteDigitos } from '../../regras/cadastroDeCliente';
import { useSessao } from '../../../sessao';
import { InativarConta } from './InativarConta';
import { Botao } from '../../../../ui/Botao';

type MeusDados = components['schemas']['MeusDados'];
type Alteracao = components['schemas']['AlteracaoDeDadosCliente'];

/** Formulário com os dados como o servidor espera, mas em texto para os campos. */
function paraFormulario(dados: MeusDados): Alteracao {
  return {
    nome: dados.nome,
    genero: dados.genero,
    dataNascimento: dados.dataNascimento,
    cpf: dados.cpf,
    email: dados.email,
    telefone: { ...dados.telefone },
  };
}

/** Consulta e alteração dos dados do próprio cliente (RF-CLI-002); endereços e cartões têm telas próprias. */
export function Dados() {
  const { usuario, definirUsuario } = useSessao();
  const [dados, definirDados] = useState<MeusDados | null>(null);
  const [formulario, definirFormulario] = useState<Alteracao | null>(null);
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [salvo, definirSalvo] = useState(false);
  const [enviando, definirEnviando] = useState(false);

  useEffect(() => {
    const controle = new AbortController();
    cliente
      .GET('/api/me', { signal: controle.signal })
      .then(({ data, error, response }) => {
        if (controle.signal.aborted) {
          return;
        }
        if (!data) {
          definirProblema(interpretarProblema(error, response.status));
          return;
        }
        definirDados(data);
        definirFormulario(paraFormulario(data));
      })
      .catch(() => {
        if (!controle.signal.aborted) {
          definirProblema({ status: 0, detail: 'Não foi possível carregar os seus dados. Tente novamente.' });
        }
      });
    return () => controle.abort();
  }, []);

  if (formulario === null || dados === null) {
    return problema ? (
      <Erro problema={problema} />
    ) : (
      <p class="aviso" role="status">
        Carregando…
      </p>
    );
  }

  function mudar<K extends keyof Alteracao>(campo: K, valor: Alteracao[K]) {
    definirSalvo(false);
    definirFormulario((atual) => (atual === null ? atual : { ...atual, [campo]: valor }));
  }

  function mudarTelefone(campo: keyof Alteracao['telefone'], valor: string) {
    definirSalvo(false);
    definirFormulario((atual) =>
      atual === null ? atual : { ...atual, telefone: { ...atual.telefone, [campo]: valor } },
    );
  }

  async function salvar(evento: Event) {
    evento.preventDefault();
    if (formulario === null || enviando) {
      return;
    }
    if (somenteDigitos(formulario.cpf ?? '').length !== 11) {
      definirSalvo(false);
      definirProblema({ status: 0, detail: 'Informe o CPF com 11 dígitos.' });
      return;
    }
    definirEnviando(true);
    definirProblema(null);
    try {
      const { data, error, response } = await cliente.PUT('/api/me', {
        body: {
          ...formulario,
          cpf: formulario.cpf ? somenteDigitos(formulario.cpf) : formulario.cpf,
          telefone: {
            tipo: formulario.telefone.tipo,
            ddd: somenteDigitos(formulario.telefone.ddd),
            numero: somenteDigitos(formulario.telefone.numero),
          },
        },
      });
      if (!data) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }
      definirDados(data);
      definirFormulario(paraFormulario(data));
      definirSalvo(true);
      if (usuario && usuario.email !== data.email) {
        definirUsuario({ ...usuario, email: data.email });
      }
    } catch {
      definirProblema({ status: 0, detail: 'Não foi possível conectar ao servidor. Tente novamente.' });
    } finally {
      definirEnviando(false);
    }
  }

  return (
    <>
      <form class="formulario cartao" noValidate onSubmit={(evento) => void salvar(evento)}>
        <h2>Meus dados</h2>
        <p>Código do cliente: {dados.id}</p>
        <p>
          Ranking de cliente: <strong data-testid="ranking">{dados.ranking}</strong> de 100, pelas compras dos últimos
          12 meses.
        </p>

        {problema?.violacoes && problema.violacoes.length > 0 ? (
          <Violacoes violacoes={problema.violacoes} />
        ) : (
          <Erro problema={problema} />
        )}
        {salvo && (
          <p class="aviso" role="status">
            Dados atualizados.
          </p>
        )}

        <div class="grade-do-cadastro">
          <div class="campo--largo">
            <CampoDeFormulario id="dados-nome" rotulo="Nome" valor={formulario.nome} required
              aoMudar={(valor) => mudar('nome', valor)} autoComplete="name" />
          </div>
          <CampoDeSelecao id="dados-genero" rotulo="Gênero" valor={formulario.genero} opcoes={GENEROS} required
            aoMudar={(valor) => {
              const genero = GENEROS.find((opcao) => opcao.valor === valor);
              if (genero) {
                mudar('genero', genero.valor);
              }
            }} />
          <CampoDeFormulario id="dados-nascimento" rotulo="Data de nascimento" tipo="date" required
            valor={formulario.dataNascimento} aoMudar={(valor) => mudar('dataNascimento', valor)} />
          <CampoDeFormulario id="dados-cpf" rotulo="CPF" valor={mascararCpf(formulario.cpf ?? '')} required
            inputMode="numeric" aoMudar={(valor) => mudar('cpf', valor)} />
          <CampoDeFormulario id="dados-email" rotulo="E-mail" tipo="email" valor={formulario.email} required
            autoComplete="email" aoMudar={(valor) => mudar('email', valor)} />
          <CampoDeSelecao id="dados-telefone-tipo" rotulo="Tipo de telefone" valor={formulario.telefone.tipo}
            opcoes={TIPOS_DE_TELEFONE} required
            aoMudar={(valor) => {
              const tipo = TIPOS_DE_TELEFONE.find((opcao) => opcao.valor === valor);
              if (tipo) {
                mudarTelefone('tipo', tipo.valor);
              }
            }} />
          <CampoDeFormulario id="dados-ddd" rotulo="DDD" valor={formulario.telefone.ddd} required
            inputMode="numeric" maxLength={2} aoMudar={(valor) => mudarTelefone('ddd', valor)} />
          <CampoDeFormulario id="dados-numero" rotulo="Número" valor={formulario.telefone.numero} required
            inputMode="tel" aoMudar={(valor) => mudarTelefone('numero', valor)} />
        </div>

        <div class="acoes">
          <Botao type="submit" disabled={enviando}>
            {enviando ? 'Salvando…' : 'Salvar alterações'}
          </Botao>
        </div>
      </form>

      <InativarConta />
    </>
  );
}
