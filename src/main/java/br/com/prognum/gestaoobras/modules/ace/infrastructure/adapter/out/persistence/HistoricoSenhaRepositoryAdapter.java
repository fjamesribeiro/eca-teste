package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence;

import br.com.prognum.gestaoobras.modules.ace.domain.model.HistoricoSenha;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.HistoricoSenhaRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.HistoricoSenhaJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.HistoricoSenhaSpringDataRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Adapter que implementa o port de domínio HistoricoSenhaRepository.
 */
@Repository
@Transactional(readOnly = true)
public class HistoricoSenhaRepositoryAdapter implements HistoricoSenhaRepository {

    private final HistoricoSenhaSpringDataRepository jpaRepo;

    public HistoricoSenhaRepositoryAdapter(HistoricoSenhaSpringDataRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    @Transactional
    public HistoricoSenha salvar(HistoricoSenha historico) {
        var entity = new HistoricoSenhaJpaEntity();
        entity.setUsuarioId(historico.getUsuarioId());
        entity.setSenhaHash(historico.getSenhaHash());
        entity.setCriadoEm(historico.getCriadoEm());

        HistoricoSenhaJpaEntity saved = jpaRepo.save(entity);

        return HistoricoSenha.reconstituir(
                saved.getId(),
                historico.getUsuarioId(),
                historico.getSenhaHash(),
                historico.getCriadoEm()
        );
    }

    @Override
    public List<HistoricoSenha> findUltimasByUsuarioId(UUID usuarioId, int quantidade) {
        return jpaRepo.findUltimasByUsuarioId(usuarioId, quantidade).stream()
                .map(e -> HistoricoSenha.reconstituir(
                        e.getId(), e.getUsuarioId(), e.getSenhaHash(), e.getCriadoEm()))
                .toList();
    }
}
