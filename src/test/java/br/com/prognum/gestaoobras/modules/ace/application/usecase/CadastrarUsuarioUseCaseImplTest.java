package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.EmailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.UsuarioSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários: CadastrarUsuarioUseCaseImpl (UC-ACE-01).
 */
@ExtendWith(MockitoExtension.class)
class CadastrarUsuarioUseCaseImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioSpringDataRepository usuarioJpaRepo;
    @Mock private VinculacaoEmpreendimentoRepository vinculacaoRepository;
    @Mock private IdentityProviderPort identityProvider;
    @Mock private EmailPort emailPort;
    @Mock private AuditTrailPort auditTrail;

    private CadastrarUsuarioUseCaseImpl useCase;
    private static final UUID ADMIN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CadastrarUsuarioUseCaseImpl(
                usuarioRepository, usuarioJpaRepo, vinculacaoRepository,
                identityProvider, emailPort, auditTrail, 72);
    }

    @Test
    @DisplayName("test_V_ACE_02_cpf_invalido_rejeita")
    void test_V_ACE_02_cpf_invalido_rejeita() {
        var comando = new CadastrarUsuarioUseCaseImpl.Comando(
                "João", "joao@test.com", "00000000000", "ENGENHEIRO_MEDICAO",
                List.of(UUID.randomUUID()));

        assertThrows(CadastrarUsuarioUseCaseImpl.CpfInvalidoException.class,
                () -> useCase.executar(comando, ADMIN_ID));
    }

    @Test
    @DisplayName("test_V_ACE_01_email_duplicado_rejeita")
    void test_V_ACE_01_email_duplicado_rejeita() {
        when(usuarioRepository.existsByEmail("joao@test.com")).thenReturn(true);

        var comando = new CadastrarUsuarioUseCaseImpl.Comando(
                "João", "joao@test.com", "52998224725", "ENGENHEIRO_MEDICAO",
                List.of(UUID.randomUUID()));

        assertThrows(CadastrarUsuarioUseCaseImpl.EmailDuplicadoException.class,
                () -> useCase.executar(comando, ADMIN_ID));
    }

    @Test
    @DisplayName("test_V_ACE_02_cpf_duplicado_rejeita")
    void test_V_ACE_02_cpf_duplicado_rejeita() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByCpf("52998224725")).thenReturn(true);

        var comando = new CadastrarUsuarioUseCaseImpl.Comando(
                "João", "joao@test.com", "52998224725", "ENGENHEIRO_MEDICAO",
                List.of(UUID.randomUUID()));

        assertThrows(CadastrarUsuarioUseCaseImpl.CpfDuplicadoException.class,
                () -> useCase.executar(comando, ADMIN_ID));
    }

    @Test
    @DisplayName("test_RN_ACE_01_perfil_invalido_rejeita")
    void test_RN_ACE_01_perfil_invalido_rejeita() {
        var comando = new CadastrarUsuarioUseCaseImpl.Comando(
                "João", "joao@test.com", "52998224725", "PERFIL_INEXISTENTE",
                List.of(UUID.randomUUID()));

        assertThrows(CadastrarUsuarioUseCaseImpl.PerfilInvalidoException.class,
                () -> useCase.executar(comando, ADMIN_ID));
    }

    @Test
    @DisplayName("test_RN_ACE_06_perfil_operacional_sem_vinculacao_rejeita")
    void test_RN_ACE_06_perfil_operacional_sem_vinculacao_rejeita() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByCpf(any())).thenReturn(false);

        var comando = new CadastrarUsuarioUseCaseImpl.Comando(
                "João", "joao@test.com", "52998224725", "ENGENHEIRO_MEDICAO",
                List.of()); // vazio!

        assertThrows(CadastrarUsuarioUseCaseImpl.VinculacaoObrigatoriaException.class,
                () -> useCase.executar(comando, ADMIN_ID));
    }

    @Test
    @DisplayName("test_RN_ACE_06_admin_sem_vinculacao_aceita")
    void test_RN_ACE_06_admin_sem_vinculacao_aceita() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByCpf(any())).thenReturn(false);
        when(identityProvider.criarUsuario(any(), any()))
                .thenReturn(new IdentityProviderPort.CriarUsuarioResult("cognito-123"));

        var comando = new CadastrarUsuarioUseCaseImpl.Comando(
                "Admin", "admin@cdhu.sp.gov.br", "52998224725", "ADMINISTRADOR",
                List.of()); // Admin não exige vinculação

        var resultado = useCase.executar(comando, ADMIN_ID);

        assertNotNull(resultado);
        assertEquals("ADMINISTRADOR", resultado.perfil());
        assertEquals("PENDENTE_ATIVACAO", resultado.statusConta());
        verify(emailPort).enviarEmailAtivacao(any(), any(), any(), any());
        verify(auditTrail).registrar(any());
    }

    @Test
    @DisplayName("test_RN_ACE_10_agente_promotor_provider_cognito")
    void test_RN_ACE_10_agente_promotor_provider_cognito() {
        assertEquals(br.com.prognum.gestaoobras.modules.ace.domain.model.ProviderType.COGNITO,
                Perfil.AGENTE_PROMOTOR.providerTypePadrao());
    }

    @Test
    @DisplayName("test_RN_ACE_10_diretor_financeiro_provider_scci")
    void test_RN_ACE_10_diretor_financeiro_provider_scci() {
        assertEquals(br.com.prognum.gestaoobras.modules.ace.domain.model.ProviderType.SCCI,
                Perfil.DIRETOR_FINANCEIRO.providerTypePadrao());
    }

    @Test
    @DisplayName("test_RN_ACE_10_tesouraria_provider_scci")
    void test_RN_ACE_10_tesouraria_provider_scci() {
        assertEquals(br.com.prognum.gestaoobras.modules.ace.domain.model.ProviderType.SCCI,
                Perfil.TESOURARIA.providerTypePadrao());
    }
}
