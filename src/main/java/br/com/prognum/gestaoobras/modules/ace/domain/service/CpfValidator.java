package br.com.prognum.gestaoobras.modules.ace.domain.service;

/**
 * Validação de CPF por dígito verificador mod 11 (V-ACE-02).
 */
public final class CpfValidator {

    private CpfValidator() {
    }

    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.length() != 11 || !cpf.matches("\\d{11}")) {
            return false;
        }
        // Rejeitar CPFs com todos os dígitos iguais
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        int d1 = calcularDigito(cpf, 10);
        int d2 = calcularDigito(cpf, 11);
        return cpf.charAt(9) - '0' == d1 && cpf.charAt(10) - '0' == d2;
    }

    private static int calcularDigito(String cpf, int peso) {
        int soma = 0;
        int qtdDigitos = peso - 1;
        for (int i = 0; i < qtdDigitos; i++) {
            soma += (cpf.charAt(i) - '0') * (peso - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
