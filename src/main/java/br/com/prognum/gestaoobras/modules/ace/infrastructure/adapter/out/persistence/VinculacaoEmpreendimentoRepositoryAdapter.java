package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence;

import br.com.prognum.gestaoobras.modules.ace.domain.model.VinculacaoEmpreendimento;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.VinculacaoEmpreendimentoJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.VinculacaoEmpreendimentoSpringDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Adapter que implementa o port de domínio VinculacaoEmpreendimentoRepository.
 */
@Repository
@Transactional(readOnly = true)
public class VinculacaoEmpreendimentoRepositoryAdapter implements VinculacaoEmpreendimentoRepository {

    private final VinculacaoEmpreendimentoSpringDataRepository jpaRepo;

    public VinculacaoEmpreendimentoRepositoryAdapter(VinculacaoEmpreendimentoSpringDataRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional
    public VinculacaoEmpreendimento salvar(VinculacaoEmpreendimento vinculacao) {
        var entity = new VinculacaoEmpreendimentoJpaEntity();
        entity.setId(vinculacao.getId());
        entity.setUsuarioId(vinculacao.getUsuarioId());
        entity.setEmpreendimentoId(vinculacao.getEmpreendimentoId());
        entity.setVinculadoPor(vinculacao.getVinculadoPor());
        entity.setVinculadoEm(vinculacao.getVinculadoEm());
        entity.setDesvinculadoEm(vinculacao.getDesvinculadoEm());

        jpaRepo.save(entity);
        return vinculacao;
    }

    @Override
    public List<VinculacaoEmpreendimento> findAtivasByUsuarioId(UUID usuarioId) {
        return jpaRepo.findAtivasByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Set<UUID> findEmpreendimentoIdsAtivosByUsuarioId(UUID usuarioId) {
        return jpaRepo.findEmpreendimentoIdsAtivosByUsuarioId(usuarioId);
    }

    private VinculacaoEmpreendimento toDomain(VinculacaoEmpreendimentoJpaEntity e) {
        return VinculacaoEmpreendimento.reconstituir(
                e.getId(),
                e.getUsuarioId(),
                e.getEmpreendimentoId(),
                e.getVinculadoPor(),
                e.getVinculadoEm(),
                e.getDesvinculadoEm()
        );
    }
}
