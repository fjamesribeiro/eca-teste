package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.VinculacaoEmpreendimento;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.CadastrarUsuarioUseCase;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de integração: cadastro e2e (UC-ACE-01).
 * Verifica fluxo completo: POST /usuarios → banco + IdP mock + e-mail + trilha.
 */
@ExtendWith(MockitoExtension.class)
class CadastroIntegracaoTest {

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
    @DisplayName("test_cadastro_e2e_engenheiro_com_vinculacoes")
    void test_cadastro_e2e_engenheiro_com_vinculacoes() {
        UUID emp1 = UUID.randomUUID();
        UUID emp2 = UUID.randomUUID();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByCpf(any())).thenReturn(false);
        when(identityProvider.criarUsuario(any(), any()))
                .thenReturn(new IdentityProviderPort.CriarUsuarioResult("scci-user-456"));

        var comando = new CadastrarUsuarioUseCase.Comando(
                "Maria Engenheira", "maria@cdhu.sp.gov.br", "52998224725",
                "ENGENHEIRO_MEDICAO", List.of(emp1, emp2));

        var resultado = useCase.executar(comando, ADMIN_ID);

        // Verificar resultado
        assertNotNull(resultado.id());
        assertEquals("Maria Engenheira", resultado.nomeCompleto());
        assertEquals("ENGENHEIRO_MEDICAO", resultado.perfil());
        assertEquals("PENDENTE_ATIVACAO", resultado.statusConta());
        assertEquals(2, resultado.empreendimentoIds().size());

        // Verificar que criou no IdP
        verify(identityProvider).criarUsuario("maria@cdhu.sp.gov.br", "Maria Engenheira");
        verify(usuarioRepository).atualizarProviderUserId(any(), eq("scci-user-456"));

        // Verificar que criou vinculações
        verify(vinculacaoRepository, times(2)).salvar(any(VinculacaoEmpreendimento.class));

        // Verificar que enviou e-mail
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).enviarEmailAtivacao(emailCaptor.capture(), any(), any(), any());
        assertEquals("maria@cdhu.sp.gov.br", emailCaptor.getValue());

        // Verificar que registrou evento na trilha
        ArgumentCaptor<EventoAcesso> eventoCaptor = ArgumentCaptor.forClass(EventoAcesso.class);
        verify(auditTrail).registrar(eventoCaptor.capture());
        assertEquals(br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso.CRIACAO_USUARIO,
                eventoCaptor.getValue().getTipoEvento());
    }

    @Test
    @DisplayName("test_cadastro_e2e_admin_sem_vinculacao_e_com_email")
    void test_cadastro_e2e_admin_sem_vinculacao_e_com_email() {
        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByCpf(any())).thenReturn(false);
        when(identityProvider.criarUsuario(any(), any()))
                .thenReturn(new IdentityProviderPort.CriarUsuarioResult("scci-admin"));

        var comando = new CadastrarUsuarioUseCase.Comando(
                "Novo Admin", "novoadmin@cdhu.sp.gov.br", "52998224725",
                "ADMINISTRADOR", List.of());

        var resultado = useCase.executar(comando, ADMIN_ID);

        assertNotNull(resultado.id());
        assertEquals("ADMINISTRADOR", resultado.perfil());
        assertTrue(resultado.empreendimentoIds().isEmpty());

        // Admin não cria vinculações
        verify(vinculacaoRepository, never()).salvar(any());
        // Mas envia e-mail e registra trilha
        verify(emailPort).enviarEmailAtivacao(any(), any(), any(), any());
        verify(auditTrail).registrar(any());
    }

    @Test
    @DisplayName("test_cadastro_e2e_agente_promotor_provider_cognito")
    void test_cadastro_e2e_agente_promotor_provider_cognito() {
        UUID emp = UUID.randomUUID();

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByCpf(any())).thenReturn(false);
        when(identityProvider.criarUsuario(any(), any()))
                .thenReturn(new IdentityProviderPort.CriarUsuarioResult("cognito-ap-123"));

        var comando = new CadastrarUsuarioUseCase.Comando(
                "AP Externo", "ap@incorporadora.com.br", "52998224725",
                "AGENTE_PROMOTOR", List.of(emp));

        var resultado = useCase.executar(comando, ADMIN_ID);

        assertEquals("AGENTE_PROMOTOR", resultado.perfil());
        verify(identityProvider).criarUsuario("ap@incorporadora.com.br", "AP Externo");
    }
}
