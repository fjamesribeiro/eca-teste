package br.com.prognum.gestaoobras.modules.ace.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários: máquina de estado Usuario (ET_ACE seção 5.1).
 * GAP-ACE-01 RESOLVIDO: 1 perfil por conta.
 */
class UsuarioTest {

    private Usuario criarUsuarioPendente() {
        return Usuario.criar("João Silva", "joao@test.com", "52998224725",
                Perfil.ENGENHEIRO_MEDICAO, "token-123",
                Instant.now().plus(72, ChronoUnit.HOURS), UUID.randomUUID());
    }

    private Usuario criarUsuarioAtivo() {
        var u = Usuario.reconstituir(
                UUID.randomUUID(), "João", "joao@test.com", "52998224725",
                Perfil.ENGENHEIRO_MEDICAO, StatusConta.ATIVA, ProviderType.SCCI,
                "scci-123", true, 0, null, Instant.now(), Instant.now(),
                null, null, UUID.randomUUID(), Instant.now(), Instant.now(), null);
        return u;
    }

    @Nested
    @DisplayName("Criação (UC-ACE-01)")
    class Criacao {

        @Test
        @DisplayName("test_RN_ACE_03_novo_usuario_status_pendente")
        void test_RN_ACE_03_novo_usuario_status_pendente() {
            var usuario = criarUsuarioPendente();

            assertEquals(StatusConta.PENDENTE_ATIVACAO, usuario.getStatusConta());
            assertFalse(usuario.isMfaConfigurado());
            assertEquals(0, usuario.getTentativasLoginFalhas());
            assertNotNull(usuario.getTokenAtivacao());
        }

        @Test
        @DisplayName("test_RN_ACE_10_provider_derivado_do_perfil")
        void test_RN_ACE_10_provider_derivado_do_perfil() {
            var engenheiro = Usuario.criar("Eng", "eng@cdhu.sp.gov.br", "52998224725",
                    Perfil.ENGENHEIRO_MEDICAO, "t", Instant.now(), UUID.randomUUID());
            assertEquals(ProviderType.SCCI, engenheiro.getProviderType());

            var ap = Usuario.criar("AP", "ap@external.com", "52998224725",
                    Perfil.AGENTE_PROMOTOR, "t", Instant.now(), UUID.randomUUID());
            assertEquals(ProviderType.COGNITO, ap.getProviderType());
        }
    }

    @Nested
    @DisplayName("Transições válidas (ET_ACE seção 5.1)")
    class TransicoesValidas {

        @Test
        @DisplayName("test_PENDENTE_para_ATIVA")
        void test_PENDENTE_para_ATIVA() {
            var usuario = criarUsuarioPendente();
            usuario.ativar();

            assertEquals(StatusConta.ATIVA, usuario.getStatusConta());
            assertTrue(usuario.isMfaConfigurado());
            assertNull(usuario.getTokenAtivacao());
        }

        @Test
        @DisplayName("test_ATIVA_para_BLOQUEADA")
        void test_ATIVA_para_BLOQUEADA() {
            var usuario = criarUsuarioAtivo();
            usuario.bloquear(30);

            assertEquals(StatusConta.BLOQUEADA, usuario.getStatusConta());
            assertNotNull(usuario.getBloqueadoAte());
        }

        @Test
        @DisplayName("test_BLOQUEADA_para_ATIVA_desbloqueio")
        void test_BLOQUEADA_para_ATIVA_desbloqueio() {
            var usuario = Usuario.reconstituir(
                    UUID.randomUUID(), "João", "joao@test.com", "52998224725",
                    Perfil.ENGENHEIRO_MEDICAO, StatusConta.BLOQUEADA, ProviderType.SCCI,
                    "scci-123", true, 5, Instant.now().plus(30, ChronoUnit.MINUTES),
                    null, null, null, null, UUID.randomUUID(), Instant.now(), Instant.now(), null);

            usuario.desbloquear();

            assertEquals(StatusConta.ATIVA, usuario.getStatusConta());
            assertEquals(0, usuario.getTentativasLoginFalhas());
            assertNull(usuario.getBloqueadoAte());
        }

        @Test
        @DisplayName("test_ATIVA_para_DESATIVADA")
        void test_ATIVA_para_DESATIVADA() {
            var usuario = criarUsuarioAtivo();
            usuario.desativar("Solicitação do gestor");

            assertEquals(StatusConta.DESATIVADA, usuario.getStatusConta());
            assertEquals("Solicitação do gestor", usuario.getMotivoDesativacao());
        }

        @Test
        @DisplayName("test_BLOQUEADA_para_DESATIVADA")
        void test_BLOQUEADA_para_DESATIVADA() {
            var usuario = Usuario.reconstituir(
                    UUID.randomUUID(), "João", "joao@test.com", "52998224725",
                    Perfil.ENGENHEIRO_MEDICAO, StatusConta.BLOQUEADA, ProviderType.SCCI,
                    null, true, 5, null, null, null, null, null,
                    UUID.randomUUID(), Instant.now(), Instant.now(), null);

            usuario.desativar(null);
            assertEquals(StatusConta.DESATIVADA, usuario.getStatusConta());
        }

