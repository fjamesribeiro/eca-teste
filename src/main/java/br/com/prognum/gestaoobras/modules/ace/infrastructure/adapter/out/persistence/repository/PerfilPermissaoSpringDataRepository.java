package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository;

import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.PerfilPermissaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository para ace_perfil_permissao.
 */
public interface PerfilPermissaoSpringDataRepository extends JpaRepository<PerfilPermissaoJpaEntity, Long> {

    boolean existsByPerfilAndModuloAndAcaoAndPermitidoTrue(String perfil, String modulo, String acao);

    List<PerfilPermissaoJpaEntity> findByPerfilAndPermitidoTrue(String perfil);

    List<PerfilPermissaoJpaEntity> findByPerfil(String perfil);

    @Modifying
    @Query("UPDATE PerfilPermissaoJpaEntity p SET p.permitido = :permitido, p.atualizadoPor = :adminId, p.atualizadoEm = :agora WHERE p.perfil = :perfil AND p.modulo = :modulo AND p.acao = :acao")
    int atualizarPermissao(@Param("perfil") String perfil,
                            @Param("modulo") String modulo,
                            @Param("acao") String acao,
                            @Param("permitido") boolean permitido,
                            @Param("adminId") UUID adminId,
                            @Param("agora") Instant agora);
}
