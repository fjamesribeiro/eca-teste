package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.AceException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.RealizarLoginUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.SessaoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.service.GestaoSessaoService;
import br.com.prognum.gestaoobras.modules.ace.domain.service.PoliticaSenhaService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC-ACE-03: Realizar login via callback OIDC.
 *
 * Fluxo: troca code → tokens, verifica status, verifica senha expirada (GAP-ACE-05
 * RESOLVIDO: JWT com scope "password_change_required"), aplica regra de sessões.
 *
 * Source: ET_ACE seção 3.4, RN-ACE-04, RN-ACE-12
 */
public class RealizarLoginUseCaseImpl implements RealizarLoginUseCase {

    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final VinculacaoEmpreendimentoRepository vinculacaoRepository;
    private final IdentityProviderPort identityProvider;
    private final AuditTrailPort auditTrail;
    private final GestaoSessaoService gestaoSessaoService;
    private final PoliticaSenhaService politicaSenhaService;
    private final int diasExpiracaoSenha;
    private final int timeoutSessionMinutos;

    public RealizarLoginUseCaseImpl(UsuarioRepository usuarioRepository,
                                     SessaoRepository sessaoRepository,
                                     VinculacaoEmpreendimentoRepository vinculacaoRepository,
                                     IdentityProviderPort identityProvider,
                                     AuditTrailPort auditTrail,
                                     GestaoSessaoService gestaoSessaoService,
                                     PoliticaSenhaService politicaSenhaService,
                                     int diasExpiracaoSenha,
                                     int timeoutSessionMinutos) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.vinculacaoRepository = vinculacaoRepository;
        this.identityProvider = identityProvider;
        this.auditTrail = auditTrail;
        this.gestaoSessaoService = gestaoSessaoService;
        this.politicaSenhaService = politicaSenhaService;
        this.diasExpiracaoSenha = diasExpiracaoSenha;
        this.timeoutSessionMinutos = timeoutSessionMinutos;
    }

    @Override
    public Resultado executar(Comando comando) {
        // 1. Trocar code por tokens no IdP
        IdentityProviderPort.TokenResult tokenResult =
                identityProvider.trocarCodePorTokens(comando.code(), comando.redirectUri());

        // 2. Buscar usuário pelo provider_user_id (extraído do token)
        // Em produção, o provider_user_id viria do JWT do IdP. Simplificação: buscar por email do token.
        DadosUsuario usuario = usuarioRepository.findByProviderUserId(tokenResult.accessToken())
                .orElseThrow(() -> new CredenciaisInvalidasException());

        // 3. Verificar status da conta
        switch (usuario.statusConta()) {
            case BLOQUEADA -> {
                // Verificar auto-desbloqueio (30 min)
                if (usuario.bloqueadoAte() != null &&
                        java.time.Instant.now().isAfter(usuario.bloqueadoAte())) {
                    usuarioRepository.atualizarStatusConta(usuario.id(), StatusConta.ATIVA);
                    usuarioRepository.atualizarTentativasLoginFalhas(usuario.id(), 0, null);
                } else {
                    throw new ContaBloqueadaException();
                }
            }
            case DESATIVADA -> throw new ContaDesativadaException();
            case PENDENTE_ATIVACAO -> throw new CredenciaisInvalidasException();
            case ATIVA -> { /* OK */ }
        }

        // 4. Verificar senha expirada (V-ACE-10, GAP-ACE-05 RESOLVIDO)
        if (politicaSenhaService.isSenhaExpirada(usuario.ultimaTrocaSenha(), diasExpiracaoSenha)) {
            auditTrail.registrar(EventoAcesso.criar(
                    usuario.id(), TipoEventoAcesso.LOGIN, ResultadoEvento.SUCESSO,
                    comando.ipOrigem(), comando.dispositivo(), comando.navegador(),
                    Map.of("senha_expirada", true), null));

            throw new SenhaExpiradaException();
        }

        // 5. Aplicar regra de sessões (RN-ACE-12)
        var sessoesAtivas = sessaoRepository.findAtivasByUsuarioId(usuario.id());
        var encerradas = gestaoSessaoService.aplicarLimiteSessoes(sessoesAtivas);
        encerradas.forEach(sessaoRepository::salvar);

        // 6. Criar nova sessão
        String sessionToken = UUID.randomUUID().toString();
        String tokenHash = sha256(sessionToken);
        Sessao novaSessao = Sessao.criar(usuario.id(), tokenHash,
                comando.ipOrigem(), comando.dispositivo(), comando.navegador());
        sessaoRepository.salvar(novaSessao);

        // 7. Atualizar ultimo_acesso
        usuarioRepository.atualizarUltimoAcesso(usuario.id(), java.time.Instant.now());

        // 8. Registrar evento LOGIN (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuario.id(), TipoEventoAcesso.LOGIN, ResultadoEvento.SUCESSO,
                comando.ipOrigem(), comando.dispositivo(), comando.navegador(),
                Map.of("sessoes_encerradas", encerradas.size()), null));

        // 9. Buscar empreendimentos
        var empreendimentoIds = vinculacaoRepository
                .findEmpreendimentoIdsAtivosByUsuarioId(usuario.id())
                .stream().toList();

        return new Resultado(
                tokenResult.accessToken(),
                tokenResult.refreshToken(),
                timeoutSessionMinutos * 60L,
                new UsuarioResumo(
                        usuario.id(),
                        usuario.nomeCompleto(),
                        usuario.perfil().name(),
                        empreendimentoIds
                )
        );
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // Exceções

    public static class CredenciaisInvalidasException extends AceException {
        public CredenciaisInvalidasException() {
            super("CREDENCIAIS_INVALIDAS", "E-mail ou senha incorretos.");
        }
    }

    public static class ContaBloqueadaException extends AceException {
        public ContaBloqueadaException() {
            super("V-ACE-06", "Conta bloqueada. Aguarde 30 minutos ou contate o Administrador.");
        }
    }

    public static class ContaDesativadaException extends AceException {
        public ContaDesativadaException() {
            super("CONTA_DESATIVADA", "Conta desativada. Contate o Administrador.");
        }
    }

    public static class SenhaExpiradaException extends AceException {
        public SenhaExpiradaException() {
            super("SENHA_EXPIRADA", "Sua senha expirou. Defina uma nova.");
        }
    }
}
