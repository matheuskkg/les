package br.fatec.les.livraria.clientes.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Component;

import br.fatec.les.livraria.clientes.aplicacao.porta.BandeiraRepositorio;
import br.fatec.les.livraria.clientes.dominio.modelo.Bandeira;
import lombok.RequiredArgsConstructor;

/** Adapter JPA das bandeiras (RN-CLI-005, DEC-16). */
@Component
@RequiredArgsConstructor
public class BandeiraRepositorioJpa implements BandeiraRepositorio {

    private final BandeiraJpaRepository repositorio;
    private final ClienteMapper mapper;

    @Override
    public Optional<Bandeira> buscarPorId(Long id) {
        return repositorio.findById(id).map(mapper::paraDominio);
    }
}
