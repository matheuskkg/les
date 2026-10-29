package br.fatec.les.livraria.recomendacao.infraestrutura.externo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.recomendacao.aplicacao.TextoNormalizado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.AssistenteDeLivros;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.FerramentasDoAssistente;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.RespostaDoProvedor;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ResumoDeLivro;
import br.fatec.les.livraria.recomendacao.dominio.modelo.ItemRecomendado;
import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.TipoDeResposta;

/**
 * Assistente determinístico, sem rede, usado nos testes e nos E2E (RNF-REC-001, DEC-03). É o último provedor da
 * cadeia: quando nenhum provedor real tem chave, só ele responde. O nome e o modelo são {@code fake} e a chave é
 * sempre considerada configurada.
 *
 * <p>Regras:</p>
 * <ul>
 *   <li>{@code recomendar} devolve os candidatos na ordem recebida, com a justificativa
 *       "Indicado pelas categorias e autores que você costuma comprar." ou, sem livros comprados, "Entre os mais
 *       vendidos da loja.";</li>
 *   <li>{@code conversar} responde {@link TipoDeResposta#FORA_DO_ESCOPO} quando a mensagem contém "previsão do
 *       tempo" ou "futebol", sem diferenciar maiúsculas nem acentos;</li>
 *   <li>quando a mensagem pede recomendação, usa {@link FerramentasDoAssistente#recomendarParaMim()} e responde
 *       {@link TipoDeResposta#RECOMENDACAO} com até três livros;</li>
 *   <li>nas demais mensagens, busca com o texto sem pontuação e, se não houver resultado, tenta cada palavra de
 *       quatro ou mais letras até achar alguma. Com resultados, responde {@link TipoDeResposta#BUSCA} e cita até
 *       três livros; sem resultado, responde {@link TipoDeResposta#DUVIDA} sem livros.</li>
 * </ul>
 */
@Component("assistenteDeLivrosFake")
public class AssistenteDeLivrosFake implements AssistenteDeLivros {

    static final String NOME = "fake";
    static final String MODELO = "fake";
    static final String JUSTIFICATIVA_DO_PERFIL = "Indicado pelas categorias e autores que você costuma comprar.";
    static final String JUSTIFICATIVA_MAIS_VENDIDOS = "Entre os mais vendidos da loja.";
    static final String TEXTO_DE_BUSCA = "Encontrei estes livros para você.";
    static final String TEXTO_DE_RECOMENDACAO = "Separei estas leituras para você.";
    static final String TEXTO_DE_DUVIDA = "Não encontrei livros para essa busca. Tente pelo título, autor ou categoria.";
    static final String TEXTO_FORA_DO_ESCOPO = "Só posso ajudar com livros da loja.";

    private static final int LIMITE_DE_LIVROS_CITADOS = 3;
    private static final int TAMANHO_MINIMO_DA_PALAVRA = 4;

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public String modelo() {
        return MODELO;
    }

    @Override
    public boolean temChave() {
        return true;
    }

    @Override
    public RespostaDoProvedor<RespostaDeRecomendacao> recomendar(
            PerfilDoCliente perfil, List<LivroCandidato> candidatos) {
        String justificativa = perfil.semLivrosComprados() ? JUSTIFICATIVA_MAIS_VENDIDOS : JUSTIFICATIVA_DO_PERFIL;
        return RespostaDoProvedor.semTokens(new RespostaDeRecomendacao(candidatos.stream()
                .map(candidato -> new ItemRecomendado(candidato.id(), justificativa))
                .toList()));
    }

    @Override
    public RespostaDoProvedor<RespostaDoAssistente> conversar(
            PerfilDoCliente perfil,
            List<MensagemAnterior> historico,
            String mensagemNova,
            FerramentasDoAssistente ferramentas,
            List<ResumoDeLivro> catalogo) {
        if (foraDoEscopo(mensagemNova)) {
            return RespostaDoProvedor.semTokens(
                    new RespostaDoAssistente(TipoDeResposta.FORA_DO_ESCOPO, TEXTO_FORA_DO_ESCOPO, List.of()));
        }

        if (pedeRecomendacao(mensagemNova)) {
            List<ResumoDeLivro> recomendados = ferramentas.recomendarParaMim();
            if (!recomendados.isEmpty()) {
                return RespostaDoProvedor.semTokens(new RespostaDoAssistente(TipoDeResposta.RECOMENDACAO,
                        TEXTO_DE_RECOMENDACAO, citados(recomendados)));
            }
        }

        List<ResumoDeLivro> encontrados = buscar(ferramentas, mensagemNova);
        if (encontrados.isEmpty()) {
            return RespostaDoProvedor.semTokens(
                    new RespostaDoAssistente(TipoDeResposta.DUVIDA, TEXTO_DE_DUVIDA, List.of()));
        }

        return RespostaDoProvedor.semTokens(
                new RespostaDoAssistente(TipoDeResposta.BUSCA, TEXTO_DE_BUSCA, citados(encontrados)));
    }

    private static List<Long> citados(List<ResumoDeLivro> livros) {
        return livros.stream()
                .limit(LIMITE_DE_LIVROS_CITADOS)
                .map(ResumoDeLivro::id)
                .toList();
    }

    /** Busca primeiro pela mensagem inteira e, se necessário, por cada palavra que possa ser uma chave. */
    private static List<ResumoDeLivro> buscar(FerramentasDoAssistente ferramentas, String mensagem) {
        String texto = semPontuacao(mensagem);
        if (texto.isBlank()) {
            return List.of();
        }
        List<ResumoDeLivro> encontrados = ferramentas.buscarLivros(texto, null, null, null);
        if (!encontrados.isEmpty()) {
            return encontrados;
        }

        for (String palavra : palavrasDaBusca(mensagem)) {
            encontrados = ferramentas.buscarLivros(palavra, null, null, null);
            if (!encontrados.isEmpty()) {
                return encontrados;
            }
        }
        return List.of();
    }

    private static boolean foraDoEscopo(String mensagem) {
        String normalizada = TextoNormalizado.de(mensagem);
        return normalizada.contains("previsao do tempo") || normalizada.contains("futebol");
    }

    private static boolean pedeRecomendacao(String mensagem) {
        String normalizada = TextoNormalizado.de(mensagem);
        return normalizada.contains("recomend") || normalizada.contains("sugest");
    }

    private static String semPontuacao(String mensagem) {
        return mensagem.replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static List<String> palavrasDaBusca(String mensagem) {
        Set<String> palavras = new LinkedHashSet<>();
        for (String palavra : semPontuacao(mensagem).split(" ")) {
            if (palavra.length() >= TAMANHO_MINIMO_DA_PALAVRA) {
                palavras.add(palavra);
            }
        }
        return new ArrayList<>(palavras);
    }
}
