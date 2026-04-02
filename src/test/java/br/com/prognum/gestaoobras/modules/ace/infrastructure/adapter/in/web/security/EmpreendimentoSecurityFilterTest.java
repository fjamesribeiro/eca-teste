package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web.security;

import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes de integração: filtro de empreendimento (RN-ACE-05, RN-ACE-06).
 * Verifica que o filtro injeta corretamente os empreendimentos acessíveis
 * e o flag de visão global no SecurityContextHelper.
 */
@ExtendWith(MockitoExtension.class)
class EmpreendimentoSecurityFilterTest {

    @Mock private VinculacaoEmpreendimentoRepository vinculacaoRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private EmpreendimentoSecurityFilter filter;

    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final UUID EMP_1 = UUID.randomUUID();
    private static final UUID EMP_2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filter = new EmpreendimentoSecurityFilter(vinculacaoRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        SecurityContextHelper.clear();
    }

    private void configurarAuthentication(String perfil) {
        Jwt jwt = new Jwt(
                "token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("usuario_id", USUARIO_ID.toString(), "perfil", perfil, "sub", USUARIO_ID.toString())
        );

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("test_RN_ACE_06_administrador_visao_global_sem_filtro")
    void test_RN_ACE_06_administrador_visao_global_sem_filtro() throws Exception {
        configurarAuthentication("ADMINISTRADOR");
        when(request.getRequestURI()).thenReturn("/api/v1/medicoes");

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(SecurityContextHelper.isVisaoGlobal());
        assertTrue(SecurityContextHelper.getEmpreendimentosAcessiveis().isEmpty());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(vinculacaoRepository);
    }

    @Test
    @DisplayName("test_RN_ACE_06_superintendente_visao_global_sem_filtro")
    void test_RN_ACE_06_superintendente_visao_global_sem_filtro() throws Exception {
        configurarAuthentication("SUPERINTENDENTE_FOMENTO");
        when(request.getRequestURI()).thenReturn("/api/v1/medicoes");

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(SecurityContextHelper.isVisaoGlobal());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(vinculacaoRepository);
    }

    @Test
    @DisplayName("test_RN_ACE_06_diretor_financeiro_visao_global_sem_filtro")
    void test_RN_ACE_06_diretor_financeiro_visao_global_sem_filtro() throws Exception {
        configurarAuthentication("DIRETOR_FINANCEIRO");
        when(request.getRequestURI()).thenReturn("/api/v1/medicoes");

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(SecurityContextHelper.isVisaoGlobal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("test_RN_ACE_05_engenheiro_ve_apenas_empreendimentos_vinculados")
    void test_RN_ACE_05_engenheiro_ve_apenas_empreendimentos_vinculados() throws Exception {
        configurarAuthentication("ENGENHEIRO_MEDICAO");
        when(request.getRequestURI()).thenReturn("/api/v1/medicoes");
        when(vinculacaoRepository.findEmpreendimentoIdsAtivosByUsuarioId(USUARIO_ID))
                .thenReturn(Set.of(EMP_1, EMP_2));

        filter.doFilterInternal(request, response, filterChain);

        assertFalse(SecurityContextHelper.isVisaoGlobal());
        assertEquals(Set.of(EMP_1, EMP_2), SecurityContextHelper.getEmpreendimentosAcessiveis());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("test_RN_ACE_05_agente_promotor_ve_apenas_proprios_empreendimentos")
    void test_RN_ACE_05_agente_promotor_ve_apenas_proprios_empreendimentos() throws Exception {
        configurarAuthentication("AGENTE_PROMOTOR");
        when(request.getRequestURI()).thenReturn("/api/v1/cadastros");
        when(vinculacaoRepository.findEmpreendimentoIdsAtivosByUsuarioId(USUARIO_ID))
                .thenReturn(Set.of(EMP_1));

        filter.doFilterInternal(request, response, filterChain);

        assertFalse(SecurityContextHelper.isVisaoGlobal());
        assertEquals(Set.of(EMP_1), SecurityContextHelper.getEmpreendimentosAcessiveis());
    }

    @Test
    @DisplayName("test_RN_ACE_05_analista_financeiro_filtrado_por_vinculacao")
    void test_RN_ACE_05_analista_financeiro_filtrado_por_vinculacao() throws Exception {
        configurarAuthentication("ANALISTA_FINANCEIRO");
        when(request.getRequestURI()).thenReturn("/api/v1/calculos");
        when(vinculacaoRepository.findEmpreendimentoIdsAtivosByUsuarioId(USUARIO_ID))
                .thenReturn(Set.of(EMP_1, EMP_2));

        filter.doFilterInternal(request, response, filterChain);

        assertFalse(SecurityContextHelper.isVisaoGlobal());
        assertEquals(2, SecurityContextHelper.getEmpreendimentosAcessiveis().size());
    }

    @Test
    @DisplayName("test_filtro_limpa_contexto_apos_execucao")
    void test_filtro_limpa_contexto_apos_execucao() throws Exception {
        configurarAuthentication("ADMINISTRADOR");
        when(request.getRequestURI()).thenReturn("/api/v1/medicoes");

        // Simular que o filterChain lê o contexto
        doAnswer(invocation -> {
            assertTrue(SecurityContextHelper.isVisaoGlobal());
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        // Após o filter, contexto deve estar limpo
        assertFalse(SecurityContextHelper.isVisaoGlobal());
        assertTrue(SecurityContextHelper.getEmpreendimentosAcessiveis().isEmpty());
    }

    @Test
    @DisplayName("test_endpoints_auth_nao_filtrados")
    void test_endpoints_auth_nao_filtrados() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/ativar-conta");

        // shouldNotFilter retorna true para /api/v1/auth/**
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    @DisplayName("test_sem_autenticacao_nao_injeta_contexto")
    void test_sem_autenticacao_nao_injeta_contexto() throws Exception {
        SecurityContextHolder.clearContext();
        when(request.getRequestURI()).thenReturn("/api/v1/medicoes");

        filter.doFilterInternal(request, response, filterChain);

        assertFalse(SecurityContextHelper.isVisaoGlobal());
        assertTrue(SecurityContextHelper.getEmpreendimentosAcessiveis().isEmpty());
        verify(filterChain).doFilter(request, response);
    }
}
