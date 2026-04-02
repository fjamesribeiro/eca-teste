package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;

/**
 * Port de saída para persistência de eventos de acesso (RN-ACE-07, RN-AUD-01).
 * Apenas INSERT — sem UPDATE ou DELETE.
 */
public interface EventoAcessoRepository {

    EventoAcesso salvar(EventoAcesso evento);
}
