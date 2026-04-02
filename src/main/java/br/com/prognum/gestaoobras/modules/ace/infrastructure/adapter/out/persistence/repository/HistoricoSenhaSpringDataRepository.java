package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository;

import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.HistoricoSenhaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository para ace_historico_senha.
 */
public interface HistoricoSenhaSpringDataRepository extends JpaRepository<HistoricoSenhaJpaEntity, Long> {

    @Query("SELECT h FROM HistoricoSenhaJpaEntity h WHERE h.usuarioId = :usuarioId ORDER BY h.criadoEm DESC LIMIT :quantidade")
    List<HistoricoSenhaJpaEntity> findUltimasByUsuarioId(@Param("usuarioId") UUID usuarioId,
                                                          @Param("quantidade") int quantidade);
}
