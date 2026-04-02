package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.audit;

import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.EventoAcessoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Implementação @Async de AuditTrailPort (RN-ACE-07, RN-AUD-01).
 * Registra eventos na trilha de auditoria sem bloquear o fluxo principal.
 *
 * Requer @EnableAsync na configuração do Spring Boot.
 */
@Component
public class AuditTrailAdapter implements AuditTrailPort {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailAdapter.class);

    private final EventoAcessoRepository eventoAcessoRepository;

    public AuditTrailAdapter(EventoAcessoRepository eventoAcessoRepository) {
        this.eventoAcessoRepository = eventoAcessoRepository;
    }

    @Async
    @Override
    public void registrar(EventoAcesso evento) {
        try {
            eventoAcessoRepository.salvar(evento);
            log.debug("Evento de auditoria registrado: tipo={}, usuario={}, resultado={}",
                    evento.getTipoEvento(), evento.getUsuarioId(), evento.getResultado());
        } catch (Exception e) {
            // Falha na trilha de auditoria não deve impedir o fluxo principal,
            // mas deve ser logada para investigação (RN-AUD-01).
            log.error("Falha ao registrar evento de auditoria: tipo={}, usuario={}",
                    evento.getTipoEvento(), evento.getUsuarioId(), e);
        }
    }
}
