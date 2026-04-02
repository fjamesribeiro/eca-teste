package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web.security;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Filtro de segurança que injeta os empreendimentos acessíveis pelo usuário
 * autenticado no SecurityContextHelper (ET_ACE seção 6.4, RN-ACE-05, RN-ACE-06).
 *
 * Perfis com visão global (ADMINISTRADOR, SUPERINTENDENTE_FOMENTO, DIRETOR_FINANCEIRO)
 * não possuem registros em ace_vinculacao_empreendimento — tratados como exceção.
 *
 * Este filtro deve ser posicionado APÓS a autenticação JWT e ANTES dos filtros de autorização.
 */
@Component
public class EmpreendimentoSecurityFilter extends OncePerRequestFilter {

    private final VinculacaoEmpreendimentoRepository vinculacaoRepository;

    public EmpreendimentoSecurityFilter(VinculacaoEmpreendimentoRepository vinculacaoRepository) {
        this.vinculacaoRepository = vinculacaoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof Jwt jwt) {

                UUID usuarioId = extractUsuarioId(jwt);
                Perfil perfil = extractPerfil(jwt);

                if (perfil != null && perfil.possuiVisaoGlobal()) {
                    SecurityContextHelper.setVisaoGlobal(true);
                    SecurityContextHelper.setEmpreendimentosAcessiveis(Set.of());
                } else if (usuarioId != null) {
                    SecurityContextHelper.setVisaoGlobal(false);
                    Set<UUID> empreendimentos = vinculacaoRepository
                            .findEmpreendimentoIdsAtivosByUsuarioId(usuarioId);
                    SecurityContextHelper.setEmpreendimentosAcessiveis(empreendimentos);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHelper.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Endpoints públicos não precisam de filtro de empreendimento
        return path.startsWith("/api/v1/auth/");
    }

    private UUID extractUsuarioId(Jwt jwt) {
        String sub = jwt.getClaimAsString("usuario_id");
        if (sub == null) {
            sub = jwt.getSubject();
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Perfil extractPerfil(Jwt jwt) {
        String perfilStr = jwt.getClaimAsString("perfil");
        if (perfilStr == null) {
            perfilStr = jwt.getClaimAsString("custom:perfil");
        }
        try {
            return perfilStr != null ? Perfil.valueOf(perfilStr) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
