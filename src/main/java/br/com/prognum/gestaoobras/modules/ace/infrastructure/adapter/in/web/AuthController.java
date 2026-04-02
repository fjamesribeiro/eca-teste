package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web;

import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.MotivoEncerramento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.AtivarContaUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.RealizarLoginUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.ResetarSenhaUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.SessaoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller para autenticação e conta (ET_ACE seções 3.3, 3.4, 3.7).
 * Base path: /api/v1/auth
 *
 * Endpoints:
 * - POST /auth/callback (UC-ACE-03) — GAP-ACE-05 RESOLVIDO
 * - POST /auth/logout (UC-ACE-03)
 * - POST /auth/ativar-conta (UC-ACE-02)
 * - POST /auth/confirmar-mfa (UC-ACE-02)
 * - POST /auth/alterar-senha (UC-ACE-06)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AtivarContaUseCase ativarContaUseCase;
    private final RealizarLoginUseCase realizarLoginUseCase;
    private final ResetarSenhaUseCase resetarSenhaUseCase;
    private final SessaoRepository sessaoRepository;
    private final AuditTrailPort auditTrail;

    public AuthController(AtivarContaUseCase ativarContaUseCase,
                           RealizarLoginUseCase realizarLoginUseCase,
                           ResetarSenhaUseCase resetarSenhaUseCase,
                           SessaoRepository sessaoRepository,
                           AuditTrailPort auditTrail) {
        this.ativarContaUseCase = ativarContaUseCase;
        this.realizarLoginUseCase = realizarLoginUseCase;
        this.resetarSenhaUseCase = resetarSenhaUseCase;
        this.sessaoRepository = sessaoRepository;
        this.auditTrail = auditTrail;
    }

    /**
     * POST /api/v1/auth/ativar-conta — Define senha e inicia MFA (UC-ACE-02).
     * Público (validado via token de ativação).
     */
    @PostMapping("/ativar-conta")
    public ResponseEntity<AtivarContaUseCase.ResultadoMfaSetup> ativarConta(
            @RequestBody AtivarContaRequest request) {

        var comando = new AtivarContaUseCase.ComandoDefinirSenha(
                request.token(), request.senha(), request.confirmarSenha());

        var resultado = ativarContaUseCase.definirSenha(comando);
        return ResponseEntity.ok(resultado);
    }

    /**
     * POST /api/v1/auth/confirmar-mfa — Confirma TOTP e ativa conta (UC-ACE-02).
     * Público (validado via token de ativação).
     */
    @PostMapping("/confirmar-mfa")
    public ResponseEntity<AtivarContaUseCase.ResultadoAtivacao> confirmarMfa(
            @RequestBody ConfirmarMfaRequest request) {

        var comando = new AtivarContaUseCase.ComandoConfirmarMfa(
                request.token(), request.codigoTotp());

        var resultado = ativarContaUseCase.confirmarMfa(comando);
        return ResponseEntity.ok(resultado);
    }

    /**
     * POST /api/v1/auth/alterar-senha — Self-service alterar senha (UC-ACE-06).
     * Requer autenticação.
     */
    @PostMapping("/alterar-senha")
    public ResponseEntity<ResetarSenhaUseCase.Resultado> alterarSenha(
            @RequestBody AlterarSenhaRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID usuarioId = extractUsuarioId(jwt);
        var comando = new ResetarSenhaUseCase.ComandoAlterarSenha(
                request.senhaAtual(),
                request.novaSenha(),
                request.confirmarNovaSenha(),
                request.codigoTotp()
        );

        var resultado = resetarSenhaUseCase.alterarSenha(comando, usuarioId);
        return ResponseEntity.ok(resultado);
    }

    /**
     * POST /api/v1/auth/callback — Callback OIDC (UC-ACE-03).
     * Público. Troca authorization code por tokens, aplica regras de sessão.
     *
     * GAP-ACE-05 RESOLVIDO: se senha expirada, retorna 403 SENHA_EXPIRADA.
     * O frontend deve redirecionar para /auth/alterar-senha com JWT parcial.
     */
    @PostMapping("/callback")
    public ResponseEntity<RealizarLoginUseCase.Resultado> callback(
            @RequestBody CallbackRequest request,
            HttpServletRequest httpRequest) {

        var comando = new RealizarLoginUseCase.Comando(
                request.code(),
                request.redirectUri(),
                request.provider(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"),
                httpRequest.getHeader("User-Agent")
        );

        var resultado = realizarLoginUseCase.executar(comando);
        return ResponseEntity.ok(resultado);
    }

    /**
     * POST /api/v1/auth/logout — Encerra sessão atual (UC-ACE-03).
     * Requer autenticação.
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal Jwt jwt) {
        UUID usuarioId = extractUsuarioId(jwt);

        // Encerrar sessões ativas do usuário (simplificação: encerra todas as do user)
        var sessoesAtivas = sessaoRepository.findAtivasByUsuarioId(usuarioId);
        for (Sessao sessao : sessoesAtivas) {
            if (sessao.isAtiva()) {
                sessao.encerrar(MotivoEncerramento.LOGOUT);
                sessaoRepository.salvar(sessao);
            }
        }

        auditTrail.registrar(EventoAcesso.criar(
                usuarioId, TipoEventoAcesso.LOGOUT, ResultadoEvento.SUCESSO,
                "sistema", null, null, null, null));

        return ResponseEntity.ok(Map.of("mensagem", "Sessão encerrada."));
    }

    private UUID extractUsuarioId(Jwt jwt) {
        String sub = jwt.getClaimAsString("usuario_id");
        if (sub == null) {
            sub = jwt.getSubject();
        }
        return UUID.fromString(sub);
    }

    // Request DTOs

    record AtivarContaRequest(String token, String senha, String confirmarSenha) {}

    record ConfirmarMfaRequest(String token, String codigoTotp) {}

    record AlterarSenhaRequest(String senhaAtual, String novaSenha, String confirmarNovaSenha, String codigoTotp) {}

    record CallbackRequest(String code, String redirectUri, String provider) {}
}
