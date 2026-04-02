package br.com.prognum.gestaoobras.modules.ace.domain.model;

import java.util.Set;

/**
 * 8 perfis fixos do sistema (RN-ACE-01).
 * Novos perfis NÃO podem ser criados — CHECK constraint no banco rejeita.
 */
public enum Perfil {

    ADMINISTRADOR,
    ANALISTA_FINANCEIRO,
    ANALISTA_FINANCEIRO_SENIOR,
    ENGENHEIRO_MEDICAO,
    DIRETOR_FINANCEIRO,
    SUPERINTENDENTE_FOMENTO,
    AGENTE_PROMOTOR,
    TESOURARIA;

    private static final Set<Perfil> PERFIS_VISAO_GLOBAL = Set.of(
            ADMINISTRADOR, SUPERINTENDENTE_FOMENTO, DIRETOR_FINANCEIRO
    );

    private static final Set<Perfil> PERFIS_VINCULACAO_OBRIGATORIA = Set.of(
            ENGENHEIRO_MEDICAO, ANALISTA_FINANCEIRO, ANALISTA_FINANCEIRO_SENIOR,
            AGENTE_PROMOTOR, TESOURARIA
    );

    /** Perfis com visão global não possuem registros em ace_vinculacao_empreendimento (RN-ACE-06). */
    public boolean possuiVisaoGlobal() {
        return PERFIS_VISAO_GLOBAL.contains(this);
    }

    /** Perfis operacionais exigem >= 1 empreendimento vinculado no cadastro (RN-ACE-06). */
    public boolean exigeVinculacao() {
        return PERFIS_VINCULACAO_OBRIGATORIA.contains(this);
    }

    /** Agente Promotor usa Cognito; todos os demais usam SCCI (RN-ACE-10). */
    public ProviderType providerTypePadrao() {
        return this == AGENTE_PROMOTOR ? ProviderType.COGNITO : ProviderType.SCCI;
    }
}
