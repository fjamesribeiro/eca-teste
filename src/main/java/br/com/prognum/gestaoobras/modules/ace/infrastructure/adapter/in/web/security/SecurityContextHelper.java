package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web.security;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Helper thread-local para compartilhar contexto de segurança entre o
 * EmpreendimentoSecurityFilter e os repositórios/services (ET_ACE seção 6.4).
 *
 * Armazena os empreendimentos acessíveis pelo usuário atual e o flag de visão global.
 */
public final class SecurityContextHelper {

    private static final ThreadLocal<Set<UUID>> EMPREENDIMENTOS_ACESSIVEIS =
            ThreadLocal.withInitial(Collections::emptySet);

    private static final ThreadLocal<Boolean> VISAO_GLOBAL =
            ThreadLocal.withInitial(() -> false);

    private SecurityContextHelper() {
    }

    public static void setEmpreendimentosAcessiveis(Set<UUID> ids) {
        EMPREENDIMENTOS_ACESSIVEIS.set(ids != null ? Set.copyOf(ids) : Collections.emptySet());
    }

    public static Set<UUID> getEmpreendimentosAcessiveis() {
        return EMPREENDIMENTOS_ACESSIVEIS.get();
    }

    public static void setVisaoGlobal(boolean global) {
        VISAO_GLOBAL.set(global);
    }

    public static boolean isVisaoGlobal() {
        return VISAO_GLOBAL.get();
    }

    /** Limpa o contexto ao final da requisição (chamado no filter). */
    public static void clear() {
        EMPREENDIMENTOS_ACESSIVEIS.remove();
        VISAO_GLOBAL.remove();
    }
}