        @Test
        @DisplayName("test_DESATIVADA_para_ATIVA_reativacao")
        void test_DESATIVADA_para_ATIVA_reativacao() {
            var usuario = Usuario.reconstituir(
                    UUID.randomUUID(), "João", "joao@test.com", "52998224725",
                    Perfil.ENGENHEIRO_MEDICAO, StatusConta.DESATIVADA, ProviderType.SCCI,
                    null, true, 0, null, null, null, null, null,
                    UUID.randomUUID(), Instant.now(), Instant.now(), "motivo antigo");

            usuario.reativar();

            assertEquals(StatusConta.ATIVA, usuario.getStatusConta());
            assertNull(usuario.getMotivoDesativacao());
        }
    }

    @Nested
    @DisplayName("Transições inválidas")
    class TransicoesInvalidas {

        @Test
        @DisplayName("test_ATIVA_para_ATIVA_ativar_rejeita")
        void test_ATIVA_para_ATIVA_ativar_rejeita() {
            var usuario = criarUsuarioAtivo();
            assertThrows(Usuario.TransicaoInvalidaException.class, usuario::ativar);
        }

        @Test
        @DisplayName("test_PENDENTE_para_BLOQUEADA_rejeita")
        void test_PENDENTE_para_BLOQUEADA_rejeita() {
            var usuario = criarUsuarioPendente();
            assertThrows(Usuario.TransicaoInvalidaException.class, () -> usuario.bloquear(30));
        }

        @Test
        @DisplayName("test_PENDENTE_para_DESATIVADA_rejeita")
        void test_PENDENTE_para_DESATIVADA_rejeita() {
            var usuario = criarUsuarioPendente();
            assertThrows(Usuario.TransicaoInvalidaException.class, () -> usuario.desativar(null));
        }

        @Test
        @DisplayName("test_DESATIVADA_para_BLOQUEADA_rejeita")
        void test_DESATIVADA_para_BLOQUEADA_rejeita() {
            var usuario = Usuario.reconstituir(
                    UUID.randomUUID(), "João", "joao@test.com", "52998224725",
                    Perfil.ENGENHEIRO_MEDICAO, StatusConta.DESATIVADA, ProviderType.SCCI,
                    null, true, 0, null, null, null, null, null,
                    UUID.randomUUID(), Instant.now(), Instant.now(), null);

            assertThrows(Usuario.TransicaoInvalidaException.class, () -> usuario.bloquear(30));
        }

        @Test
        @DisplayName("test_ATIVA_para_PENDENTE_rejeita")
        void test_ATIVA_para_PENDENTE_rejeita() {
            var usuario = criarUsuarioAtivo();
            // Não existe transição para PENDENTE — não há método para isso
            assertThrows(Usuario.TransicaoInvalidaException.class,
                    () -> usuario.reenviarAtivacao("t", Instant.now()));
        }
    }

    @Nested
    @DisplayName("Comportamentos")
    class Comportamentos {

        @Test
        @DisplayName("test_RN_ACE_04_registrar_5_falhas_atinge_limite")
        void test_RN_ACE_04_registrar_5_falhas_atinge_limite() {
            var usuario = criarUsuarioAtivo();
            for (int i = 0; i < 4; i++) {
                assertFalse(usuario.registrarFalhaLogin(5));
            }
            assertTrue(usuario.registrarFalhaLogin(5));
            assertEquals(5, usuario.getTentativasLoginFalhas());
        }

        @Test
        @DisplayName("test_login_sucesso_reseta_contador")
        void test_login_sucesso_reseta_contador() {
            var usuario = criarUsuarioAtivo();
            usuario.registrarFalhaLogin(5);
            usuario.registrarFalhaLogin(5);
            assertEquals(2, usuario.getTentativasLoginFalhas());

            usuario.registrarLoginSucesso();
            assertEquals(0, usuario.getTentativasLoginFalhas());
            assertNotNull(usuario.getUltimoAcesso());
        }

        @Test
        @DisplayName("test_bloqueio_expirado_retorna_true")
        void test_bloqueio_expirado_retorna_true() {
            var usuario = Usuario.reconstituir(
                    UUID.randomUUID(), "João", "joao@test.com", "52998224725",
                    Perfil.ENGENHEIRO_MEDICAO, StatusConta.BLOQUEADA, ProviderType.SCCI,
                    null, true, 5, Instant.now().minus(1, ChronoUnit.MINUTES),
                    null, null, null, null, UUID.randomUUID(), Instant.now(), Instant.now(), null);

            assertTrue(usuario.isBloqueioExpirado());
        }

        @Test
        @DisplayName("test_alterar_perfil_atualiza_provider")
        void test_alterar_perfil_atualiza_provider() {
            var usuario = criarUsuarioAtivo(); // ENGENHEIRO → SCCI
            usuario.alterarPerfil(Perfil.AGENTE_PROMOTOR);

            assertEquals(Perfil.AGENTE_PROMOTOR, usuario.getPerfil());
            assertEquals(ProviderType.COGNITO, usuario.getProviderType());
        }
    }
}
