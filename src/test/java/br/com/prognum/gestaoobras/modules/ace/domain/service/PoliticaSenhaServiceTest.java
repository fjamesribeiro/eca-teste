package br.com.prognum.gestaoobras.modules.ace.domain.service;

import br.com.prognum.gestaoobras.modules.ace.domain.model.HistoricoSenha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários: PoliticaSenhaService (RN-ACE-08, V-ACE-03, V-ACE-10).
 * Naming convention: test_RN_ACE_XX_descricao (ADR-008).
 */
class PoliticaSenhaServiceTest {

    private PoliticaSenhaService service;

    @BeforeEach
    void setUp() {
        service = new PoliticaSenhaService();
    }

    @Nested
    @DisplayName("Validação de complexidade (V-ACE-03)")
    class Complexidade {

        @Test
        @DisplayName("test_V_ACE_03_senha_valida_aceita")
        void test_V_ACE_03_senha_valida_aceita() {
            assertDoesNotThrow(() -> service.validarComplexidade("Senh@Forte123!"));
        }

        @Test
        @DisplayName("test_V_ACE_03_senha_12_caracteres_minimo_aceita")
        void test_V_ACE_03_senha_12_caracteres_minimo_aceita() {
            assertDoesNotThrow(() -> service.validarComplexidade("Abcdefgh1!ab"));
        }

        @Test
        @DisplayName("test_V_ACE_03_senha_null_rejeita")
        void test_V_ACE_03_senha_null_rejeita() {
            var ex = assertThrows(PoliticaSenhaService.SenhaInvalidaException.class,
                    () -> service.validarComplexidade(null));
            assertTrue(ex.getMessage().contains("12"));
        }

        @Test
        @DisplayName("test_V_ACE_03_senha_curta_rejeita")
        void test_V_ACE_03_senha_curta_rejeita() {
            var ex = assertThrows(PoliticaSenhaService.SenhaInvalidaException.class,
                    () -> service.validarComplexidade("Abc1!short"));
            assertTrue(ex.getMessage().contains("12"));
        }

        @Test
        @DisplayName("test_V_ACE_03_sem_maiuscula_rejeita")
        void test_V_ACE_03_sem_maiuscula_rejeita() {
            var ex = assertThrows(PoliticaSenhaService.SenhaInvalidaException.class,
                    () -> service.validarComplexidade("senhaforte123!"));
            assertTrue(ex.getMessage().contains("maiúscula"));
        }

        @Test
        @DisplayName("test_V_ACE_03_sem_minuscula_rejeita")
        void test_V_ACE_03_sem_minuscula_rejeita() {
            var ex = assertThrows(PoliticaSenhaService.SenhaInvalidaException.class,
                    () -> service.validarComplexidade("SENHAFORTE123!"));
            assertTrue(ex.getMessage().contains("minúscula"));
        }

        @Test
        @DisplayName("test_V_ACE_03_sem_numero_rejeita")
        void test_V_ACE_03_sem_numero_rejeita() {
            var ex = assertThrows(PoliticaSenhaService.SenhaInvalidaException.class,
                    () -> service.validarComplexidade("SenhaForte!!ab"));
            assertTrue(ex.getMessage().contains("número"));
        }

        @Test
        @DisplayName("test_V_ACE_03_sem_especial_rejeita")
        void test_V_ACE_03_sem_especial_rejeita() {
            var ex = assertThrows(PoliticaSenhaService.SenhaInvalidaException.class,
                    () -> service.validarComplexidade("SenhaForte123a"));
            assertTrue(ex.getMessage().contains("especial"));
        }

        @Test
        @DisplayName("test_V_ACE_03_senha_vazia_rejeita")
        void test_V_ACE_03_senha_vazia_rejeita() {
            assertThrows(PoliticaSenhaService.SenhaInvalidaException.class,
                    () -> service.validarComplexidade(""));
        }
    }

    @Nested
    @DisplayName("Anti-reuso de senhas (RN-ACE-08)")
    class AntiReuso {

        private final PoliticaSenhaService.BcryptMatcher matcherSimulado =
                (raw, encoded) -> raw.equals(encoded);

