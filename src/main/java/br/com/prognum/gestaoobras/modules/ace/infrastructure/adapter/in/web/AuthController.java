package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web;

import br.com.prognum.gestaoobras.modules.ace.domain.port.in.AtivarContaUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.ResetarSenhaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller para autenticação e conta (ET_ACE seções 3.3, 3.4, 3.7).
 * Base path: /api/v1/auth
 *
 * Nota: POST /auth/callback e POST /auth/logout estão bloqueados por GAP-ACE-05 (T-034).
 * Este controller implementa os endpoints sem bloqueio:
 * - POST /auth/ativar-conta (UC-ACE-02)
 * - POST /auth/confirmar-mfa (UC-ACE-02)
 * - POST /auth/alterar-senha (UC-ACE-06)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AtivarContaUseCase ativarContaUseCase;
    private final ResetarSenhaUseCase resetarSenhaUseCase;

    public AuthController(AtivarContaUseCase ativarContaUseCase,
                           ResetarSenhaUseCase resetarSenhaUseCase) {
        this.ativarContaUseCase = ativarContaUseCase;
        this.resetarSenhaUseCase = resetarSenhaUseCase;
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

    // POST /auth/callback e POST /auth/logout — bloqueados por GAP-ACE-05 (T-034)

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
}
