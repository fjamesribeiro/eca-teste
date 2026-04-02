package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ProviderType;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.RealizarLoginUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.SessaoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.service.GestaoSessaoService;
import br.com.prognum.gestaoobras.modules.ace.domain.service.PoliticaSenhaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de integração: login callback (UC-ACE-03).
 * GAP-ACE-05 RESOLVIDO: senha expirada → SenhaExpiradaException.
 */
@ExtendWith(MockitoExtension.class)
class RealizarLoginUseCaseImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private SessaoRepository sessaoRepository;
    @Mock private VinculacaoEmpreendimentoRepository vinculacaoRepository;
    @Mock private IdentityProviderPort identityProvider;
    @Mock private AuditTrailPort auditTrail;

    private RealizarLoginUseCaseImpl useCase;
    private static final UUID USUARIO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RealizarLoginUseCaseImpl(
                usuarioRepository, sessaoRepository, vinculacaoRepository,
                identityProvider, auditTrail,
                new GestaoSessaoService(), new PoliticaSenhaService(),
                90, 480);
    }

    private DadosUsuario criarUsuarioAtivo(Instant ultimaTrocaSenha) {
        return new DadosUsuario(
                USUARIO_ID, "João", "joao@test.com", "52998224725",
                Perfil.ENGENHEIRO_MEDICAO, StatusConta.ATIVA, ProviderType.SCCI,
                "access-token-ok", true, 0, null, Instant.now(), ultimaTrocaSenha,
                null, null, UUID.randomUUID(), Instant.now(), Instant.now(), null);
    }

    @Test
    @DisplayName("test_login_sucesso_cria_sessao_e_retorna_tokens")
    void test_login_sucesso_cria_sessao_e_retorna_tokens() {
        when(identityProvider.trocarCodePorTokens(any(), any()))
                .thenReturn(new IdentityProviderPort.TokenResult("access-token-ok", "refresh-123", 28800));
        when(usuarioRepository.findByProviderUserId("access-token-ok"))
                .thenReturn(Optional.of(criarUsuarioAtivo(Instant.now())));
        when(sessaoRepository.findAtivasByUsuarioId(USUARIO_ID)).thenReturn(List.of());
        when(sessaoRepository.salvar(any())).thenAnswer(i -> i.getArgument(0));
        when(vinculacaoRepository.findEmpreendimentoIdsAtivosByUsuarioId(USUARIO_ID))
                .thenReturn(Set.of(UUID.randomUUID()));

        var comando = new RealizarLoginUseCase.Comando(
                "auth-code", "http://localhost/callback", "SCCI",
                "127.0.0.1", "Chrome", "Windows");

        var resultado = useCase.executar(comando);

        assertNotNull(resultado);
        assertEquals("access-token-ok", resultado.accessToken());
        assertEquals(USUARIO_ID, resultado.usuario().id());
        verify(sessaoRepository).salvar(any(Sessao.class));
        verify(auditTrail).registrar(any());
    }

    @Test
    @DisplayName("test_V_ACE_06_conta_bloqueada_rejeita")
    void test_V_ACE_06_conta_bloqueada_rejeita() {
        var bloqueado = new DadosUsuario(
                USUARIO_ID, "João", "joao@test.com", "52998224725",
                Perfil.ENGENHEIRO_MEDICAO, StatusConta.BLOQUEADA, ProviderType.SCCI,
                "access-token-ok", true, 5,
                Instant.now().plus(30, ChronoUnit.MINUTES), // bloqueio não expirou
                null, Instant.now(), null, null,
                UUID.randomUUID(), Instant.now(), Instant.now(), null);

        when(identityProvider.trocarCodePorTokens(any(), any()))
                .thenReturn(new IdentityProviderPort.TokenResult("access-token-ok", "r", 28800));
        when(usuarioRepository.findByProviderUserId("access-token-ok"))
                .thenReturn(Optional.of(bloqueado));

        assertThrows(RealizarLoginUseCaseImpl.ContaBloqueadaException.class, () ->
                useCase.executar(new RealizarLoginUseCase.Comando(
                        "code", "uri", "SCCI", "127.0.0.1", null, null)));
    }

    @Test
    @DisplayName("test_conta_desativada_rejeita")
    void test_conta_desativada_rejeita() {
        var desativado = new DadosUsuario(
                USUARIO_ID, "João", "joao@test.com", "52998224725",
                Perfil.ENGENHEIRO_MEDICAO, StatusConta.DESATIVADA, ProviderType.SCCI,
                "access-token-ok", true, 0, null, null, Instant.now(), null, null,
                UUID.randomUUID(), Instant.now(), Instant.now(), "motivo");

        when(identityProvider.trocarCodePorTokens(any(), any()))
                .thenReturn(new IdentityProviderPort.TokenResult("access-token-ok", "r", 28800));
        when(usuarioRepository.findByProviderUserId("access-token-ok"))
                .thenReturn(Optional.of(desativado));

        assertThrows(RealizarLoginUseCaseImpl.ContaDesativadaException.class, () ->
                useCase.executar(new RealizarLoginUseCase.Comando(
                        "code", "uri", "SCCI", "127.0.0.1", null, null)));
    }

    @Test
    @DisplayName("test_V_ACE_10_senha_expirada_lanca_excecao")
    void test_V_ACE_10_senha_expirada_lanca_excecao() {
        var senhaExpirada = criarUsuarioAtivo(Instant.now().minus(91, ChronoUnit.DAYS));

        when(identityProvider.trocarCodePorTokens(any(), any()))
                .thenReturn(new IdentityProviderPort.TokenResult("access-token-ok", "r", 28800));
        when(usuarioRepository.findByProviderUserId("access-token-ok"))
                .thenReturn(Optional.of(senhaExpirada));

        assertThrows(RealizarLoginUseCaseImpl.SenhaExpiradaException.class, () ->
                useCase.executar(new RealizarLoginUseCase.Comando(
                        "code", "uri", "SCCI", "127.0.0.1", null, null)));
    }

    @Test
    @DisplayName("test_RN_ACE_12_3a_sessao_encerra_mais_antiga")
    void test_RN_ACE_12_3a_sessao_encerra_mais_antiga() {
        Instant agora = Instant.now();
        Sessao s1 = Sessao.reconstituir(UUID.randomUUID(), USUARIO_ID, "h1",
                "10.0.0.1", null, null, agora.minus(2, ChronoUnit.HOURS), agora, null, null);
        Sessao s2 = Sessao.reconstituir(UUID.randomUUID(), USUARIO_ID, "h2",
                "10.0.0.2", null, null, agora.minus(1, ChronoUnit.HOURS), agora, null, null);

        when(identityProvider.trocarCodePorTokens(any(), any()))
                .thenReturn(new IdentityProviderPort.TokenResult("access-token-ok", "r", 28800));
        when(usuarioRepository.findByProviderUserId("access-token-ok"))
                .thenReturn(Optional.of(criarUsuarioAtivo(Instant.now())));
        when(sessaoRepository.findAtivasByUsuarioId(USUARIO_ID)).thenReturn(List.of(s1, s2));
        when(sessaoRepository.salvar(any())).thenAnswer(i -> i.getArgument(0));
        when(vinculacaoRepository.findEmpreendimentoIdsAtivosByUsuarioId(USUARIO_ID))
                .thenReturn(Set.of());

        useCase.executar(new RealizarLoginUseCase.Comando(
                "code", "uri", "SCCI", "127.0.0.1", null, null));

        // s1 (mais antiga) deve ter sido encerrada + nova sessão salva
        assertFalse(s1.isAtiva());
        // 1 save para s1 encerrada + 1 save para nova sessão = pelo menos 2 saves
        verify(sessaoRepository, atLeast(2)).salvar(any(Sessao.class));
    }
}
