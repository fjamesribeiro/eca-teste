package br.com.prognum.gestaoobras.modules.ace.domain.model;

/**
 * Provedor de identidade (RN-ACE-10, ADR-007).
 * COGNITO para usuários externos (Agente Promotor).
 * SCCI para usuários internos CDHU.
 */
public enum ProviderType {

    COGNITO,
    SCCI
}
