import { BotaoLink } from '../ui/Botao';

export default function NaoEncontrada() {
  return (
    <section class="pagina pagina--estreita">
      <p class="destaque__sobretitulo">Erro 404</p>
      <h1>Página não encontrada</h1>
      <p class="pagina__introducao">
        O endereço acessado não existe ou foi movido para outro lugar.
      </p>
      <div class="acoes">
        <BotaoLink href="/">
          Voltar para a vitrine
        </BotaoLink>
      </div>
    </section>
  );
}
