package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.util.List;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import lombok.RequiredArgsConstructor;

/**
 * Adapter JPA das bandeiras (RN-CLI-005, DEC-16, DEC-18). A tabela é estática, preenchida pelo seed, então ela é
 * lida e interpretada uma única vez e a lista fica em memória. A primeira leitura acontece depois de o seed rodar,
 * porque o cache é preenchido sob demanda.
 */
@Component
@RequiredArgsConstructor
public class BandeiraRepositorioJpa implements BandeiraRepositorio {

    private final BandeiraJpaRepository repositorio;
    private final ClienteMapper mapper;

    private volatile List<Bandeira> cache;

    @Override
    public List<Bandeira> listar() {
        List<Bandeira> atuais = cache;
        if (atuais == null) {
            synchronized (this) {
                atuais = cache;
                if (atuais == null) {
                    atuais = repositorio.findAllByOrderByIdAsc().stream().map(mapper::paraDominio).toList();
                    cache = atuais;
                }
            }
        }
        return atuais;
    }
}
