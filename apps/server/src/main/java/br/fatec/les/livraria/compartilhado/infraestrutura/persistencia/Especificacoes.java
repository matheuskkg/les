package br.fatec.les.livraria.compartilhado.infraestrutura.persistencia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;

import br.fatec.les.livraria.compartilhado.aplicacao.Intervalo;
import br.fatec.les.livraria.compartilhado.aplicacao.ParametroInvalido;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/**
 * Fábrica de {@link Specification} reutilizáveis para os filtros combinados das listagens (DEC-07, RF-CLI-004,
 * RF-LIV-005). Todo valor passa por {@code trim} e valor nulo ou em branco é ignorado.
 */
public final class Especificacoes {

    /** Letras acentuadas trocadas pelo {@code translate}, que existe no H2 e no PostgreSQL. */
    private static final String ACENTUADAS = "áàâãäéèêëíìîïóòôõöúùûüçñÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ";
    private static final String SEM_ACENTO = "aaaaaeeeeiiiiooooouuuucnAAAAAEEEEIIIIOOOOOUUUUCN";
    private static final char ESCAPE = '\\';

    private Especificacoes() {
    }

    /** "Contém" em texto, sem diferenciar maiúsculas nem acentos. */
    public static <T> Specification<T> contem(String campo, String termo) {
        return contem(raiz -> raiz.get(campo), termo);
    }

    /**
     * "Contém" em texto de um campo de um relacionamento muitos-para-um, sem diferenciar maiúsculas nem
     * acentos. Diferente de {@link #existe(String, String, Collection)}, não filtra muitos-para-muitos: a
     * navegação para um único registro não duplica linhas.
     */
    public static <T> Specification<T> contem(String relacao, String campo, String termo) {
        return contem(raiz -> raiz.get(relacao).get(campo), termo);
    }

    private static <T> Specification<T> contem(Function<Root<T>, Path<String>> caminho, String termo) {
        String escapado = padraoContem(termo);
        if (escapado == null) {
            return semFiltro();
        }
        return (raiz, consulta, cb) -> predicadoContem(cb, caminho.apply(raiz), escapado);
    }

    /** Igualdade; texto passa por {@code trim} e valor nulo ou em branco é ignorado. */
    public static <T> Specification<T> igual(String campo, Object valor) {
        Object limpo = valorLimpo(valor);
        if (limpo == null) {
            return semFiltro();
        }
        return (raiz, consulta, cb) -> cb.equal(raiz.get(campo), limpo);
    }

    /** Igualdade a qualquer um dos valores informados (OU dentro do campo). */
    public static <T> Specification<T> em(String campo, Collection<?> valores) {
        List<Object> limpos = valoresLimpos(valores);
        if (limpos.isEmpty()) {
            return semFiltro();
        }
        return (raiz, consulta, cb) -> raiz.get(campo).in(limpos);
    }

    /**
     * Intervalo inclusivo; {@code de} e {@code ate} podem ser nulos, mas {@code de} maior que {@code ate}
     * causa HTTP 400.
     */
    public static <T, Y extends Comparable<? super Y>> Specification<T> entre(String campo, Y de, Y ate) {
        Y inicio = limparIntervalo(de);
        Y fim = limparIntervalo(ate);
        if (inicio == null && fim == null) {
            return semFiltro();
        }
        Intervalo.validar(campo, inicio, fim);
        return (raiz, consulta, cb) -> {
            Path<Y> caminho = raiz.get(campo);
            if (inicio != null && fim != null) {
                return cb.between(caminho, inicio, fim);
            }
            if (inicio != null) {
                return cb.greaterThanOrEqualTo(caminho, inicio);
            }
            return cb.lessThanOrEqualTo(caminho, fim);
        };
    }

    /** Compara só os dígitos de CPF, CEP ou telefone; a coluna guarda o valor sem máscara. */
    public static <T> Specification<T> digitos(String campo, String termo) {
        String digitos = apenasDigitos(campo, termo);
        if (digitos == null) {
            return semFiltro();
        }
        return (raiz, consulta, cb) -> cb.equal(raiz.get(campo), digitos);
    }

    /**
     * Filtra um relacionamento muitos-para-muitos por {@code exists}, para não duplicar linhas nem quebrar a
     * contagem da paginação. Vários valores combinam com OU; a coleção vazia ou em branco não filtra.
     */
    public static <T> Specification<T> existe(String relacao, String campo, Collection<?> valores) {
        List<Object> limpos = valoresLimpos(valores);
        if (limpos.isEmpty()) {
            return semFiltro();
        }
        return (raiz, consulta, cb) -> {
            Subquery<Integer> subconsulta = consulta.subquery(Integer.class);
            Root<T> externa = subconsulta.correlate(raiz);
            Join<T, ?> juncao = externa.join(relacao);
            subconsulta.select(cb.literal(1)).where(juncao.get(campo).in(limpos));
            return cb.exists(subconsulta);
        };
    }

