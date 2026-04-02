package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ProviderType;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.UsuarioJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.UsuarioSpringDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa o port de domínio UsuarioRepository
 * delegando para o Spring Data JPA repository.
 */
@Repository
@Transactional(readOnly = true)
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioSpringDataRepository jpaRepo;

    public UsuarioRepositoryAdapter(UsuarioSpringDataRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepo.existsByEmail(email);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepo.existsByCpf(cpf);
    }

    @Override
    public Optional<UUID> findIdByEmail(String email) {
        return jpaRepo.findIdByEmail(email);
    }

    @Override
    public Optional<Perfil> findPerfilById(UUID id) {
        return jpaRepo.findById(id)
                .map(e -> Perfil.valueOf(e.getPerfil()));
    }

    @Override
    public Optional<StatusConta> findStatusContaById(UUID id) {
        return jpaRepo.findById(id)
                .map(e -> StatusConta.valueOf(e.getStatusConta()));
    }

    @Override
    public Optional<DadosUsuario> findById(UUID id) {
        return jpaRepo.findById(id).map(this::toDadosUsuario);
    }

    @Override
    public Optional<DadosUsuario> findByTokenAtivacao(String tokenAtivacao) {
        return jpaRepo.findByTokenAtivacao(tokenAtivacao).map(this::toDadosUsuario);
    }

    @Override
    public Optional<DadosUsuario> findByProviderUserId(String providerUserId) {
        return jpaRepo.findByProviderUserId(providerUserId).map(this::toDadosUsuario);
    }

    @Override
    @Transactional
    public void atualizarStatusConta(UUID id, StatusConta novoStatus) {
        jpaRepo.atualizarStatusConta(id, novoStatus.name());
    }

    @Override
    @Transactional
    public void atualizarMfaConfigurado(UUID id, boolean mfaConfigurado) {
        jpaRepo.atualizarMfaConfigurado(id, mfaConfigurado);
    }

    @Override
    @Transactional
    public void atualizarProviderUserId(UUID id, String providerUserId) {
        jpaRepo.atualizarProviderUserId(id, providerUserId);
    }

    @Override
    @Transactional
    public void atualizarUltimoAcesso(UUID id, Instant ultimoAcesso) {
        jpaRepo.atualizarUltimoAcesso(id, ultimoAcesso);
    }

    @Override
    @Transactional
    public void atualizarUltimaTrocaSenha(UUID id, Instant ultimaTrocaSenha) {
        jpaRepo.atualizarUltimaTrocaSenha(id, ultimaTrocaSenha);
    }

    @Override
    @Transactional
    public void atualizarTokenAtivacao(UUID id, String tokenAtivacao, Instant expiraEm) {
        jpaRepo.atualizarTokenAtivacao(id, tokenAtivacao, expiraEm);
    }

    @Override
    @Transactional
    public void atualizarMotivoDesativacao(UUID id, String motivo) {
        jpaRepo.atualizarMotivoDesativacao(id, motivo);
    }

    @Override
    @Transactional
    public void atualizarTentativasLoginFalhas(UUID id, int tentativas, Instant bloqueadoAte) {
        jpaRepo.atualizarTentativasLoginFalhas(id, tentativas, bloqueadoAte);
    }

    private DadosUsuario toDadosUsuario(UsuarioJpaEntity e) {
        return new DadosUsuario(
                e.getId(),
                e.getNomeCompleto(),
                e.getEmail(),
                e.getCpf(),
                Perfil.valueOf(e.getPerfil()),
                StatusConta.valueOf(e.getStatusConta()),
                ProviderType.valueOf(e.getProviderType()),
                e.getProviderUserId(),
                e.isMfaConfigurado(),
                e.getTentativasLoginFalhas(),
                e.getBloqueadoAte(),
                e.getUltimoAcesso(),
                e.getUltimaTrocaSenha(),
                e.getTokenAtivacao(),
                e.getTokenAtivacaoExpiraEm(),
                e.getCriadoPor(),
                e.getCriadoEm(),
                e.getAtualizadoEm(),
                e.getMotivoDesativacao()
        );
    }
}
