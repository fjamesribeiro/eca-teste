package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository;

import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.SessaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository para ace_sessao.
 */
public interface SessaoSpringDataRepository extends JpaRepository<SessaoJpaEntity, UUID> {

    Optional<SessaoJpaEntity> findByTokenHash(String tokenHash);

    @Query("SELECT s FROM SessaoJpaEntity s WHERE s.usuarioId = :usuarioId AND s.encerradaEm IS NULL ORDER BY s.criadaEm ASC")
    List<SessaoJpaEntity> findAtivasByUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query("SELECT COUNT(s) FROM SessaoJpaEntity s WHERE s.usuarioId = :usuarioId AND s.encerradaEm IS NULL")
    int countAtivasByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
