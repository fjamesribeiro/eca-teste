package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository;

import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.EventoAcessoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository para ace_evento_acesso.
 * Apenas INSERT — sem UPDATE ou DELETE (RN-AUD-01).
 */
public interface EventoAcessoSpringDataRepository extends JpaRepository<EventoAcessoJpaEntity, Long> {
}
