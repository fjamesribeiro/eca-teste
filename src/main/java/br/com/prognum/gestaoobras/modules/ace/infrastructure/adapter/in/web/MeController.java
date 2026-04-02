package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.PerfilPermissaoJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.PerfilPermissaoSpringDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GET /api/v1/usuarios/me — Dados do usuário autenticado (ET_ACE seção 3.9).
 * Actor: Qualquer usuário autenticado
 */
@RestController
@RequestMapping("/api/v1/usuarios/me")
public class MeController {

    private final UsuarioRepository usuarioRepository;
    private final VinculacaoEmpreendimentoRepository vinculacaoRepository;
    private final PerfilPermissaoSpringDataRepository perfilPermissaoRepo;

    public MeController(UsuarioRepository usuarioRepository,
                         VinculacaoEmpreendimentoRepository vinculacaoRepository,
                         PerfilPermissaoSpringDataRepository perfilPermissaoRepo) {
        this.usuarioRepository = usuarioRepository;
        this.vinculacaoRepository = vinculacaoRepository;
        this.perfilPermissaoRepo = perfilPermissaoRepo;
    }

    @GetMapping
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID usuarioId = extractUsuarioId(jwt);

        DadosUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        Set<UUID> empreendimentoIds = vinculacaoRepository
                .findEmpreendimentoIdsAtivosByUsuarioId(usuarioId);

        List<PerfilPermissaoJpaEntity> permissoes = perfilPermissaoRepo
                .findByPerfilAndPermitidoTrue(usuario.perfil().name());

        Map<String, List<String>> porModulo = permissoes.stream()
                .collect(Collectors.groupingBy(
                        PerfilPermissaoJpaEntity::getModulo,
                        Collectors.mapping(PerfilPermissaoJpaEntity::getAcao, Collectors.toList())
                ));

        List<ModuloPermissoes> moduloPermissoes = porModulo.entrySet().stream()
                .map(e -> new ModuloPermissoes(e.getKey(), e.getValue()))
                .toList();

        return ResponseEntity.ok(new MeResponse(
                usuario.id(),
                usuario.nomeCompleto(),
                usuario.perfil().name(),
                empreendimentoIds.stream().toList(),
                moduloPermissoes
        ));
    }

    private UUID extractUsuarioId(Jwt jwt) {
        String sub = jwt.getClaimAsString("usuario_id");
        if (sub == null) {
            sub = jwt.getSubject();
        }
        return UUID.fromString(sub);
    }

    record MeResponse(
            UUID id,
            String nomeCompleto,
            String perfil,
            List<UUID> empreendimentoIds,
            List<ModuloPermissoes> permissoes
    ) {}

    record ModuloPermissoes(String modulo, List<String> acoes) {}
}
