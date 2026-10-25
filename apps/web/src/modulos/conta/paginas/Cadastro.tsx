import { useEffect, useRef, useState } from 'preact/hooks';
import { useLocation } from 'preact-iso';
import { cliente, interpretarProblema, type ProblemaDetalhe } from '../../../api/cliente';
import { ajudaDaBandeira } from '../regras/bandeiras';
import {
  GENEROS,
  TIPOS_DE_ENDERECO,
  TIPOS_DE_LOGRADOURO,
  TIPOS_DE_TELEFONE,
  UFS,
  cartaoVazio,
  enderecoVazio,
  formatarCep,
  mascararCpf,
  formatarValidade,
  formularioVazio,
  montarCadastro,
  validarCadastro,
  type CartaoDoFormulario,
  type EnderecoDoFormulario,
  type ErrosDoCadastro,
  type FormularioDeCadastro,
  type TelefoneDoFormulario,
} from '../regras/cadastroDeCliente';
import { CampoDeAreaDeTexto } from '../../../ui/CampoDeAreaDeTexto';
import { CampoDeFormulario } from '../../../ui/CampoDeFormulario';
import { CampoDeSelecao } from '../../../ui/CampoDeSelecao';
import { Erro } from '../../../ui/Erro';
import { Violacoes } from '../../../ui/Violacoes';
import { entrar, useSessao } from '../../sessao';
import { Botao, BotaoLink } from '../../../ui/Botao';