    /**
     * Filtra um relacionamento muitos-para-muitos por um único valor, por {@code exists}, para não duplicar linhas
     * nem quebrar a contagem da paginação.
     */
    public static <T> Specification<T> existe(String relacao, String campo, Object valor) {
        Object limpo = valorLimpo(valor);
        if (limpo == null) {
            return semFiltro();
        }
        return existe(relacao, campo, List.of(limpo));
    }

    /**
     * "Contém" em um campo de texto de um relacionamento muitos-para-muitos, por {@code exists}, para não
     * duplicar linhas nem quebrar a contagem da paginação. Como {@link #contem(String, String)}, não diferencia
     * maiúsculas nem acentos.
     */
    public static <T> Specification<T> existeContem(String relacao, String campo, String termo) {
        String escapado = padraoContem(termo);
        if (escapado == null) {
            return semFiltro();
        }
        return (raiz, consulta, cb) -> {
            Subquery<Integer> subconsulta = consulta.subquery(Integer.class);
            Root<T> externa = subconsulta.correlate(raiz);
            Join<T, ?> juncao = externa.join(relacao);
            subconsulta.select(cb.literal(1)).where(predicadoContem(cb, juncao.get(campo), escapado));
            return cb.exists(subconsulta);
        };
    }

    /** Prepara o termo de um "contém": {@code trim}, ou {@code null} quando o valor é ignorado, já escapado. */
    private static String padraoContem(String termo) {
        String limpo = textoLimpo(termo);
        return limpo == null ? null : escaparLike(limpo);
    }

    /** O predicado "contém" comum a {@link #contem(String, String)} e {@link #existeContem}: LIKE sem caixa. */
    private static Predicate predicadoContem(CriteriaBuilder cb, Expression<String> coluna, String escapado) {
        Expression<String> colunaNormalizada = normalizada(cb, coluna);
        Expression<String> valor = normalizada(cb, cb.literal(escapado));
        return cb.like(colunaNormalizada,
                cb.concat(cb.concat(cb.literal("%"), valor), cb.literal("%")), ESCAPE);
    }

    private static Expression<String> normalizada(CriteriaBuilder cb, Expression<String> expressao) {
        Expression<String> traduzida = cb.function("translate", String.class, expressao,
                cb.literal(ACENTUADAS), cb.literal(SEM_ACENTO));
        return cb.lower(traduzida);
    }

    private static String escaparLike(String valor) {
        StringBuilder escapado = new StringBuilder(valor.length());
        for (int i = 0; i < valor.length(); i++) {
            char caractere = valor.charAt(i);
            if (caractere == ESCAPE || caractere == '%' || caractere == '_') {
                escapado.append(ESCAPE);
            }
            escapado.append(caractere);
        }
        return escapado.toString();
    }

    private static String apenasDigitos(String campo, String termo) {
        String limpo = textoLimpo(termo);
        if (limpo == null) {
            return null;
        }
        StringBuilder digitos = new StringBuilder(limpo.length());
        for (int i = 0; i < limpo.length(); i++) {
            char caractere = limpo.charAt(i);
            if (caractere >= '0' && caractere <= '9') {
                digitos.append(caractere);
            }
        }
        if (digitos.isEmpty()) {
            throw new ParametroInvalido(campo,
                    "O parâmetro " + campo + " deve conter ao menos um número.");
        }
        return digitos.toString();
    }

    private static String textoLimpo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }

    private static Object valorLimpo(Object valor) {
        return valor instanceof String texto ? textoLimpo(texto) : valor;
    }

    private static List<Object> valoresLimpos(Collection<?> valores) {
        List<Object> limpos = new ArrayList<>();
        if (valores != null) {
            for (Object valor : valores) {
                Object limpo = valorLimpo(valor);
                if (limpo != null) {
                    limpos.add(limpo);
                }
            }
        }
        return limpos;
    }

    @SuppressWarnings("unchecked")
    private static <Y> Y limparIntervalo(Y valor) {
        return valor instanceof String texto ? (Y) textoLimpo(texto) : valor;
    }

    private static <T> Specification<T> semFiltro() {
        return (raiz, consulta, cb) -> cb.conjunction();
    }
}
