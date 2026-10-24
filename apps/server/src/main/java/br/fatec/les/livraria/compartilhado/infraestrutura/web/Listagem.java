package br.fatec.les.livraria.compartilhado.infraestrutura.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declara uma listagem paginada: os campos ordenáveis e a ordem padrão. O parâmetro anotado é do tipo
 * {@link br.fatec.les.livraria.compartilhado.aplicacao.Paginacao} e é resolvido por {@link ResolvedorDePaginacao}
 * (DEC-07).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Listagem {

    /** Campos ordenáveis no formato {@code campoDaApi} ou {@code campoDaApi=propriedadeDoModelo}. */
    String[] campos();

    /** Ordem padrão no formato {@code campo,asc|desc}; usada quando o cliente não informa {@code ordem}. */
    String[] padrao() default {};
}
