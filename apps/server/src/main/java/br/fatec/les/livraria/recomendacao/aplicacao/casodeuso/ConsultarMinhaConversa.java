package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.catalogo.aplicacao.consulta.LivroListado;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.IdentificarCliente;
import br.fatec.les.livraria.recomendacao.aplicacao.LerCatalogo;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.MensagemExibida;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ConversasRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.MensagemDaConversa;
import lombok.RequiredArgsConstructor;

/**
 * Conversa ativa do cliente com o assistente, retomada em qualquer acesso (RNF-REC-001, DEC-03). Sem conversa
 * ativa, a lista volta vazia. Os livros citados aparecem enquanto seguirem ativos no catálogo; o cartão mostra o
 * preço e o disponível de agora.
 */
@Component
@RequiredArgsConstructor
public class ConsultarMinhaConversa {

    private final IdentificarCliente identificarCliente;
    private final ConversasRepositorio conversas;
    private final LerCatalogo lerCatalogo;

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
        Map<Long, LivroListado> ativos = lerCatalogo.ativos().stream()
                .collect(Collectors.toMap(livro -> livro.livro().id(), Function.identity()));
        return mensagens.stream()
                .map(mensagem -> new MensagemExibida(mensagem, mensagem.livrosCitados().stream()
                        .map(ativos::get)
                        .filter(Objects::nonNull)
                        .toList()))
                .toList();
    }
}