        @Test
        @DisplayName("test_RN_ACE_08_senha_nova_aceita")
        void test_RN_ACE_08_senha_nova_aceita() {
            List<HistoricoSenha> historico = List.of(
                    HistoricoSenha.reconstituir(1L, UUID.randomUUID(), "senhaAntiga1", Instant.now()),
                    HistoricoSenha.reconstituir(2L, UUID.randomUUID(), "senhaAntiga2", Instant.now())
            );

            assertDoesNotThrow(() ->
                    service.validarAntiReuso("senhaTotalmenteNova", historico, matcherSimulado));
        }

        @Test
        @DisplayName("test_RN_ACE_08_senha_reutilizada_rejeita")
        void test_RN_ACE_08_senha_reutilizada_rejeita() {
            List<HistoricoSenha> historico = List.of(
                    HistoricoSenha.reconstituir(1L, UUID.randomUUID(), "senhaReutilizada", Instant.now()),
                    HistoricoSenha.reconstituir(2L, UUID.randomUUID(), "outraSenha", Instant.now())
            );

            assertThrows(PoliticaSenhaService.SenhaReutilizadaException.class,
                    () -> service.validarAntiReuso("senhaReutilizada", historico, matcherSimulado));
        }

        @Test
        @DisplayName("test_RN_ACE_08_historico_vazio_aceita")
        void test_RN_ACE_08_historico_vazio_aceita() {
            assertDoesNotThrow(() ->
                    service.validarAntiReuso("qualquerSenha", List.of(), matcherSimulado));
        }

        @Test
        @DisplayName("test_RN_ACE_08_reutiliza_ultima_das_5_rejeita")
        void test_RN_ACE_08_reutiliza_ultima_das_5_rejeita() {
            UUID userId = UUID.randomUUID();
            List<HistoricoSenha> historico = List.of(
                    HistoricoSenha.reconstituir(1L, userId, "senha1", Instant.now()),
                    HistoricoSenha.reconstituir(2L, userId, "senha2", Instant.now()),
                    HistoricoSenha.reconstituir(3L, userId, "senha3", Instant.now()),
                    HistoricoSenha.reconstituir(4L, userId, "senha4", Instant.now()),
                    HistoricoSenha.reconstituir(5L, userId, "senha5", Instant.now())
            );

            assertThrows(PoliticaSenhaService.SenhaReutilizadaException.class,
                    () -> service.validarAntiReuso("senha5", historico, matcherSimulado));
        }
    }

    @Nested
    @DisplayName("Expiração de senha (V-ACE-10)")
    class Expiracao {

        @Test
        @DisplayName("test_V_ACE_10_senha_dentro_do_prazo_nao_expirada")
        void test_V_ACE_10_senha_dentro_do_prazo_nao_expirada() {
            Instant trocaRecente = Instant.now().minus(30, ChronoUnit.DAYS);
            assertFalse(service.isSenhaExpirada(trocaRecente, 90));
        }

        @Test
        @DisplayName("test_V_ACE_10_senha_90_dias_expirada")
        void test_V_ACE_10_senha_90_dias_expirada() {
            Instant troca91DiasAtras = Instant.now().minus(91, ChronoUnit.DAYS);
            assertTrue(service.isSenhaExpirada(troca91DiasAtras, 90));
        }

        @Test
        @DisplayName("test_V_ACE_10_senha_null_expirada")
        void test_V_ACE_10_senha_null_expirada() {
            assertTrue(service.isSenhaExpirada(null, 90));
        }

        @Test
        @DisplayName("test_V_ACE_10_senha_exatamente_90_dias_nao_expirada")
        void test_V_ACE_10_senha_exatamente_90_dias_nao_expirada() {
            // Exatamente 90 dias — ainda não expirou (precisa passar de 90)
            Instant troca90Dias = Instant.now().minus(90, ChronoUnit.DAYS).plus(1, ChronoUnit.MINUTES);
            assertFalse(service.isSenhaExpirada(troca90Dias, 90));
        }

        @Test
        @DisplayName("test_V_ACE_10_parametro_customizado_expiracao")
        void test_V_ACE_10_parametro_customizado_expiracao() {
            Instant troca31DiasAtras = Instant.now().minus(31, ChronoUnit.DAYS);
            assertTrue(service.isSenhaExpirada(troca31DiasAtras, 30));
            assertFalse(service.isSenhaExpirada(troca31DiasAtras, 60));
        }
    }
}
