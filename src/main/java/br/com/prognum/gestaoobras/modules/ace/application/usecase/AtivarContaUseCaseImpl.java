package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.CodigoTotpInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.SenhasDiferentesException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.StatusInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.TokenInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.HistoricoSenha;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.AtivarContaUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.HistoricoSenhaRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;
import br.com.prognum.gestaoobras.modules.ace.domain.service.PoliticaSenhaService;

import java.time.Instant;
import java.util.Map;

/**
 * UC-ACE-02: Ativar conta e configurar MFA.
 *
 * Fluxo:
 * 1. definirSenha: valida token → valida senha → define no IdP → retorna QR code MFA
 * 2. confirmarMfa: valida TOTP → ativa conta → registra evento
 *
 * Source: ET_ACE seção 3.3, RN-ACE-03, RN-ACE-04, RN-ACE-08
 */
public class AtivarContaUseCaseImpl implements AtivarContaUseCase {

    private final UsuarioRepository usuarioRepository;
    private final IdentityProviderPort identityProvider;
    private final HistoricoSenhaRepository historicoSenhaRepository;
    private final AuditTrailPort auditTrail;
    private final PoliticaSenhaService politicaSenhaService;
    private final PoliticaSenhaService.BcryptMatcher bcryptMatcher;
    private final HashEncoder hashEncoder;

    public AtivarContaUseCaseImpl(UsuarioRepository usuarioRepository,
                                   IdentityProviderPort identityProvider,
                                   HistoricoSenhaRepository historicoSenhaRepository,
                                   AuditTrailPort auditTrail,
                                   PoliticaSenhaService politicaSenhaService,
                                   PoliticaSenhaService.BcryptMatcher bcryptMatcher,
                                   HashEncoder hashEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.identityProvider = identityProvider;
        this.historicoSenhaRepository = historicoSenhaRepository;
        this.auditTrail = auditTrail;
        this.politicaSenhaService = politicaSenhaService;
        this.bcryptMatcher = bcryptMatcher;
        this.hashEncoder = hashEncoder;
    }

    @Override
    public ResultadoMfaSetup definirSenha(ComandoDefinirSenha comando) {
        // 1. Buscar usuário pelo token de ativação
        DadosUsuario usuario = usuarioRepository.findByTokenAtivacao(comando.token())
                .orElseThrow(TokenInvalidoException::invalido);

        // 2. Verificar status PENDENTE_ATIVACAO
        if (usuario.statusConta() != StatusConta.PENDENTE_ATIVACAO) {
            throw new StatusInvalidoException(
                    "Ativação só é possível para contas com status PENDENTE_ATIVACAO.");
        }

        // 3. Verificar token não expirado (V-ACE-04)
        if (usuario.tokenAtivacaoExpiraEm() == null ||
                Instant.now().isAfter(usuario.tokenAtivacaoExpiraEm())) {
            throw TokenInvalidoException.expirado();
        }

        // 4. Verificar senhas iguais
        if (!comando.senha().equals(comando.confirmarSenha())) {
            throw new SenhasDiferentesException();
        }

        // 5. Validar complexidade da senha (V-ACE-03, RN-ACE-08)
        politicaSenhaService.validarComplexidade(comando.senha());

        // 6. Validar anti-reuso (RN-ACE-08)
        var historicoRecente = historicoSenhaRepository
                .findUltimasByUsuarioId(usuario.id(), 5);
        politicaSenhaService.validarAntiReuso(comando.senha(), historicoRecente, bcryptMatcher);

        // 7. Definir senha no IdP
        identityProvider.definirSenha(usuario.providerUserId(), comando.senha());

        // 8. Salvar hash no histórico de senhas
        String senhaHash = hashEncoder.encode(comando.senha());
        historicoSenhaRepository.salvar(HistoricoSenha.criar(usuario.id(), senhaHash));

        // 9. Atualizar ultima_troca_senha
        usuarioRepository.atualizarUltimaTrocaSenha(usuario.id(), Instant.now());

        // 10. Iniciar setup MFA (RN-ACE-04)
        IdentityProviderPort.MfaSetupResult mfaSetup =
                identityProvider.iniciarSetupMfa(usuario.providerUserId());

        return new ResultadoMfaSetup(mfaSetup.qrCodeUri(), mfaSetup.secret());
    }

    @Override
    public ResultadoAtivacao confirmarMfa(ComandoConfirmarMfa comando) {
        // 1. Buscar usuário pelo token
        DadosUsuario usuario = usuarioRepository.findByTokenAtivacao(comando.token())
                .orElseThrow(TokenInvalidoException::invalido);

        // 2. Verificar status
        if (usuario.statusConta() != StatusConta.PENDENTE_ATIVACAO) {
            throw new StatusInvalidoException(
                    "Confirmação MFA só é possível para contas com status PENDENTE_ATIVACAO.");
        }

        // 3. Validar TOTP no IdP (V-ACE-05)
        boolean totpValido = identityProvider.confirmarMfa(
                usuario.providerUserId(), comando.codigoTotp());
        if (!totpValido) {
            throw new CodigoTotpInvalidoException();
        }

        // 4. Atualizar mfa_configurado
        usuarioRepository.atualizarMfaConfigurado(usuario.id(), true);

        // 5. Transição PENDENTE_ATIVACAO → ATIVA
        usuarioRepository.atualizarStatusConta(usuario.id(), StatusConta.ATIVA);

        // 6. Limpar token de ativação
        usuarioRepository.atualizarTokenAtivacao(usuario.id(), null, null);

        // 7. Registrar evento na trilha (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuario.id(),
                TipoEventoAcesso.ATIVACAO_CONTA,
                ResultadoEvento.SUCESSO,
                "sistema",
                null, null,
                Map.of("perfil", usuario.perfil().name()),
                null
        ));

        return new ResultadoAtivacao("ATIVA", "Conta ativada com sucesso. MFA configurado.");
    }

    /**
     * Interface funcional para encoding de senhas (BCrypt).
     * Mantém a camada de aplicação livre de dependência de Spring Security.
     */
    @FunctionalInterface
    public interface HashEncoder {
        String encode(String rawPassword);
    }
}
