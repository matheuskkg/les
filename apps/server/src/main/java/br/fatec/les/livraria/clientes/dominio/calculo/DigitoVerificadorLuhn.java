package br.fatec.les.livraria.clientes.dominio.calculo;

import br.fatec.les.livraria.acesso.dominio.modelo.Telefone;

import org.springframework.stereotype.Component;

/**
 * Dígito verificador de cartão de crédito pelo algoritmo de Luhn (RN-CLI-004, DEC-18). Aceita o número com ou sem
 * máscara; a validação complementa a composição do cartão e só é aplicada depois de a bandeira ser identificada.
 */
@Component
public class DigitoVerificadorLuhn {

    /** {@code true} se a soma de verificação de Luhn confere; número nulo, vazio ou sem dígitos é inválido. */
    public boolean valido(String numero) {
        String digitos = Telefone.somenteDigitos(numero);
        if (digitos.isEmpty()) {
            return false;
        }
        int soma = 0;
        boolean dobrar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int digito = Character.digit(digitos.charAt(i), 10);
            if (dobrar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            soma += digito;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }
}
