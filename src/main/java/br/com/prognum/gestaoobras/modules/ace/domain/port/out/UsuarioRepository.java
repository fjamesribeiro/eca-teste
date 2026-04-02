package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ProviderType;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de saída para persistência de usuários.
 * Implementado na camada de infraestrutura via JPA.
 *
 * Note: Usuario domain model (T-010) está bloqueado por GAP-ACE-01.
 * Quando T-010 for implementado, métodos de save/find retornarão o tipo Usuario.
 * Por ora, contratos usam record DadosUsuario para operações de leitura/escrita.
 */
public interface UsuarioRepository {

    /** Projeção de leitura do usuário enquanto T-010 está bloqueado. */
    record DadosUsuario(
            UUID id,
            String nomeCompleto,
            String email,
            String cpf,
            Perfil perfil,
            StatusConta statusConta,
            ProviderType providerType,
            String providerUserId,
            boolean mfaConfigurado,
            int tentativasLoginFalhas,
            Instant bloqueadoAte,
            Instant ultimoAcesso,
            Instant ultimaTrocaSenha,
            String tokenAtivacao,
            Instant tokenAtivacaoExpiraEm,
            UUID criadoPor,
            Instant criadoEm,
            Instant atualizadoEm,
            String motivoDesativacao
    ) {}

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<UUID> findIdByEmail(String email);

    Optional<Perfil> findPerfilById(UUID id);

    Optional<StatusConta> findStatusContaById(UUID id);

    Optional<DadosUsuario> findById(UUID id);

    Optional<DadosUsuario> findByTokenAtivacao(String tokenAtivacao);

    Optional<DadosUsuario> findByProviderUserId(String providerUserId);

    void atualizarStatusConta(UUID id, StatusConta novoStatus);

    void atualizarMfaConfigurado(UUID id, boolean mfaConfigurado);

    void atualizarProviderUserId(UUID id, String providerUserId);

    void atualizarUltimoAcesso(UUID id, Instant ultimoAcesso);

    void atualizarUltimaTrocaSenha(UUID id, Instant ultimaTrocaSenha);

    void atualizarTokenAtivacao(UUID id, String tokenAtivacao, Instant expiraEm);

    void atualizarMotivoDesativacao(UUID id, String motivo);

    void atualizarTentativasLoginFalhas(UUID id, int tentativas, Instant bloqueadoAte);
}
