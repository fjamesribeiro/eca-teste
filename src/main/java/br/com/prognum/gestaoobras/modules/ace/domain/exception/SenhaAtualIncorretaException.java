package br.com.prognum.gestaoobras.modules.ace.domain.exception;

public class SenhaAtualIncorretaException extends AceException {

    public SenhaAtualIncorretaException() {
        super("SENHA_ATUAL_INCORRETA", "Senha atual incorreta.");
    }
}