export default function Cadastro() {
  const { definirUsuario } = useSessao();
  const { route } = useLocation();
  const [formulario, definirFormulario] = useState<FormularioDeCadastro>(formularioVazio);
  const [erros, definirErros] = useState<ErrosDoCadastro>({});
  const [problema, definirProblema] = useState<ProblemaDetalhe | null>(null);
  const [falha, definirFalha] = useState<string | null>(null);
  const [enviando, definirEnviando] = useState(false);
  const [contaCriada, definirContaCriada] = useState(false);
  const resumo = useRef<HTMLDivElement>(null);

  // Leva o foco ao resumo quando a validação ou o server recusam o cadastro.
  useEffect(() => {
    if (problema !== null || falha !== null) {
      resumo.current?.focus();
    }
  }, [problema, falha]);

  function atualizarFormulario(mudancas: Partial<FormularioDeCadastro>): void {
    definirFormulario((atual) => ({ ...atual, ...mudancas }));
  }

  function atualizarTelefone(mudancas: Partial<TelefoneDoFormulario>): void {
    definirFormulario((atual) => ({ ...atual, telefone: { ...atual.telefone, ...mudancas } }));
  }

  function atualizarEndereco(
    indice: number,
    mudancas: Partial<EnderecoDoFormulario>,
  ): void {
    definirFormulario((atual) => ({
      ...atual,
      enderecos: atual.enderecos.map((endereco, posicao) =>
        posicao === indice ? { ...endereco, ...mudancas } : endereco,
      ),
    }));
  }

  function atualizarCartao(indice: number, mudancas: Partial<CartaoDoFormulario>): void {
    definirFormulario((atual) => ({
      ...atual,
      cartoes: atual.cartoes.map((cartao, posicao) =>
        posicao === indice ? { ...cartao, ...mudancas } : cartao,
      ),
    }));
  }

  function adicionarEndereco(): void {
    definirFormulario((atual) => ({
      ...atual,
      enderecos: [...atual.enderecos, enderecoVazio()],
    }));
  }

  function removerEndereco(indice: number): void {
    definirFormulario((atual) => {
      if (atual.enderecos.length <= 1) {
        return atual;
      }
      return {
        ...atual,
        enderecos: atual.enderecos.filter((_, posicao) => posicao !== indice),
      };
    });
  }

  function adicionarCartao(): void {
    definirFormulario((atual) => ({ ...atual, cartoes: [...atual.cartoes, cartaoVazio()] }));
  }

  function removerCartao(indice: number): void {
    definirFormulario((atual) => ({
      ...atual,
      cartoes: atual.cartoes.filter((_, posicao) => posicao !== indice),
    }));
  }

  const erroDeEndereco = (
    indice: number,
    campo: keyof EnderecoDoFormulario,
  ): string | undefined => erros[`enderecos.${indice}.${campo}`];

  const erroDeCartao = (indice: number, campo: keyof CartaoDoFormulario): string | undefined =>
    erros[`cartoes.${indice}.${campo}`];

  async function aoEnviar(evento: Event): Promise<void> {
    evento.preventDefault();
    if (enviando) {
      return;
    }

    definirProblema(null);
    definirFalha(null);

    const encontrados = validarCadastro(formulario);
    definirErros(encontrados);
    if (Object.keys(encontrados).length > 0) {
      definirFalha('Confira os campos destacados para concluir o cadastro.');
      return;
    }

    definirEnviando(true);
    try {
      const { error, response } = await cliente.POST('/api/clientes', {
        body: montarCadastro(formulario),
      });

      if (error !== undefined) {
        definirProblema(interpretarProblema(error, response.status));
        return;
      }

      try {
        const usuario = await entrar(formulario.email.trim(), formulario.senha);
        definirUsuario(usuario);
        route('/minha-conta', true);
      } catch {
        definirContaCriada(true);
        definirFalha(
          'Sua conta foi criada, mas não foi possível entrar automaticamente. Use a tela de login.',
        );
      }
    } catch {
      definirFalha('Não foi possível conectar ao servidor. Tente novamente.');
    } finally {
      definirEnviando(false);
    }
  }

  if (contaCriada) {
    return (
      <section class="pagina pagina--estreita">
        <h1>Conta criada</h1>
        <Erro mensagem={falha} />
        <p>Entre com o seu e-mail e a senha que você acabou de cadastrar.</p>
        <div class="acoes">
          <BotaoLink href="/login">
            Ir para o login
          </BotaoLink>
        </div>
      </section>
    );
  }

  return (
    <section class="pagina">
      <header>
        <h1>Criar conta</h1>
        <p class="pagina__introducao">
          Guarde seus dados, endereços e cartões para comprar com mais agilidade. Os campos com
          asterisco são obrigatórios.
        </p>
      </header>

      <form
        class="formulario"
        noValidate
        onSubmit={(evento) => {
          void aoEnviar(evento);
        }}
      >
        {(problema !== null || falha !== null) && (
          <div class="cadastro__resumo" ref={resumo} tabIndex={-1}>
            {problema?.violacoes && problema.violacoes.length > 0 ? (
              <Violacoes violacoes={problema.violacoes} />
            ) : (
              <Erro problema={problema} mensagem={falha} />
            )}
          </div>
        )}

        <fieldset class="grupo-do-cadastro">
          <legend>Dados pessoais</legend>

          <div class="grade-do-cadastro">
            <div class="campo--largo">
              <CampoDeFormulario
                id="nome"
                rotulo="Nome completo"
                valor={formulario.nome}
                aoMudar={(valor) => atualizarFormulario({ nome: valor })}
                name="nome"
                autoComplete="name"
                required
                erro={erros.nome}
              />
            </div>

            <CampoDeSelecao
              id="genero"
              rotulo="Gênero"
              valor={formulario.genero}
              aoMudar={(valor) =>
                atualizarFormulario({ genero: valor as FormularioDeCadastro['genero'] })
              }
              opcoes={GENEROS}
              name="genero"
              required
              placeholder="Selecione…"
              erro={erros.genero}
            />

            <CampoDeFormulario
              id="dataNascimento"
              rotulo="Data de nascimento"
              tipo="date"
              valor={formulario.dataNascimento}
              aoMudar={(valor) => atualizarFormulario({ dataNascimento: valor })}
              name="dataNascimento"
              autoComplete="bday"
              required
              erro={erros.dataNascimento}
            />

            <CampoDeFormulario
              id="cpf"
              rotulo="CPF"
              valor={formulario.cpf}
              aoMudar={(valor) => atualizarFormulario({ cpf: mascararCpf(valor) })}
              name="cpf"
              inputMode="numeric"
              placeholder="000.000.000-00"
              maxLength={14}
              required
              ajuda="Pode digitar com ou sem pontuação."
              erro={erros.cpf}
            />

            <CampoDeFormulario
              id="email"
              rotulo="E-mail"
              tipo="email"
              valor={formulario.email}
              aoMudar={(valor) => atualizarFormulario({ email: valor })}
              name="email"
              autoComplete="email"
              required
              erro={erros.email}
            />

            <CampoDeSelecao
              id="telefone-tipo"
              rotulo="Tipo de telefone"
              valor={formulario.telefone.tipo}
              aoMudar={(valor) =>
                atualizarTelefone({ tipo: valor as TelefoneDoFormulario['tipo'] })
              }
              opcoes={TIPOS_DE_TELEFONE}
              name="telefone.tipo"
              required
              erro={erros['telefone-tipo']}
            />

            <CampoDeFormulario
              id="ddd"
              rotulo="DDD"
              valor={formulario.telefone.ddd}
              aoMudar={(valor) => atualizarTelefone({ ddd: valor })}
              name="telefone.ddd"
              inputMode="numeric"
              placeholder="11"
              maxLength={2}
              required
              erro={erros.ddd}
            />

            <CampoDeFormulario
              id="telefone-numero"
              rotulo="Telefone"
              tipo="tel"
              valor={formulario.telefone.numero}
              aoMudar={(valor) => atualizarTelefone({ numero: valor })}
              name="telefone.numero"
              autoComplete="tel-national"
              placeholder="98765-4321"
              required
              erro={erros['telefone-numero']}
            />
          </div>
        </fieldset>

        <fieldset class="grupo-do-cadastro">
          <legend>Senha</legend>

          <div class="grade-do-cadastro">
            <CampoDeFormulario
              id="senha"
              rotulo="Senha"
              tipo="password"
              valor={formulario.senha}
              aoMudar={(valor) => atualizarFormulario({ senha: valor })}
              name="senha"
              autoComplete="new-password"
              required
              ajuda="Pelo menos 8 caracteres, com letras maiúsculas e minúsculas e um caractere especial."
              erro={erros.senha}
            />

            <CampoDeFormulario
              id="confirmacaoSenha"
              rotulo="Confirme a senha"
              tipo="password"
              valor={formulario.confirmacaoSenha}
              aoMudar={(valor) => atualizarFormulario({ confirmacaoSenha: valor })}
              name="confirmacaoSenha"
              autoComplete="new-password"
              required
              erro={erros.confirmacaoSenha}
            />
          </div>
        </fieldset>

        <fieldset class="grupo-do-cadastro">
          <legend>Endereços</legend>
          <p class="grupo-do-cadastro__introducao">
            Informe ao menos um endereço. O país é sempre o Brasil.
          </p>

          {formulario.enderecos.map((endereco, indice) => (
            <div class="item-do-cadastro" key={indice}>
              <div class="item-do-cadastro__cabecalho">
                <h3>Endereço {indice + 1}</h3>
                {formulario.enderecos.length > 1 && (
                  <Botao variante="secundario"
                    type="button"
                    aria-label={`Remover endereço ${indice + 1}`}
                    onClick={() => removerEndereco(indice)}
                  >
                    Remover
                  </Botao>
                )}
              </div>

              <div class="grade-do-cadastro">
                <CampoDeFormulario
                  id={`endereco-${indice}-nome-curto`}
                  rotulo="Nome curto"
                  valor={endereco.nomeCurto}
                  aoMudar={(valor) => atualizarEndereco(indice, { nomeCurto: valor })}
                  placeholder="Casa"
                  required
                  ajuda="Uma frase curta para identificar o endereço."
                  erro={erroDeEndereco(indice, 'nomeCurto')}
                />

                <CampoDeSelecao
                  id={`endereco-${indice}-tipo`}
                  rotulo="Tipo de endereço"
                  valor={endereco.tipoEndereco}
                  aoMudar={(valor) =>
                    atualizarEndereco(indice, {
                      tipoEndereco: valor as EnderecoDoFormulario['tipoEndereco'],
                    })
                  }
                  opcoes={TIPOS_DE_ENDERECO}
                  required
                  erro={erroDeEndereco(indice, 'tipoEndereco')}
                />

                <CampoDeSelecao
                  id={`endereco-${indice}-tipo-logradouro`}
                  rotulo="Tipo de logradouro"
                  valor={endereco.tipoLogradouro}
                  aoMudar={(valor) =>
                    atualizarEndereco(indice, {
                      tipoLogradouro: valor as EnderecoDoFormulario['tipoLogradouro'],
                    })
                  }
                  opcoes={TIPOS_DE_LOGRADOURO}
                  required
                  erro={erroDeEndereco(indice, 'tipoLogradouro')}
                />

                <div class="campo--largo">
                  <CampoDeFormulario
                    id={`endereco-${indice}-logradouro`}
                    rotulo="Logradouro"
                    valor={endereco.logradouro}
                    aoMudar={(valor) => atualizarEndereco(indice, { logradouro: valor })}
                    autoComplete="street-address"
                    required
                    erro={erroDeEndereco(indice, 'logradouro')}
                  />
                </div>

                <CampoDeFormulario
                  id={`endereco-${indice}-numero`}
                  rotulo="Número"
                  valor={endereco.numero}
                  aoMudar={(valor) => atualizarEndereco(indice, { numero: valor })}
                  required
                  erro={erroDeEndereco(indice, 'numero')}
                />

                <CampoDeFormulario
                  id={`endereco-${indice}-bairro`}
                  rotulo="Bairro"
                  valor={endereco.bairro}
                  aoMudar={(valor) => atualizarEndereco(indice, { bairro: valor })}
                  required
                  erro={erroDeEndereco(indice, 'bairro')}
                />

                <CampoDeFormulario
                  id={`endereco-${indice}-cep`}
                  rotulo="CEP"
                  valor={endereco.cep}
                  aoMudar={(valor) => atualizarEndereco(indice, { cep: formatarCep(valor) })}
                  inputMode="numeric"
                  placeholder="00000-000"
                  maxLength={9}
                  required
                  erro={erroDeEndereco(indice, 'cep')}
                />

                <CampoDeFormulario
                  id={`endereco-${indice}-cidade`}
                  rotulo="Cidade"
                  valor={endereco.cidade}
                  aoMudar={(valor) => atualizarEndereco(indice, { cidade: valor })}
                  autoComplete="address-level2"
                  required
                  erro={erroDeEndereco(indice, 'cidade')}
                />

                <CampoDeSelecao
                  id={`endereco-${indice}-uf`}
                  rotulo="Estado (UF)"
                  valor={endereco.uf}
                  aoMudar={(valor) =>
                    atualizarEndereco(indice, { uf: valor as EnderecoDoFormulario['uf'] })
                  }
                  opcoes={UFS}
                  placeholder="Selecione…"
                  required
                  erro={erroDeEndereco(indice, 'uf')}
                />

                <div class="campo">
                  <span class="campo__rotulo">País</span>
                  <p class="campo__valor-fixo">Brasil</p>
                </div>

                <div class="campo--largo">
                  <CampoDeAreaDeTexto
                    id={`endereco-${indice}-observacoes`}
                    rotulo="Observações"
                    valor={endereco.observacoes}
                    aoMudar={(valor) => atualizarEndereco(indice, { observacoes: valor })}
                    ajuda="Opcional: complemento, ponto de referência etc."
                    erro={erroDeEndereco(indice, 'observacoes')}
                  />
                </div>
              </div>
            </div>
          ))}

          <div class="acoes">
            <Botao variante="secundario" type="button" onClick={adicionarEndereco}>
              Adicionar endereço
            </Botao>
          </div>
        </fieldset>

        <fieldset class="grupo-do-cadastro">
          <legend>Cartões de crédito (opcional)</legend>
          <p class="grupo-do-cadastro__introducao">
            O cartão não é obrigatório no cadastro. Havendo cartões, o primeiro será o
            preferencial, e o código de segurança não é armazenado.
          </p>

          {formulario.cartoes.map((cartao, indice) => (
            <div class="item-do-cadastro" key={indice}>
              <div class="item-do-cadastro__cabecalho">
                <h3>Cartão {indice + 1}</h3>
                <Botao variante="secundario"
                  type="button"
                  aria-label={`Remover cartão ${indice + 1}`}
                  onClick={() => removerCartao(indice)}
                >
                  Remover
                </Botao>
              </div>

              <div class="grade-do-cadastro">
                <div class="campo--largo">
                  <CampoDeFormulario
                    id={`cartao-${indice}-numero`}
                    rotulo="Número do cartão"
                    valor={cartao.numero}
                    aoMudar={(valor) => atualizarCartao(indice, { numero: valor })}
                    inputMode="numeric"
                    placeholder="0000 0000 0000 0000"
                    maxLength={23}
                    required
                    ajuda={ajudaDaBandeira(cartao.numero)}
                    erro={erroDeCartao(indice, 'numero')}
                  />
                </div>

                <CampoDeFormulario
                  id={`cartao-${indice}-nome-impresso`}
                  rotulo="Nome impresso"
                  valor={cartao.nomeImpresso}
                  aoMudar={(valor) => atualizarCartao(indice, { nomeImpresso: valor })}
                  autoComplete="cc-name"
                  required
                  erro={erroDeCartao(indice, 'nomeImpresso')}
                />

                <CampoDeFormulario
                  id={`cartao-${indice}-validade`}
                  rotulo="Validade"
                  tipo="month"
                  valor={cartao.validade}
                  aoMudar={(valor) => atualizarCartao(indice, { validade: valor })}
                  required
                  ajuda={
                    formatarValidade(cartao.validade) === ''
                      ? 'Mês e ano, como aparecem no cartão.'
                      : `Será gravada como ${formatarValidade(cartao.validade)}.`
                  }
                  erro={erroDeCartao(indice, 'validade')}
                />
              </div>
            </div>
          ))}

          <div class="acoes">
            <Botao variante="secundario" type="button" onClick={adicionarCartao}>
              Adicionar cartão
            </Botao>
          </div>
        </fieldset>

        <div class="acoes">
          <Botao type="submit" disabled={enviando}>
            {enviando ? 'Criando conta…' : 'Criar conta'}
          </Botao>
          <BotaoLink variante="secundario" href="/login">
            Já tenho conta
          </BotaoLink>
        </div>
      </form>
    </section>
  );
}
