package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository;

import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.UsuarioJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository para ace_usuario.
 * Interface interna da infraestrutura — não exposta ao domínio.
 */
public interface UsuarioSpringDataRepository extends JpaRepository<UsuarioJpaEntity, UUID> {

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    @Query("SELECT u.id FROM UsuarioJpaEntity u WHERE u.email = :email")
    Optional<UUID> findIdByEmail(@Param("email") String email);

    Optional<UsuarioJpaEntity> findByTokenAtivacao(String tokenAtivacao);

    Optional<UsuarioJpaEntity> findByProviderUserId(String providerUserId);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.statusConta = :status, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarStatusConta(@Param("id") UUID id, @Param("status") String status);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.mfaConfigurado = :mfa, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarMfaConfigurado(@Param("id") UUID id, @Param("mfa") boolean mfa);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.providerUserId = :providerUserId, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarProviderUserId(@Param("id") UUID id, @Param("providerUserId") String providerUserId);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.ultimoAcesso = :ultimoAcesso, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarUltimoAcesso(@Param("id") UUID id, @Param("ultimoAcesso") Instant ultimoAcesso);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.ultimaTrocaSenha = :ultimaTrocaSenha, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarUltimaTrocaSenha(@Param("id") UUID id, @Param("ultimaTrocaSenha") Instant ultimaTrocaSenha);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.tokenAtivacao = :token, u.tokenAtivacaoExpiraEm = :expiraEm, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarTokenAtivacao(@Param("id") UUID id, @Param("token") String token, @Param("expiraEm") Instant expiraEm);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.motivoDesativacao = :motivo, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarMotivoDesativacao(@Param("id") UUID id, @Param("motivo") String motivo);

    @Modifying
    @Query("UPDATE UsuarioJpaEntity u SET u.tentativasLoginFalhas = :tentativas, u.bloqueadoAte = :bloqueadoAte, u.atualizadoEm = CURRENT_TIMESTAMP WHERE u.id = :id")
    void atualizarTentativasLoginFalhas(@Param("id") UUID id, @Param("tentativas") int tentativas, @Param("bloqueadoAte") Instant bloqueadoAte);
}
