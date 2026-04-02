package br.com.prognum.gestaoobras.modules.ace.domain.exception;

import java.util.UUID;

public class SessaoNaoEncontradaException extends AceException {

    public SessaoNaoEncontradaException(UUID sessaoId) {
        super("SESSAO_NAO_ENCONTRADA", "Sessão não encontrada: " + sessaoId);
    }
}
