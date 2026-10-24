package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import java.sql.Types;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * Dialeto do H2 usado no profile {@code h2}. O Envers grava o tipo da revisão (ADD/MOD/DEL) numa coluna
 * {@code tinyint} ({@code RevisionTypeType}), que o H2 em modo PostgreSQL recusa; {@code smallint} é o tipo que o
 * PostgreSQL usa para a mesma coluna, então os dois profiles ficam iguais.
 */
public class DialetoH2 extends H2Dialect {

    public DialetoH2() {
    }

    public DialetoH2(DialectResolutionInfo informacoes) {
        super(informacoes);
    }

    @Override
    protected String columnType(int tipoSql) {
        return tipoSql == Types.TINYINT ? "smallint" : super.columnType(tipoSql);
    }
}
