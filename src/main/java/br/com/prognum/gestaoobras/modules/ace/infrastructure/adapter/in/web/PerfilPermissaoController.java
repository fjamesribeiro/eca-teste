package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.PerfilPermissaoJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.PerfilPermissaoSpringDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller para gestão de perfis e permissões (ET_ACE seção 3.9).
 * Base path: /api/v1/ace
 * Actor: Administrador
 */
@RestController
@RequestMapping("/api/v1/ace")
public class PerfilPermissaoController {

    private final PerfilPermissaoSpringDataRepository perfilPermissaoRepo;

    public PerfilPermissaoController(PerfilPermissaoSpringDataRepository perfilPermissaoRepo) {
        this.perfilPermissaoRepo = perfilPermissaoRepo;
    }

    /**
     * GET /api/v1/ace/perfis — Lista os 8 perfis com suas permissões (RN-ACE-01, RN-ACE-02).
     */
    @GetMapping("/perfis")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'VISUALIZAR')")
    public ResponseEntity<List<PerfilComPermissoes>> listarPerfis() {
        List<PerfilComPermissoes> perfis = Arrays.stream(Perfil.values())
                .map(perfil -> {
                    List<PerfilPermissaoJpaEntity> permissoes =
                            perfilPermissaoRepo.findByPerfilAndPermitidoTrue(perfil.name());

                    Map<String, List<String>> porModulo = permissoes.stream()
                            .collect(Collectors.groupingBy(
                                    PerfilPermissaoJpaEntity::getModulo,
                                    Collectors.mapping(PerfilPermissaoJpaEntity::getAcao, Collectors.toList())
                            ));

                    List<ModuloPermissoes> moduloPermissoes = porModulo.entrySet().stream()
                            .map(e -> new ModuloPermissoes(e.getKey(), e.getValue()))
                            .toList();

                    return new PerfilComPermissoes(
                            perfil.name(),
                            perfil.possuiVisaoGlobal(),
                            perfil.exigeVinculacao(),
                            moduloPermissoes
                    );
                })
                .toList();

        return ResponseEntity.ok(perfis);
    }

    /**
     * PUT /api/v1/ace/perfis/{perfil}/permissoes — Atualiza matriz de permissões (RN-ACE-02).
     */
    @PutMapping("/perfis/{perfil}/permissoes")
    @PreAuthorize("@acePermissionEvaluator.hasPermission(authentication, 'ACE', 'EDITAR')")
    @Transactional
    public ResponseEntity<Map<String, String>> atualizarPermissoes(
            @PathVariable String perfil,
            @RequestBody List<PermissaoUpdate> permissoes,
            @AuthenticationPrincipal Jwt jwt) {

        // Validar que o perfil existe
        try {
            Perfil.valueOf(perfil);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Perfil inválido: " + perfil));
        }

        UUID adminId = extractUsuarioId(jwt);
        Instant agora = Instant.now();

        for (PermissaoUpdate update : permissoes) {
            perfilPermissaoRepo.atualizarPermissao(
                    perfil, update.modulo(), update.acao(), update.permitido(), adminId, agora);
        }

        return ResponseEntity.ok(Map.of("mensagem", "Permissões atualizadas."));
    }

    private UUID extractUsuarioId(Jwt jwt) {
        String sub = jwt.getClaimAsString("usuario_id");
        if (sub == null) {
            sub = jwt.getSubject();
        }
        return UUID.fromString(sub);
    }

    // DTOs

    record PerfilComPermissoes(
            String perfil,
            boolean visaoGlobal,
            boolean exigeVinculacao,
            List<ModuloPermissoes> permissoes
    ) {}

    record ModuloPermissoes(String modulo, List<String> acoes) {}

    record PermissaoUpdate(String modulo, String acao, boolean permitido) {}
}
