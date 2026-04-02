package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração: trilha imutável (RN-AUD-01).
 * Verifica que os triggers rejeitam UPDATE e DELETE em ace_evento_acesso.
 *
 * Requer Testcontainers com PostgreSQL + migrations Flyway aplicadas.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditTrailImutavelTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ace_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("test_RN_AUD_01_insert_evento_acesso_sucesso")
    void test_RN_AUD_01_insert_evento_acesso_sucesso() {
        // Primeiro, criar um usuario bootstrap para FK
        jdbcTemplate.execute("""
                INSERT INTO ace_usuario (id, nome_completo, email, cpf, perfil, status_conta, provider_type, criado_por)
                VALUES ('00000000-0000-0000-0000-000000000001', 'Admin Bootstrap', 'admin@cdhu.sp.gov.br',
                        '00000000001', 'ADMINISTRADOR', 'ATIVA', 'SCCI', '00000000-0000-0000-0000-000000000001')
                ON CONFLICT DO NOTHING
                """);

        // INSERT deve funcionar
        assertDoesNotThrow(() -> jdbcTemplate.execute("""
                INSERT INTO ace_evento_acesso (usuario_id, tipo_evento, resultado, ip_origem)
                VALUES ('00000000-0000-0000-0000-000000000001', 'LOGIN', 'SUCESSO', '127.0.0.1')
                """));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ace_evento_acesso WHERE tipo_evento = 'LOGIN'", Integer.class);
        assertTrue(count != null && count > 0);
    }

    @Test
    @DisplayName("test_RN_AUD_01_update_evento_acesso_rejeitado_por_trigger")
    void test_RN_AUD_01_update_evento_acesso_rejeitado_por_trigger() {
        // Inserir evento
        jdbcTemplate.execute("""
                INSERT INTO ace_usuario (id, nome_completo, email, cpf, perfil, status_conta, provider_type, criado_por)
                VALUES ('00000000-0000-0000-0000-000000000002', 'Test User', 'test_update@cdhu.sp.gov.br',
                        '00000000002', 'ADMINISTRADOR', 'ATIVA', 'SCCI', '00000000-0000-0000-0000-000000000002')
                ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.execute("""
                INSERT INTO ace_evento_acesso (usuario_id, tipo_evento, resultado, ip_origem)
                VALUES ('00000000-0000-0000-0000-000000000002', 'LOGIN', 'SUCESSO', '10.0.0.1')
                """);

        // UPDATE deve ser rejeitado pelo trigger
        var ex = assertThrows(Exception.class, () ->
                jdbcTemplate.execute("UPDATE ace_evento_acesso SET resultado = 'FALHA' WHERE ip_origem = '10.0.0.1'"));
        assertTrue(ex.getMessage().contains("append-only") || ex.getMessage().contains("ace_evento_acesso"),
                "Trigger deve rejeitar UPDATE com mensagem sobre append-only");
    }

    @Test
    @DisplayName("test_RN_AUD_01_delete_evento_acesso_rejeitado_por_trigger")
    void test_RN_AUD_01_delete_evento_acesso_rejeitado_por_trigger() {
        jdbcTemplate.execute("""
                INSERT INTO ace_usuario (id, nome_completo, email, cpf, perfil, status_conta, provider_type, criado_por)
                VALUES ('00000000-0000-0000-0000-000000000003', 'Test User Del', 'test_del@cdhu.sp.gov.br',
                        '00000000003', 'ADMINISTRADOR', 'ATIVA', 'SCCI', '00000000-0000-0000-0000-000000000003')
                ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.execute("""
                INSERT INTO ace_evento_acesso (usuario_id, tipo_evento, resultado, ip_origem)
                VALUES ('00000000-0000-0000-0000-000000000003', 'LOGOUT', 'SUCESSO', '10.0.0.2')
                """);

        // DELETE deve ser rejeitado pelo trigger
        var ex = assertThrows(Exception.class, () ->
                jdbcTemplate.execute("DELETE FROM ace_evento_acesso WHERE ip_origem = '10.0.0.2'"));
        assertTrue(ex.getMessage().contains("delete") || ex.getMessage().contains("ace_evento_acesso"),
                "Trigger deve rejeitar DELETE");
    }

    @Test
    @DisplayName("test_RN_ACE_09_delete_usuario_rejeitado_por_trigger")
    void test_RN_ACE_09_delete_usuario_rejeitado_por_trigger() {
        jdbcTemplate.execute("""
                INSERT INTO ace_usuario (id, nome_completo, email, cpf, perfil, status_conta, provider_type, criado_por)
                VALUES ('00000000-0000-0000-0000-000000000004', 'No Delete', 'nodelete@cdhu.sp.gov.br',
                        '00000000004', 'ADMINISTRADOR', 'ATIVA', 'SCCI', '00000000-0000-0000-0000-000000000004')
                ON CONFLICT DO NOTHING
                """);

        // DELETE em ace_usuario deve ser rejeitado (RN-ACE-09)
        var ex = assertThrows(Exception.class, () ->
                jdbcTemplate.execute("DELETE FROM ace_usuario WHERE id = '00000000-0000-0000-0000-000000000004'"));
        assertTrue(ex.getMessage().contains("ace_usuario") || ex.getMessage().contains("delete"),
                "Trigger deve rejeitar DELETE em ace_usuario");
    }
}
