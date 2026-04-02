package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;

/**
 * Port de saída para registro na trilha de auditoria imutável (RN-ACE-07, RN-AUD-01).
 * Implementação deve ser @Async para não bloquear o fluxo principal.
 */
public interface AuditTrailPort {

    void registrar(EventoAcesso evento);
}
