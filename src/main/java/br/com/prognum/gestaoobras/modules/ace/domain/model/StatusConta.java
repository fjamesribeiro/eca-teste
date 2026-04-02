package br.com.prognum.gestaoobras.modules.ace.domain.model;

/**
 * Status do ciclo de vida da conta de usuário (RN-ACE-03, RN-ACE-04, RN-ACE-09).
 * Transições controladas pela entidade Usuario.
 */
public enum StatusConta {

    PENDENTE_ATIVACAO,
    ATIVA,
    DESATIVADA,
    BLOQUEADA
}
