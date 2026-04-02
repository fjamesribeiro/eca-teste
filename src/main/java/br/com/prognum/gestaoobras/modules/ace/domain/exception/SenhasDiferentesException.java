package br.com.prognum.gestaoobras.modules.ace.domain.exception;

public class SenhasDiferentesException extends AceException {

    public SenhasDiferentesException() {
        super("SENHAS_DIFERENTES", "Senhas não conferem.");
    }
}
