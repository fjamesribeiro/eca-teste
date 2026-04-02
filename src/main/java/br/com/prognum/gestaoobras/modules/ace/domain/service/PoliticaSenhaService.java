package br.com.prognum.gestaoobras.modules.ace.domain.service;

import br.com.prognum.gestaoobras.modules.ace.domain.model.HistoricoSenha;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Domain service para política de senhas (RN-ACE-08).
 * - Complexidade: 12+ chars, maiúscula, minúscula, número, especial.
 * - Anti-reuso: nova senha não pode coincidir com as últimas N.
 * - Expiração: senha expira após N dias.
 *
 * Sem dependências de framework — puro domínio.
 */
public class PoliticaSenhaService {

    private static final int TAMANHO_MINIMO = 12;
    private static final Pattern MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO = Pattern.compile("[0-9]");
    private static final Pattern ESPECIAL = Pattern.compile("[^A-Za-z0-9]");

    /**
     * Valida complexidade da senha (V-ACE-03).
     * @throws SenhaInvalidaException se não atender aos requisitos.
     */
    public void validarComplexidade(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO) {
            throw new SenhaInvalidaException(
                    "Senha deve ter no mínimo " + TAMANHO_MINIMO + " caracteres.");
        }
        if (!MAIUSCULA.matcher(senha).find()) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 1 letra maiúscula.");
        }
        if (!MINUSCULA.matcher(senha).find()) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 1 letra minúscula.");
        }
        if (!NUMERO.matcher(senha).find()) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 1 número.");
        }
        if (!ESPECIAL.matcher(senha).find()) {
            throw new SenhaInvalidaException("Senha deve conter pelo menos 1 caractere especial.");
        }
    }

    /**
     * Verifica anti-reuso: a senha não pode coincidir com as últimas senhas (RN-ACE-08).
     * A comparação é feita via BCrypt match.
     *
     * @param senhaPlaintext nova senha em plaintext
     * @param historicoRecente lista dos últimos registros de histórico (hashes BCrypt)
     * @param bcryptMatcher função que compara plaintext com hash (injetada para manter domínio puro)
     * @throws SenhaReutilizadaException se a senha coincide com alguma anterior
     */
    public void validarAntiReuso(String senhaPlaintext, List<HistoricoSenha> historicoRecente,
                                  BcryptMatcher bcryptMatcher) {
        for (HistoricoSenha historico : historicoRecente) {
            if (bcryptMatcher.matches(senhaPlaintext, historico.getSenhaHash())) {
                throw new SenhaReutilizadaException("Esta senha já foi utilizada recentemente.");
            }
        }
    }

    /**
     * Verifica se a senha está expirada (V-ACE-10).
     *
     * @param ultimaTrocaSenha timestamp da última troca
     * @param diasExpiracao dias para expiração (configurável, padrão 90)
     * @return true se a senha está expirada
     */
    public boolean isSenhaExpirada(Instant ultimaTrocaSenha, int diasExpiracao) {
        if (ultimaTrocaSenha == null) {
            return true;
        }
        return Instant.now().isAfter(ultimaTrocaSenha.plus(diasExpiracao, ChronoUnit.DAYS));
    }

    /**
     * Interface funcional para comparar senha plaintext com hash BCrypt.
     * Mantém o domínio livre de dependências de BCrypt.
     */
    @FunctionalInterface
    public interface BcryptMatcher {
        boolean matches(String rawPassword, String encodedPassword);
    }

    public static class SenhaInvalidaException extends RuntimeException {
        public SenhaInvalidaException(String message) {
            super(message);
        }
    }

    public static class SenhaReutilizadaException extends RuntimeException {
        public SenhaReutilizadaException(String message) {
            super(message);
        }
    }
}
