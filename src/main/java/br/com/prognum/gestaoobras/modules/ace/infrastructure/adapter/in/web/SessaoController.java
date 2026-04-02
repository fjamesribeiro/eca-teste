package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.EncerrarSessaoUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.SessaoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller para gestão de sessões (ET_ACE seção 3.8).
 * Base path: /api/v1/usuarios/{usuarioId}/sessoes
 * Actor: Administrador (UC-ACE-07)
 */
@RestController
@RequestMapping("/api/v1/usuarios/{usuarioId}/sessoes")
public class SessaoController {

    private final EncerrarSessaoUseCase encerrarSessaoUseCase;
    private final SessaoRepository sessaoRepository;
    private final UsuarioRepository usuarioRepository;

    public SessaoController(EncerrarSessaoUseCase encerrarSessaoUseCase,
                             SessaoRepository sessaoRepository,
                             UsuarioRepository usuarioRepository) {
        this.encerrarSessaoUseCase = encerrarSessaoUseCase;
        this.sessaoRepository = sessaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * GET /api/v1/usuarios/{usuarioId}/sessoes — Lista sessões ativas (UC-ACE-07 passo 2).
     * Permission: ACE → VISUALIZAR
     */
    @GetMapping
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'VISUALIZAR')")
    public ResponseEntity<SessoesResponse> listarSessoesAtivas(@PathVariable UUID usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        List<Sessao> sessoesAtivas = sessaoRepository.findAtivasByUsuarioId(usuarioId);

        List<SessaoDto> sessoes = sessoesAtivas.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(new SessoesResponse(sessoes));
    }

    /**
     * DELETE /api/v1/usuarios/{usuarioId}/sessoes/{sessaoId} — Encerrar sessão individual (UC-ACE-07).
     * Permission: ACE → EDITAR
     */
    @DeleteMapping("/{sessaoId}")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    public ResponseEntity<EncerrarSessaoUseCase.ResultadoEncerramento> encerrar(
            @PathVariable UUID usuarioId,
            @PathVariable UUID sessaoId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID adminId = extractUsuarioId(jwt);
        var resultado = encerrarSessaoUseCase.encerrar(usuarioId, sessaoId, adminId);
        return ResponseEntity.ok(resultado);
    }

    /**
     * DELETE /api/v1/usuarios/{usuarioId}/sessoes — Encerrar todas as sessões (UC-ACE-07).
     * Permission: ACE → EDITAR
     */
    @DeleteMapping
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    public ResponseEntity<EncerrarSessaoUseCase.ResultadoEncerramentoTodas> encerrarTodas(
            @PathVariable UUID usuarioId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID adminId = extractUsuarioId(jwt);
        var resultado = encerrarSessaoUseCase.encerrarTodas(usuarioId, adminId);
        return ResponseEntity.ok(resultado);
    }

    private UUID extractUsuarioId(Jwt jwt) {
        String sub = jwt.getClaimAsString("usuario_id");
        if (sub == null) {
            sub = jwt.getSubject();
        }
        return UUID.fromString(sub);
    }

    private SessaoDto toDto(Sessao sessao) {
        String duracao = formatarDuracao(Duration.between(sessao.getCriadaEm(), Instant.now()));
        return new SessaoDto(
                sessao.getId(),
                sessao.getDispositivo(),
                sessao.getNavegador(),
                sessao.getIpOrigem(),
                sessao.getCriadaEm().toString(),
                sessao.getUltimaAtividade().toString(),
                duracao
        );
    }

    private String formatarDuracao(Duration duracao) {
        long horas = duracao.toHours();
        long minutos = duracao.toMinutesPart();
        if (horas > 0) {
            return horas + "h " + minutos + "min";
        }
        return minutos + "min";
    }

    record SessoesResponse(List<SessaoDto> sessoes) {}

    record SessaoDto(
            UUID id,
            String dispositivo,
            String navegador,
            String ipOrigem,
            String criadaEm,
            String ultimaAtividade,
            String duracao
    ) {}
}
