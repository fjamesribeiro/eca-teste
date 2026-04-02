package br.com.prognum.gestaoobras.modules.ace.domain.model;

/**
 * Tipos de evento registrados na trilha de auditoria (RN-ACE-07).
 */
public enum TipoEventoAcesso {

    LOGIN,
    LOGOUT,
    FALHA_LOGIN,
    BLOQUEIO_CONTA,
    DESBLOQUEIO_CONTA,
    RESET_SENHA,
    TROCA_SENHA,
    ALTERACAO_PERFIL,
    ATIVACAO_CONTA,
    DESATIVACAO_CONTA,
    REATIVACAO_CONTA,
    CRIACAO_USUARIO,
    ALTERACAO_VINCULACAO,
    ENCERRAMENTO_SESSAO,
    CONFIGURACAO_MFA
}
