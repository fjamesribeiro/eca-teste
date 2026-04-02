package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de saída para persistência de sessões.
 */
public interface SessaoRepository {

    Sessao salvar(Sessao sessao);

    Optional<Sessao> findById(UUID id);

    Optional<Sessao> findByTokenHash(String tokenHash);

    List<Sessao> findAtivasByUsuarioId(UUID usuarioId);

    int countAtivasByUsuarioId(UUID usuarioId);
}
