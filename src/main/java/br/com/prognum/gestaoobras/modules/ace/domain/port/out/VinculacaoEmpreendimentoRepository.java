package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import br.com.prognum.gestaoobras.modules.ace.domain.model.VinculacaoEmpreendimento;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Port de saída para persistência de vinculações usuário-empreendimento (RN-ACE-06).
 */
public interface VinculacaoEmpreendimentoRepository {

    VinculacaoEmpreendimento salvar(VinculacaoEmpreendimento vinculacao);

    List<VinculacaoEmpreendimento> findAtivasByUsuarioId(UUID usuarioId);

    Set<UUID> findEmpreendimentoIdsAtivosByUsuarioId(UUID usuarioId);
}
