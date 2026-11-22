package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.casodeuso.ConsultarLivrosAtivos;
import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.MensagemExibida;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import lombok.RequiredArgsConstructor;

/**
 * Conversa ativa do cliente com o assistente, retomada em qualquer acesso (RNF-REC-001, DEC-03). Sem conversa
 * ativa, a lista volta vazia. Os livros citados aparecem enquanto seguirem ativos no catálogo; o cartão mostra o
 * preço e o disponível de agora. Só os livros citados são consultados, em um lote por id (RNF-GER-001), e não a
 * vitrine inteira.
 */
@Component
@RequiredArgsConstructor
public class ConsultarMinhaConversa {

    private final IdentificarCliente identificarCliente;
    private final ConversasRepositorio conversas;
    private final ConsultarLivrosAtivos consultarLivrosAtivos;

    public List<MensagemExibida> executar() {
        Long clienteId = identificarCliente.executar();
        return conversas.conversaAtiva(clienteId)
                .map(conversaId -> exibir(conversas.mensagens(conversaId)))
                .orElse(List.of());
    }

    private List<MensagemExibida> exibir(List<MensagemDaConversa> mensagens) {
        if (mensagens.stream().allMatch(mensagem -> mensagem.livrosCitados().isEmpty())) {
            return mensagens.stream().map(mensagem -> new MensagemExibida(mensagem, List.of())).toList();
        }
        Map<Long, LivroListado> ativos = consultarLivrosAtivos.executar(mensagens.stream()
                .flatMap(mensagem -> mensagem.livrosCitados().stream())
                .distinct()
                .toList());
        return mensagens.stream()
                .map(mensagem -> new MensagemExibida(mensagem, mensagem.livrosCitados().stream()
                        .map(ativos::get)
                        .filter(Objects::nonNull)
                        .toList()))
                .toList();
    }
}
