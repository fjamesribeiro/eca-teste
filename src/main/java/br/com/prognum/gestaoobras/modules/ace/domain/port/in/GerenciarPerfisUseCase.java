package br.com.prognum.gestaoobras.modules.ace.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * UC-ACE-04: Gerenciar perfis e vinculações (RN-ACE-01, RN-ACE-06).
 * Somente Administrador pode alterar perfil e vinculações de um usuário.
 */
public interface GerenciarPerfisUseCase {

    record Comando(
            UUID usuarioId,
            String perfil,
            List<UUID> empreendimentoIds
    ) {}

    record Resultado(
            UUID id,
            String perfil,
            List<UUID> empreendimentoIds,
            String atualizadoEm
    ) {}

    Resultado executar(Comando comando, UUID adminId);
}
