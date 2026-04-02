package br.com.prognum.gestaoobras.modules.ace.domain.exception;

import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;

public class StatusInvalidoException extends AceException {

    public StatusInvalidoException(StatusConta statusAtual, StatusConta statusEsperado) {
        super("STATUS_INVALIDO",
                "Operação requer status " + statusEsperado + ", mas o status atual é " + statusAtual + ".");
    }

    public StatusInvalidoException(String mensagem) {
        super("STATUS_INVALIDO", mensagem);
    }
}
