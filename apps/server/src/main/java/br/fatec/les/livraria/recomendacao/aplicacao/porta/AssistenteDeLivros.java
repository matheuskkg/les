package br.fatec.les.livraria.recomendacao.aplicacao.porta;

import br.fatec.les.livraria.recomendacao.dominio.modelo.LivroCandidato;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemAnterior;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PerfilDoCliente;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDeRecomendacao;
import br.fatec.les.livraria.recomendacao.dominio.modelo.RespostaDoAssistente;

import java.util.List;

/**
 * Porta de saída de um provedor de IA generativa, com a recomendação da vitrine e o chatbot (RNF-REC-001, DEC-03). Cada
 * adapter representa um provedor; a {@code CadeiaDeProvedores} escolhe um deles pela ordem da tabela {@code
 * provedor_ia}. Nenhum tipo do Spring AI aparece na porta: o domínio e a aplicação conhecem só os records abaixo.
 *
 * <p>Em falha, a operação lança {@link FalhaNaChamadaDeIa} para que a cadeia registre o resultado e passe ao próximo
 * provedor.
 */
public interface AssistenteDeLivros {

    /** Nome do provedor, igual ao da coluna {@code provedor_ia.nome} (por exemplo {@code gemini} ou {@code fake}). */
    String nome();

    /** Modelo usado pelo provedor, gravado em {@code chamada_ia.modelo}. */
    String modelo();

    /** Indica se o provedor tem chave configurada e pode entrar na cadeia (DEC-03). */
    boolean temChave();

    /**
     * Ordena os candidatos e justifica cada indicação para a vitrine (DEC-03). A saída é estruturada e validada pelo
     * server antes de ser gravada.
     *
     * @param perfil histórico e preferências do cliente
     * @param candidatos livros ativos e com estoque, já pontuados pelo server
     */
    RespostaDoProvedor<RespostaDeRecomendacao> recomendar(PerfilDoCliente perfil, List<LivroCandidato> candidatos);

    /**
     * Responde à mensagem nova do cliente usando o perfil, o histórico da conversa, o catálogo compacto e as tools só
     * de leitura (DEC-03). O id do cliente nunca vem do modelo: as tools o obtêm de {@code UsuarioAtual}.
     *
     * @param perfil histórico e preferências do cliente
     * @param historico últimas mensagens da conversa ativa, lidas do banco
     * @param mensagemNova mensagem que o cliente acabou de enviar
     * @param ferramentas tools de consulta ao catálogo, ao estoque e às compras do cliente
     * @param catalogo livros à venda, no formato compacto do prompt do chat (REC-09)
     */
    RespostaDoProvedor<RespostaDoAssistente> conversar(
            PerfilDoCliente perfil,
            List<MensagemAnterior> historico,
            String mensagemNova,
            FerramentasDoAssistente ferramentas,
            List<ResumoDeLivro> catalogo);
}
