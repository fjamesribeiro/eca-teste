package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence;

import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.EventoAcessoRepository;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.EventoAcessoJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.EventoAcessoSpringDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Adapter que implementa o port de domínio EventoAcessoRepository.
 * Append-only — sem UPDATE ou DELETE (RN-AUD-01).
 */
@Repository
@Transactional
public class EventoAcessoRepositoryAdapter implements EventoAcessoRepository {

    private final EventoAcessoSpringDataRepository jpaRepo;
    private final ObjectMapper objectMapper;

    public EventoAcessoRepositoryAdapter(EventoAcessoSpringDataRepository jpaRepo,
                                          ObjectMapper objectMapper) {
        this.jpaRepo = jpaRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public EventoAcesso salvar(EventoAcesso evento) {
        var entity = new EventoAcessoJpaEntity();
        entity.setUsuarioId(evento.getUsuarioId());
        entity.setTipoEvento(evento.getTipoEvento().name());
        entity.setResultado(evento.getResultado().name());
        entity.setIpOrigem(evento.getIpOrigem());
        entity.setDispositivo(evento.getDispositivo());
        entity.setNavegador(evento.getNavegador());
        entity.setCriadoEm(evento.getCriadoEm());
        entity.setEmailTentativa(evento.getEmailTentativa());

        if (evento.getDetalhes() != null && !evento.getDetalhes().isEmpty()) {
            try {
                entity.setDetalhes(objectMapper.writeValueAsString(evento.getDetalhes()));
            } catch (JsonProcessingException e) {
                entity.setDetalhes("{}");
            }
        }

        EventoAcessoJpaEntity saved = jpaRepo.save(entity);

        return EventoAcesso.reconstituir(
                saved.getId(),
                evento.getUsuarioId(),
                evento.getTipoEvento(),
                evento.getResultado(),
                evento.getIpOrigem(),
                evento.getDispositivo(),
                evento.getNavegador(),
                evento.getDetalhes(),
                evento.getCriadoEm(),
                evento.getEmailTentativa()
        );
    }
}
