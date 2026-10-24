package br.fatec.les.livraria.clientes.aplicacao.regra;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.acesso.aplicacao.casodeuso.ContasEmUso;
import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.clientes.aplicacao.casodeuso.DadosAlteradosDoCliente;
import br.fatec.les.livraria.compartilhado.dominio.Regra;
import br.fatec.les.livraria.compartilhado.dominio.Violacoes;
import lombok.RequiredArgsConstructor;

/**
 * Unicidade do telefone na alteração dos dados do cliente (RF-CLI-002, DEC-10): compara DDD + número, sem o tipo, e
 * ignora a própria conta. Manter o telefone atual não bloqueia a operação, mas o de outra conta em uso é violação.
 */
@Component
@RequiredArgsConstructor
public class TelefoneUnicoNaAlteracao implements Regra<DadosAlteradosDoCliente> {

    private final ContasEmUso contasEmUso;

    @Override
    public void validar(DadosAlteradosDoCliente alvo, Violacoes violacoes) {
        if (!Telefone.valido(alvo.ddd(), alvo.numero())) {
            return;
        }
        if (contasEmUso.telefoneEmUsoExceto(Telefone.somenteDigitos(alvo.ddd()),
                Telefone.somenteDigitos(alvo.numero()), alvo.usuarioId())) {
            violacoes.adicionar("RF-CLI-002", "Já existe uma conta em uso com este telefone.");
        }
    }
}
