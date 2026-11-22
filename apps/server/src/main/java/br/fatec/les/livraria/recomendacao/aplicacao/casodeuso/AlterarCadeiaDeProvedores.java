package br.fatec.les.livraria.recomendacao.aplicacao.casodeuso;

import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import br.fatec.les.livraria.recomendacao.aplicacao.consulta.ProvedorConfigurado;
import br.fatec.les.livraria.recomendacao.aplicacao.porta.ProvedoresIaRepositorio;
import br.fatec.les.livraria.recomendacao.dominio.modelo.AlteracaoDaCadeia;
import br.fatec.les.livraria.recomendacao.dominio.modelo.PosicaoNaCadeia;
import br.fatec.les.livraria.recomendacao.dominio.regra.CadeiaComTodosOsProvedores;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Reordena e ativa ou desativa os provedores da cadeia de IA (RNF-REC-001, DEC-03). A ordem é a posição na lista
 * enviada. Sem transação própria: a gravação do repositório faz o commit antes de o cache da cadeia ser descartado,
 * para que uma requisição concorrente não volte a guardar as linhas antigas.
 */
@Component
@RequiredArgsConstructor
public class AlterarCadeiaDeProvedores {

    private final ProvedoresIaRepositorio provedores;
    private final CadeiaComTodosOsProvedores cadeiaComTodosOsProvedores;
    private final CadeiaDeProvedores cadeia;
    private final ConsultarProvedoresIa consultarProvedores;

    /**
     * @param posicoes provedores na ordem desejada, com a ativação de cada um
     * @return a cadeia gravada, como em {@link ConsultarProvedoresIa}
     * @throws br.fatec.les.livraria.compartilhado.dominio.ExcecaoDeNegocio se a lista não trouxer cada provedor uma
     *     única vez (HTTP 422)
     */
    public List<ProvedorConfigurado> executar(List<PosicaoNaCadeia> posicoes) {
        AlteracaoDaCadeia alteracao = new AlteracaoDaCadeia(provedores.listarEmOrdem(), posicoes);
        Violacoes.verificar(alteracao, cadeiaComTodosOsProvedores);
        provedores.salvar(alteracao.aplicar());
        cadeia.invalidarCache();
        return consultarProvedores.executar();
    }
}
