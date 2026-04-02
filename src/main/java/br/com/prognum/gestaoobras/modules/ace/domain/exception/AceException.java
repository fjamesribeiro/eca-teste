package br.com.prognum.gestaoobras.modules.ace.domain.exception;

/**
 * Exceção base do módulo ACE.
 * Todas as exceções de domínio do módulo estendem esta classe.
 */
public abstract class AceException extends RuntimeException {

    private final String codigoErro;

    protected AceException(String codigoErro, String mensagem) {
        super(mensagem);
        this.codigoErro = codigoErro;
    }

    public String getCodigoErro() {
        return codigoErro;
    }
}
