package br.com.prognum.gestaoobras.modules.ace.domain.exception;

public class CodigoTotpInvalidoException extends AceException {

    public CodigoTotpInvalidoException() {
        super("V-ACE-05", "Código incorreto. Verifique seu aplicativo autenticador.");
    }
}
