package br.com.prognum.gestaoobras.modules.ace.domain.service;

import br.com.prognum.gestaoobras.modules.ace.domain.model.MotivoEncerramento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários: GestaoSessaoService (RN-ACE-12, RN-ACE-04).
 * Naming convention: test_RN_ACE_XX_descricao (ADR-008).
 */
class GestaoSessaoServiceTest {

    private GestaoSessaoService service;
    private static final UUID USUARIO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GestaoSessaoService();
    }

    private Sessao criarSessaoAtiva(Instant criadaEm, Instant ultimaAtividade) {
        return Sessao.reconstituir(
                UUID.randomUUID(), USUARIO_ID, "hash-" + UUID.randomUUID(),
                "127.0.0.1", "Chrome", "Windows",
                criadaEm, ultimaAtividade, null, null
        );
    }

    @Nested
    @DisplayName("Limite de sessões simultâneas (RN-ACE-12)")
    class LimiteSessoes {

        @Test
        @DisplayName("test_RN_ACE_12_menos_de_2_sessoes_nao_encerra")
        void test_RN_ACE_12_menos_de_2_sessoes_nao_encerra() {
            Sessao sessao = criarSessaoAtiva(Instant.now(), Instant.now());
            List<Sessao> encerradas = service.aplicarLimiteSessoes(List.of(sessao));

            assertTrue(encerradas.isEmpty());
            assertTrue(sessao.isAtiva());
        }

        @Test
        @DisplayName("test_RN_ACE_12_zero_sessoes_retorna_vazio")
        void test_RN_ACE_12_zero_sessoes_retorna_vazio() {
            List<Sessao> encerradas = service.aplicarLimiteSessoes(List.of());
            assertTrue(encerradas.isEmpty());
        }

        @Test
        @DisplayName("test_RN_ACE_12_2_sessoes_ativas_encerra_mais_antiga")
        void test_RN_ACE_12_2_sessoes_ativas_encerra_mais_antiga() {
            Instant agora = Instant.now();
            Sessao antiga = criarSessaoAtiva(agora.minus(2, ChronoUnit.HOURS), agora);
            Sessao recente = criarSessaoAtiva(agora.minus(1, ChronoUnit.HOURS), agora);

            List<Sessao> encerradas = service.aplicarLimiteSessoes(List.of(antiga, recente));

            assertEquals(1, encerradas.size());
            assertFalse(antiga.isAtiva());
            assertEquals(MotivoEncerramento.NOVA_SESSAO, antiga.getMotivoEncerramento());
            assertTrue(recente.isAtiva());
        }

        @Test
        @DisplayName("test_RN_ACE_12_3_sessoes_ativas_encerra_2_mais_antigas")
        void test_RN_ACE_12_3_sessoes_ativas_encerra_2_mais_antigas() {
            Instant agora = Instant.now();
            Sessao s1 = criarSessaoAtiva(agora.minus(3, ChronoUnit.HOURS), agora);
            Sessao s2 = criarSessaoAtiva(agora.minus(2, ChronoUnit.HOURS), agora);
            Sessao s3 = criarSessaoAtiva(agora.minus(1, ChronoUnit.HOURS), agora);

            List<Sessao> encerradas = service.aplicarLimiteSessoes(List.of(s1, s2, s3));

            assertEquals(2, encerradas.size());
            assertFalse(s1.isAtiva());
            assertFalse(s2.isAtiva());
            assertTrue(s3.isAtiva());
        }

        @Test
        @DisplayName("test_RN_ACE_12_motivo_encerramento_NOVA_SESSAO")
        void test_RN_ACE_12_motivo_encerramento_NOVA_SESSAO() {
            Instant agora = Instant.now();
            Sessao antiga = criarSessaoAtiva(agora.minus(2, ChronoUnit.HOURS), agora);
            Sessao recente = criarSessaoAtiva(agora.minus(1, ChronoUnit.HOURS), agora);

            service.aplicarLimiteSessoes(List.of(antiga, recente));

            assertEquals(MotivoEncerramento.NOVA_SESSAO, antiga.getMotivoEncerramento());
            assertNotNull(antiga.getEncerradaEm());
        }
    }

    @Nested
    @DisplayName("Expiração por inatividade (RN-ACE-04)")
    class ExpiracaoInatividade {

        @Test
        @DisplayName("test_RN_ACE_04_sessao_dentro_timeout_nao_expirada")
        void test_RN_ACE_04_sessao_dentro_timeout_nao_expirada() {
            Sessao sessao = criarSessaoAtiva(
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().minus(10, ChronoUnit.MINUTES)
            );

            assertFalse(service.isSessaoExpirada(sessao, 480)); // 8h timeout
        }

        @Test
        @DisplayName("test_RN_ACE_04_sessao_apos_timeout_expirada")
        void test_RN_ACE_04_sessao_apos_timeout_expirada() {
            Sessao sessao = criarSessaoAtiva(
                    Instant.now().minus(10, ChronoUnit.HOURS),
                    Instant.now().minus(9, ChronoUnit.HOURS)
            );

            assertTrue(service.isSessaoExpirada(sessao, 480)); // 8h = 480 min
        }

        @Test
        @DisplayName("test_RN_ACE_04_sessao_encerrada_nao_considerada_expirada")
        void test_RN_ACE_04_sessao_encerrada_nao_considerada_expirada() {
            Sessao sessao = Sessao.reconstituir(
                    UUID.randomUUID(), USUARIO_ID, "hash",
                    "127.0.0.1", null, null,
                    Instant.now().minus(10, ChronoUnit.HOURS),
                    Instant.now().minus(9, ChronoUnit.HOURS),
                    Instant.now(), MotivoEncerramento.LOGOUT
            );

            assertFalse(service.isSessaoExpirada(sessao, 480));
        }

        @Test
        @DisplayName("test_RN_ACE_04_timeout_customizado_60_minutos")
        void test_RN_ACE_04_timeout_customizado_60_minutos() {
            Sessao sessao = criarSessaoAtiva(
                    Instant.now().minus(2, ChronoUnit.HOURS),
                    Instant.now().minus(61, ChronoUnit.MINUTES)
            );

            assertTrue(service.isSessaoExpirada(sessao, 60));
        }

        @Test
        @DisplayName("test_RN_ACE_04_expirar_sessao_seta_motivo_EXPIRADA")
        void test_RN_ACE_04_expirar_sessao_seta_motivo_EXPIRADA() {
            Sessao sessao = criarSessaoAtiva(Instant.now(), Instant.now());

            service.expirarSessao(sessao);

            assertFalse(sessao.isAtiva());
            assertEquals(MotivoEncerramento.EXPIRADA, sessao.getMotivoEncerramento());
            assertNotNull(sessao.getEncerradaEm());
        }
    }

    @Nested
    @DisplayName("Encerramento por desativação (UC-ACE-05)")
    class EncerramentoPorDesativacao {

        @Test
        @DisplayName("test_RN_ACE_09_desativacao_encerra_todas_sessoes")
        void test_RN_ACE_09_desativacao_encerra_todas_sessoes() {
            Instant agora = Instant.now();
            List<Sessao> sessoes = new ArrayList<>();
            sessoes.add(criarSessaoAtiva(agora.minus(2, ChronoUnit.HOURS), agora));
            sessoes.add(criarSessaoAtiva(agora.minus(1, ChronoUnit.HOURS), agora));

            int count = service.encerrarTodasPorDesativacao(sessoes);

            assertEquals(2, count);
            sessoes.forEach(s -> {
                assertFalse(s.isAtiva());
                assertEquals(MotivoEncerramento.DESATIVACAO, s.getMotivoEncerramento());
            });
        }

        @Test
        @DisplayName("test_RN_ACE_09_desativacao_sem_sessoes_retorna_zero")
        void test_RN_ACE_09_desativacao_sem_sessoes_retorna_zero() {
            int count = service.encerrarTodasPorDesativacao(List.of());
            assertEquals(0, count);
        }
    }
}
