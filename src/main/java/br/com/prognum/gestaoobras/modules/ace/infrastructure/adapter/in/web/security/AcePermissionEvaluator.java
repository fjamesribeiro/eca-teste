package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web.security;

import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.PerfilPermissaoSpringDataRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Avaliador de permissões customizado para @PreAuthorize (ET_ACE seção 6.2, RN-ACE-02).
 *
 * Faz lookup na tabela ace_perfil_permissao para verificar se o perfil do
 * usuário autenticado possui permissão para o módulo/ação solicitado.
 *
 * Uso nos controllers:
 * {@code @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'CAD', 'VISUALIZAR')")}
 */
@Component("acePermissionEvaluator")
public class AcePermissionEvaluator {

    private final PerfilPermissaoSpringDataRepository perfilPermissaoRepo;

    public AcePermissionEvaluator(PerfilPermissaoSpringDataRepository perfilPermissaoRepo) {
        this.perfilPermissaoRepo = perfilPermissaoRepo;
    }

    /**
     * Verifica se o usuário autenticado possui permissão para o módulo/ação.
     *
     * @param authentication token JWT do usuário
     * @param modulo módulo do sistema (CAD, MED, CAL, APR, EXP, ACE)
     * @param acao ação solicitada (VISUALIZAR, CRIAR, EDITAR, APROVAR, EXPORTAR)
     * @return true se o perfil do usuário tem permissão
     */
    public boolean hasPermission(Authentication authentication, String modulo, String acao) {
        String perfil = extractPerfil(authentication);
        if (perfil == null) {
            return false;
        }
        return perfilPermissaoRepo.existsByPerfilAndModuloAndAcaoAndPermitidoTrue(perfil, modulo, acao);
    }

    private String extractPerfil(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        String perfil = jwt.getClaimAsString("perfil");
        if (perfil == null) {
            perfil = jwt.getClaimAsString("custom:perfil");
        }
        return perfil;
    }
}
