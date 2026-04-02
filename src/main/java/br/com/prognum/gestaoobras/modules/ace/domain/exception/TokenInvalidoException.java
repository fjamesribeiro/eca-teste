package br.com.prognum.gestaoobras.modules.ace.domain.exception;

public class TokenInvalidoException extends AceException {

    public TokenInvalidoException(String mensagem) {
        super("TOKEN_INVALIDO", mensagem);
    }

    public static TokenInvalidoException expirado() {
        return new TokenInvalidoException("Link expirado. Solicite ao Administrador um novo envio.");
    }

    public static TokenInvalidoException invalido() {
        return new TokenInvalidoException("Token de ativação inválido.");
    }
}
