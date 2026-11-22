package br.fatec.les.livraria.acesso.infraestrutura.persistencia;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;

import org.mapstruct.Mapper;

/** Mapeamento domínio ↔ tabela {@code usuario} (componentModel {@code spring}, configurado no pom). */
@Mapper
public interface UsuarioMapper {

    UsuarioJpa paraJpa(Usuario usuario);

    Usuario paraDominio(UsuarioJpa usuario);

    TelefoneJpa paraJpa(Telefone telefone);

    Telefone paraDominio(TelefoneJpa telefone);
}
