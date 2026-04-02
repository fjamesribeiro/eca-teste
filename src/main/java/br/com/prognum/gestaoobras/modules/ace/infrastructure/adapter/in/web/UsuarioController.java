package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web;

import br.com.prognum.gestaoobras.modules.ace.domain.port.in.CadastrarUsuarioUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.DesativarUsuarioUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.GerenciarPerfisUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.ResetarSenhaUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller para gestão de usuários (ET_ACE seções 3.2, 3.5, 3.6).
 * Base path: /api/v1/usuarios
 * Actor: Administrador (RN-ACE-03)
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final GerenciarPerfisUseCase gerenciarPerfisUseCase;
    private final DesativarUsuarioUseCase desativarUsuarioUseCase;
    private final ResetarSenhaUseCase resetarSenhaUseCase;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(CadastrarUsuarioUseCase cadastrarUsuarioUseCase,
                              GerenciarPerfisUseCase gerenciarPerfisUseCase,
                              DesativarUsuarioUseCase desativarUsuarioUseCase,
                              ResetarSenhaUseCase resetarSenhaUseCase,
                              UsuarioRepository usuarioRepository) {
        this.cadastrarUsuarioUseCase = cadastrarUsuarioUseCase;
        this.gerenciarPerfisUseCase = gerenciarPerfisUseCase;
        this.desativarUsuarioUseCase = desativarUsuarioUseCase;
        this.resetarSenhaUseCase = resetarSenhaUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * POST /api/v1/usuarios — Cadastrar novo usuário (UC-ACE-01).
     * Permission: ACE → CRIAR
     */
    @PostMapping
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'CRIAR')")
    public ResponseEntity<CadastrarUsuarioUseCase.Resultado> cadastrar(
            @RequestBody CadastrarRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID adminId = extractUsuarioId(jwt);
        var comando = new CadastrarUsuarioUseCase.Comando(
                request.nomeCompleto(),
                request.email(),
                request.cpf(),
                request.perfil(),
                request.empreendimentoIds() != null ? request.empreendimentoIds() : List.of()
        );

        var resultado = cadastrarUsuarioUseCase.executar(comando, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * GET /api/v1/usuarios/{id} — Dados completos do usuário (UC-ACE-04).
     * Permission: ACE → VISUALIZAR
     */
    @GetMapping("/{id}")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'VISUALIZAR')")
    public ResponseEntity<UsuarioRepository.DadosUsuario> buscarPorId(@PathVariable UUID id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        return ResponseEntity.ok(usuario);
    }

    /**
     * PUT /api/v1/usuarios/{id} — Alterar perfil e vinculações (UC-ACE-04).
     * Permission: ACE → EDITAR
     */
    @PutMapping("/{id}")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    public ResponseEntity<GerenciarPerfisUseCase.Resultado> atualizar(
            @PathVariable UUID id,
            @RequestBody AtualizarRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID adminId = extractUsuarioId(jwt);
        var comando = new GerenciarPerfisUseCase.Comando(
                id,
                request.perfil(),
                request.empreendimentoIds() != null ? request.empreendimentoIds() : List.of()
        );

        var resultado = gerenciarPerfisUseCase.executar(comando, adminId);
        return ResponseEntity.ok(resultado);
    }

    /**
     * POST /api/v1/usuarios/{id}/desativar — Desativar conta (UC-ACE-05).
     * Permission: ACE → EDITAR
     */
    @PostMapping("/{id}/desativar")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    public ResponseEntity<DesativarUsuarioUseCase.ResultadoDesativacao> desativar(
            @PathVariable UUID id,
            @RequestBody(required = false) DesativarRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID adminId = extractUsuarioId(jwt);
        String motivo = request != null ? request.motivo() : null;
        var comando = new DesativarUsuarioUseCase.ComandoDesativar(id, motivo);

        var resultado = desativarUsuarioUseCase.desativar(comando, adminId);
        return ResponseEntity.ok(resultado);
    }

    /**
     * POST /api/v1/usuarios/{id}/reativar — Reativar conta (UC-ACE-05).
     * Permission: ACE → EDITAR
     */
    @PostMapping("/{id}/reativar")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    public ResponseEntity<DesativarUsuarioUseCase.ResultadoReativacao> reativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID adminId = extractUsuarioId(jwt);
        var resultado = desativarUsuarioUseCase.reativar(id, adminId);
        return ResponseEntity.ok(resultado);
    }

    /**
     * POST /api/v1/usuarios/{id}/reenviar-ativacao — Reenviar e-mail de ativação (UC-ACE-01 7a).
     * Permission: ACE → EDITAR
     */
    @PostMapping("/{id}/reenviar-ativacao")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    public ResponseEntity<Map<String, String>> reenviarAtivacao(@PathVariable UUID id) {
        // TODO: implementar lógica de reenvio (depende de T-019 CadastrarUsuarioUseCaseImpl)
        // Por ora, retorna placeholder
        return ResponseEntity.ok(Map.of("message", "Reenvio de ativação pendente de implementação (T-019)."));
    }

    /**
     * POST /api/v1/usuarios/{id}/forcar-reset-senha — Admin força reset (UC-ACE-06).
     * Permission: ACE → EDITAR
     */
    @PostMapping("/{id}/forcar-reset-senha")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    public ResponseEntity<ResetarSenhaUseCase.Resultado> forcarResetSenha(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID adminId = extractUsuarioId(jwt);
        var resultado = resetarSenhaUseCase.forcarReset(id, adminId);
        return ResponseEntity.ok(resultado);
    }

    private UUID extractUsuarioId(Jwt jwt) {
        String sub = jwt.getClaimAsString("usuario_id");
        if (sub == null) {
            sub = jwt.getSubject();
        }
        return UUID.fromString(sub);
    }

    // Request DTOs

    record CadastrarRequest(
            String nomeCompleto,
            String email,
            String cpf,
            String perfil,
            List<UUID> empreendimentoIds
    ) {}

    record AtualizarRequest(
            String perfil,
            List<UUID> empreendimentoIds
    ) {}

    record DesativarRequest(
            String motivo
    ) {}
}
