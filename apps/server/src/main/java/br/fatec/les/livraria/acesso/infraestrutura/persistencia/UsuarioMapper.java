package br.fatec.les.livraria.acesso.infraestrutura.persistencia;

import org.mapstruct.Mapper;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;
import br.fatec.les.livraria.acesso.dominio.modelo.Usuario;

/** Mapeamento domínio ↔ tabela {@code usuario} (componentModel {@code spring}, configurado no pom). */
@Mapper
public interface UsuarioMapper {

    UsuarioJpa paraJpa(Usuario usuario);

    Usuario paraDominio(UsuarioJpa usuario);

    TelefoneJpa paraJpa(Telefone telefone);

    Telefone paraDominio(TelefoneJpa telefone);
}
