package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence;

import br.com.prognum.gestaoobras.modules.ace.domain.model.MotivoEncerramento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.SessaoRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.SessaoJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.SessaoSpringDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter que implementa o port de domínio SessaoRepository.
 */
@Repository
@Transactional(readOnly = true)
public class SessaoRepositoryAdapter implements SessaoRepository {

    private final SessaoSpringDataRepository jpaRepo;

    public SessaoRepositoryAdapter(SessaoSpringDataRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional
    public Sessao salvar(Sessao sessao) {
        SessaoJpaEntity entity = toEntity(sessao);
        jpaRepo.save(entity);
        return sessao;
    }

    @Override
    public Optional<Sessao> findById(UUID id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Sessao> findByTokenHash(String tokenHash) {
        return jpaRepo.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public List<Sessao> findAtivasByUsuarioId(UUID usuarioId) {
        return jpaRepo.findAtivasByUsuarioId(usuarioId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int countAtivasByUsuarioId(UUID usuarioId) {
        return jpaRepo.countAtivasByUsuarioId(usuarioId);
    }

    private SessaoJpaEntity toEntity(Sessao s) {
        var entity = new SessaoJpaEntity();
        entity.setId(s.getId());
        entity.setUsuarioId(s.getUsuarioId());
        entity.setTokenHash(s.getTokenHash());
        entity.setIpOrigem(s.getIpOrigem());
        entity.setDispositivo(s.getDispositivo());
        entity.setNavegador(s.getNavegador());
        entity.setCriadaEm(s.getCriadaEm());
        entity.setUltimaAtividade(s.getUltimaAtividade());
        entity.setEncerradaEm(s.getEncerradaEm());
        entity.setMotivoEncerramento(
                s.getMotivoEncerramento() != null ? s.getMotivoEncerramento().name() : null);
        return entity;
    }

    private Sessao toDomain(SessaoJpaEntity e) {
        return Sessao.reconstituir(
                e.getId(),
                e.getUsuarioId(),
                e.getTokenHash(),
                e.getIpOrigem(),
                e.getDispositivo(),
                e.getNavegador(),
                e.getCriadaEm(),
                e.getUltimaAtividade(),
                e.getEncerradaEm(),
                e.getMotivoEncerramento() != null
                        ? MotivoEncerramento.valueOf(e.getMotivoEncerramento()) : null
        );
    }
}
