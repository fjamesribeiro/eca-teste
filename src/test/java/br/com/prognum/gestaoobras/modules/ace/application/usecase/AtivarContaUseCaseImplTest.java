package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.CodigoTotpInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.SenhasDiferentesException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.StatusInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.TokenInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.HistoricoSenha;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ProviderType;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.AtivarContaUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.HistoricoSenhaRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;
import br.com.prognum.gestaoobras.modules.ace.domain.service.PoliticaSenhaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes de integração: ativação de conta e2e (UC-ACE-02).
 * Usa mocks para IdP, repositórios e audit trail.
 */
@ExtendWith(MockitoExtension.class)
class AtivarContaUseCaseImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private IdentityProviderPort identityProvider;
    @Mock private HistoricoSenhaRepository historicoSenhaRepository;
    @Mock private AuditTrailPort auditTrail;

    private PoliticaSenhaService politicaSenhaService;
    private AtivarContaUseCaseImpl useCase;

    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final String TOKEN = "token-ativacao-valido";
    private static final String PROVIDER_USER_ID = "cognito-user-123";
    private static final String SENHA_VALIDA = "Senh@Forte123!";

    @BeforeEach
    void setUp() {
        politicaSenhaService = new PoliticaSenhaService();
        useCase = new AtivarContaUseCaseImpl(
                usuarioRepository, identityProvider, historicoSenhaRepository,
                auditTrail, politicaSenhaService,
                (raw, encoded) -> raw.equals(encoded),
                raw -> "hashed-" + raw
        );
    }

    private DadosUsuario criarUsuarioPendente() {
        return new DadosUsuario(
                USUARIO_ID, "João Silva", "joao@test.com", "12345678901",
                Perfil.AGENTE_PROMOTOR, StatusConta.PENDENTE_ATIVACAO, ProviderType.COGNITO,
                PROVIDER_USER_ID, false, 0, null, null, null,
                TOKEN, Instant.now().plus(24, ChronoUnit.HOURS),
                UUID.randomUUID(), Instant.now(), Instant.now(), null
        );
    }

    @Nested
    @DisplayName("definirSenha — UC-ACE-02 passos 1-7")
    class DefinirSenha {

        @Test
        @DisplayName("test_RN_ACE_03_ativacao_sucesso_retorna_mfa_setup")
        void test_RN_ACE_03_ativacao_sucesso_retorna_mfa_setup() {
            when(usuarioRepository.findByTokenAtivacao(TOKEN))
                    .thenReturn(Optional.of(criarUsuarioPendente()));
            when(historicoSenhaRepository.findUltimasByUsuarioId(eq(USUARIO_ID), anyInt()))
                    .thenReturn(List.of());
            when(identityProvider.iniciarSetupMfa(PROVIDER_USER_ID))
                    .thenReturn(new IdentityProviderPort.MfaSetupResult("otpauth://...", "SECRET123"));

            var resultado = useCase.definirSenha(
                    new AtivarContaUseCase.ComandoDefinirSenha(TOKEN, SENHA_VALIDA, SENHA_VALIDA));

            assertNotNull(resultado);
            assertEquals("otpauth://...", resultado.qrCodeUri());
            assertEquals("SECRET123", resultado.secret());

            verify(identityProvider).definirSenha(PROVIDER_USER_ID, SENHA_VALIDA);
            verify(historicoSenhaRepository).salvar(any(HistoricoSenha.class));
            verify(usuarioRepository).atualizarUltimaTrocaSenha(eq(USUARIO_ID), any());
        }

        @Test
        @DisplayName("test_V_ACE_04_token_invalido_rejeita")
        void test_V_ACE_04_token_invalido_rejeita() {
            when(usuarioRepository.findByTokenAtivacao("token-inexistente"))
                    .thenReturn(Optional.empty());

            assertThrows(TokenInvalidoException.class, () ->
                    useCase.definirSenha(new AtivarContaUseCase.ComandoDefinirSenha(
                            "token-inexistente", SENHA_VALIDA, SENHA_VALIDA)));
        }

        @Test
        @DisplayName("test_V_ACE_04_token_expirado_rejeita")
        void test_V_ACE_04_token_expirado_rejeita() {
            var usuario = new DadosUsuario(
                    USUARIO_ID, "João", "joao@test.com", "12345678901",
                    Perfil.AGENTE_PROMOTOR, StatusConta.PENDENTE_ATIVACAO, ProviderType.COGNITO,
                    PROVIDER_USER_ID, false, 0, null, null, null,
                    TOKEN, Instant.now().minus(1, ChronoUnit.HOURS), // expirado
                    UUID.randomUUID(), Instant.now(), Instant.now(), null
            );
            when(usuarioRepository.findByTokenAtivacao(TOKEN)).thenReturn(Optional.of(usuario));

            assertThrows(TokenInvalidoException.class, () ->
                    useCase.definirSenha(new AtivarContaUseCase.ComandoDefinirSenha(
                            TOKEN, SENHA_VALIDA, SENHA_VALIDA)));
        }

        @Test
        @DisplayName("test_RN_ACE_03_status_nao_pendente_rejeita")
        void test_RN_ACE_03_status_nao_pendente_rejeita() {
            var usuario = new DadosUsuario(
                    USUARIO_ID, "João", "joao@test.com", "12345678901",
                    Perfil.AGENTE_PROMOTOR, StatusConta.ATIVA, ProviderType.COGNITO,
                    PROVIDER_USER_ID, true, 0, null, null, null,
                    TOKEN, Instant.now().plus(24, ChronoUnit.HOURS),
                    UUID.randomUUID(), Instant.now(), Instant.now(), null
            );
            when(usuarioRepository.findByTokenAtivacao(TOKEN)).thenReturn(Optional.of(usuario));

            assertThrows(StatusInvalidoException.class, () ->
                    useCase.definirSenha(new AtivarContaUseCase.ComandoDefinirSenha(
                            TOKEN, SENHA_VALIDA, SENHA_VALIDA)));
        }

        @Test
        @DisplayName("test_senhas_diferentes_rejeita")
        void test_senhas_diferentes_rejeita() {
            when(usuarioRepository.findByTokenAtivacao(TOKEN))
                    .thenReturn(Optional.of(criarUsuarioPendente()));

            assertThrows(SenhasDiferentesException.class, () ->
                    useCase.definirSenha(new AtivarContaUseCase.ComandoDefinirSenha(
                            TOKEN, SENHA_VALIDA, "SenhaDiferente1!")));
        }

        @Test
        @DisplayName("test_V_ACE_03_senha_fraca_rejeita")
        void test_V_ACE_03_senha_fraca_rejeita() {
            when(usuarioRepository.findByTokenAtivacao(TOKEN))
                    .thenReturn(Optional.of(criarUsuarioPendente()));

            assertThrows(PoliticaSenhaService.SenhaInvalidaException.class, () ->
                    useCase.definirSenha(new AtivarContaUseCase.ComandoDefinirSenha(
                            TOKEN, "fraca", "fraca")));
        }

        @Test
        @DisplayName("test_RN_ACE_08_senha_reutilizada_rejeita")
        void test_RN_ACE_08_senha_reutilizada_rejeita() {
            when(usuarioRepository.findByTokenAtivacao(TOKEN))
                    .thenReturn(Optional.of(criarUsuarioPendente()));
            when(historicoSenhaRepository.findUltimasByUsuarioId(eq(USUARIO_ID), anyInt()))
                    .thenReturn(List.of(
                            HistoricoSenha.reconstituir(1L, USUARIO_ID, SENHA_VALIDA, Instant.now())
                    ));

            assertThrows(PoliticaSenhaService.SenhaReutilizadaException.class, () ->
                    useCase.definirSenha(new AtivarContaUseCase.ComandoDefinirSenha(
                            TOKEN, SENHA_VALIDA, SENHA_VALIDA)));
        }
    }

    @Nested
    @DisplayName("confirmarMfa — UC-ACE-02 passos 8-9")
    class ConfirmarMfa {

        @Test
        @DisplayName("test_RN_ACE_04_mfa_confirmado_ativa_conta")
        void test_RN_ACE_04_mfa_confirmado_ativa_conta() {
            when(usuarioRepository.findByTokenAtivacao(TOKEN))
                    .thenReturn(Optional.of(criarUsuarioPendente()));
            when(identityProvider.confirmarMfa(PROVIDER_USER_ID, "123456"))
                    .thenReturn(true);

            var resultado = useCase.confirmarMfa(
                    new AtivarContaUseCase.ComandoConfirmarMfa(TOKEN, "123456"));

            assertEquals("ATIVA", resultado.status());
            verify(usuarioRepository).atualizarMfaConfigurado(USUARIO_ID, true);
            verify(usuarioRepository).atualizarStatusConta(USUARIO_ID, StatusConta.ATIVA);
            verify(usuarioRepository).atualizarTokenAtivacao(USUARIO_ID, null, null);
            verify(auditTrail).registrar(any(EventoAcesso.class));
        }

        @Test
        @DisplayName("test_V_ACE_05_totp_invalido_rejeita")
        void test_V_ACE_05_totp_invalido_rejeita() {
            when(usuarioRepository.findByTokenAtivacao(TOKEN))
                    .thenReturn(Optional.of(criarUsuarioPendente()));
            when(identityProvider.confirmarMfa(PROVIDER_USER_ID, "000000"))
                    .thenReturn(false);

            assertThrows(CodigoTotpInvalidoException.class, () ->
                    useCase.confirmarMfa(
                            new AtivarContaUseCase.ComandoConfirmarMfa(TOKEN, "000000")));
        }
    }
}
