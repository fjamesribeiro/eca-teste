package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.VinculacaoEmpreendimento;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.GerenciarPerfisUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.UsuarioJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.UsuarioSpringDataRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UC-ACE-04: Gerenciar perfis e vinculações.
 *
 * 1 perfil por conta (GAP-ACE-01 RESOLVIDO).
 * Altera perfil e/ou vinculações. Registra evento ALTERACAO_PERFIL.
 *
 * Source: ET_ACE seção 3.5
 */
public class GerenciarPerfisUseCaseImpl implements GerenciarPerfisUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioSpringDataRepository usuarioJpaRepo;
    private final VinculacaoEmpreendimentoRepository vinculacaoRepository;
    private final AuditTrailPort auditTrail;

    public GerenciarPerfisUseCaseImpl(UsuarioRepository usuarioRepository,
                                       UsuarioSpringDataRepository usuarioJpaRepo,
                                       VinculacaoEmpreendimentoRepository vinculacaoRepository,
                                       AuditTrailPort auditTrail) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioJpaRepo = usuarioJpaRepo;
        this.vinculacaoRepository = vinculacaoRepository;
        this.auditTrail = auditTrail;
    }

    @Override
    public Resultado executar(Comando comando, UUID adminId) {
        // 1. Buscar usuário
        DadosUsuario usuario = usuarioRepository.findById(comando.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(comando.usuarioId()));

        String perfilAnterior = usuario.perfil().name();

        // 2. Determinar novo perfil
        Perfil novoPerfil;
        if (comando.perfil() != null && !comando.perfil().isBlank()) {
            try {
                novoPerfil = Perfil.valueOf(comando.perfil());
            } catch (IllegalArgumentException e) {
                throw new CadastrarUsuarioUseCaseImpl.PerfilInvalidoException(comando.perfil());
            }
        } else {
            novoPerfil = usuario.perfil();
        }

        // 3. Validar vinculação (RN-ACE-06)
        List<UUID> novosEmpreendimentos = comando.empreendimentoIds() != null
                ? comando.empreendimentoIds() : List.of();
        if (novoPerfil.exigeVinculacao() && novosEmpreendimentos.isEmpty()) {
            throw new CadastrarUsuarioUseCaseImpl.VinculacaoObrigatoriaException(novoPerfil);
        }

        // 4. Atualizar perfil no banco
        if (novoPerfil != usuario.perfil()) {
            UsuarioJpaEntity entity = usuarioJpaRepo.findById(usuario.id()).orElseThrow();
            entity.setPerfil(novoPerfil.name());
            entity.setProviderType(novoPerfil.providerTypePadrao().name());
            entity.setAtualizadoEm(Instant.now());
            usuarioJpaRepo.save(entity);
        }

        // 5. Atualizar vinculações: desvincular antigas, criar novas
        var vinculacoesAtuais = vinculacaoRepository.findAtivasByUsuarioId(usuario.id());
        Set<UUID> idsAtuais = vinculacoesAtuais.stream()
                .map(VinculacaoEmpreendimento::getEmpreendimentoId)
                .collect(Collectors.toSet());
        Set<UUID> idsNovos = Set.copyOf(novosEmpreendimentos);

        // Desvincular os que não estão mais na lista
        for (VinculacaoEmpreendimento v : vinculacoesAtuais) {
            if (!idsNovos.contains(v.getEmpreendimentoId())) {
                v.desvincular();
                vinculacaoRepository.salvar(v);
            }
        }

        // Criar novas vinculações
        for (UUID empId : idsNovos) {
            if (!idsAtuais.contains(empId)) {
                vinculacaoRepository.salvar(
                        VinculacaoEmpreendimento.criar(usuario.id(), empId, adminId));
            }
        }

        // 6. Registrar evento (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuario.id(),
                TipoEventoAcesso.ALTERACAO_PERFIL,
                ResultadoEvento.SUCESSO,
                "sistema", null, null,
                Map.of("perfil_anterior", perfilAnterior,
                        "perfil_novo", novoPerfil.name(),
                        "admin_id", adminId.toString()),
                null
        ));

        return new Resultado(
                usuario.id(),
                novoPerfil.name(),
                novosEmpreendimentos,
                Instant.now().toString()
        );
    }
}
