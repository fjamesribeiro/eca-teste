package br.com.prognum.gestaoobras.modules.ace.domain.exception;

import java.util.UUID;

public class UsuarioNaoEncontradoException extends AceException {

    public UsuarioNaoEncontradoException(UUID id) {
        super("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado: " + id);
    }
}
