package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository;

import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.VinculacaoEmpreendimentoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Data JPA repository para ace_vinculacao_empreendimento.
 */
public interface VinculacaoEmpreendimentoSpringDataRepository extends JpaRepository<VinculacaoEmpreendimentoJpaEntity, UUID> {

    @Query("SELECT v FROM VinculacaoEmpreendimentoJpaEntity v WHERE v.usuarioId = :usuarioId AND v.desvinculadoEm IS NULL")
    List<VinculacaoEmpreendimentoJpaEntity> findAtivasByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT v.empreendimentoId FROM VinculacaoEmpreendimentoJpaEntity v WHERE v.usuarioId = :usuarioId AND v.desvinculadoEm IS NULL")
    Set<UUID> findEmpreendimentoIdsAtivosByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
