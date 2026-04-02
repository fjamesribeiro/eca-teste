package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.CodigoTotpInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.SenhaAtualIncorretaException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.SenhasDiferentesException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.StatusInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.HistoricoSenha;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.ResetarSenhaUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.EmailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.HistoricoSenhaRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;
import br.com.prognum.gestaoobras.modules.ace.domain.service.PoliticaSenhaService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * UC-ACE-06: Resetar/alterar senha.
 *
 * Dois fluxos:
 * 1. alterarSenha (self-service): senha atual + nova senha + TOTP
 * 2. forcarReset (admin): envia e-mail de redefinição
 *
 * Source: ET_ACE seção 3.7, RN-ACE-08
 */
public class ResetarSenhaUseCaseImpl implements ResetarSenhaUseCase {

    private final UsuarioRepository usuarioRepository;
    private final IdentityProviderPort identityProvider;
    private final HistoricoSenhaRepository historicoSenhaRepository;
    private final EmailPort emailPort;
    private final AuditTrailPort auditTrail;
    private final PoliticaSenhaService politicaSenhaService;
    private final PoliticaSenhaService.BcryptMatcher bcryptMatcher;
    private final AtivarContaUseCaseImpl.HashEncoder hashEncoder;
    private final int historicoQuantidade;

    public ResetarSenhaUseCaseImpl(UsuarioRepository usuarioRepository,
                                    IdentityProviderPort identityProvider,
                                    HistoricoSenhaRepository historicoSenhaRepository,
                                    EmailPort emailPort,
                                    AuditTrailPort auditTrail,
                                    PoliticaSenhaService politicaSenhaService,
                                    PoliticaSenhaService.BcryptMatcher bcryptMatcher,
                                    AtivarContaUseCaseImpl.HashEncoder hashEncoder,
                                    int historicoQuantidade) {
        this.usuarioRepository = usuarioRepository;
        this.identityProvider = identityProvider;
        this.historicoSenhaRepository = historicoSenhaRepository;
        this.emailPort = emailPort;
        this.auditTrail = auditTrail;
        this.politicaSenhaService = politicaSenhaService;
        this.bcryptMatcher = bcryptMatcher;
        this.hashEncoder = hashEncoder;
        this.historicoQuantidade = historicoQuantidade;
    }

    @Override
    public Resultado alterarSenha(ComandoAlterarSenha comando, UUID usuarioId) {
        // 1. Buscar usuário
        DadosUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        // 2. Verificar conta ATIVA
        if (usuario.statusConta() != StatusConta.ATIVA) {
            throw new StatusInvalidoException(usuario.statusConta(), StatusConta.ATIVA);
        }

        // 3. Validar senha atual no IdP (UC-ACE-06 1a)
        boolean senhaAtualValida = identityProvider.validarSenhaAtual(
                usuario.providerUserId(), comando.senhaAtual());
        if (!senhaAtualValida) {
            throw new SenhaAtualIncorretaException();
        }

        // 4. Verificar que nova senha e confirmação coincidem
        if (!comando.novaSenha().equals(comando.confirmarNovaSenha())) {
            throw new SenhasDiferentesException();
        }

        // 5. Validar complexidade (V-ACE-03, RN-ACE-08)
        politicaSenhaService.validarComplexidade(comando.novaSenha());

        // 6. Validar anti-reuso (RN-ACE-08)
        var historicoRecente = historicoSenhaRepository
                .findUltimasByUsuarioId(usuario.id(), historicoQuantidade);
        politicaSenhaService.validarAntiReuso(comando.novaSenha(), historicoRecente, bcryptMatcher);

        // 7. Validar TOTP (RN-ACE-04, UC-ACE-06 passo 4)
        boolean totpValido = identityProvider.confirmarMfa(
                usuario.providerUserId(), comando.codigoTotp());
        if (!totpValido) {
            throw new CodigoTotpInvalidoException();
        }

        // 8. Alterar senha no IdP
        identityProvider.alterarSenha(usuario.providerUserId(), comando.novaSenha());

        // 9. Salvar hash no histórico
        String senhaHash = hashEncoder.encode(comando.novaSenha());
        historicoSenhaRepository.salvar(HistoricoSenha.criar(usuario.id(), senhaHash));

        // 10. Atualizar ultima_troca_senha
        usuarioRepository.atualizarUltimaTrocaSenha(usuario.id(), Instant.now());

        // 11. Registrar evento (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuario.id(),
                TipoEventoAcesso.TROCA_SENHA,
                ResultadoEvento.SUCESSO,
                "sistema",
                null, null,
                Map.of("tipo", "self-service"),
                null
        ));

        return new Resultado("Senha alterada com sucesso.");
    }

    @Override
    public Resultado forcarReset(UUID usuarioId, UUID adminId) {
        // 1. Buscar usuário
        DadosUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        // 2. Verificar conta ATIVA
        if (usuario.statusConta() != StatusConta.ATIVA) {
            throw new StatusInvalidoException(usuario.statusConta(), StatusConta.ATIVA);
        }

        // 3. Forçar reset no IdP
        identityProvider.forcarResetSenha(usuario.providerUserId());

        // 4. Enviar e-mail de redefinição
        String linkReset = "/auth/reset-senha"; // URL base será configurada na infra
        emailPort.enviarEmailResetSenha(usuario.email(), usuario.nomeCompleto(), linkReset);

        // 5. Registrar evento (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuario.id(),
                TipoEventoAcesso.RESET_SENHA,
                ResultadoEvento.SUCESSO,
                "sistema",
                null, null,
                Map.of("tipo", "admin-forcado", "admin_id", adminId.toString()),
                null
        ));

        String emailMascarado = mascararEmail(usuario.email());
        return new Resultado("E-mail de redefinição de senha enviado para " + emailMascarado + ".");
    }

    private String mascararEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
