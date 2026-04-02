package br.com.prognum.gestaoobras.modules.ace.domain.port.in;

import java.util.UUID;

/**
 * UC-ACE-05: Desativar/reativar usuário (RN-ACE-09).
 * Somente Administrador pode executar.
 */
public interface DesativarUsuarioUseCase {

    record ComandoDesativar(
            UUID usuarioId,
            String motivo
    ) {}

    record ResultadoDesativacao(
            UUID id,
            String statusConta,
            int sessoesEncerradas,
            String mensagem
    ) {}

    record ResultadoReativacao(
            UUID id,
            String statusConta,
            String mensagem
    ) {}

    ResultadoDesativacao desativar(ComandoDesativar comando, UUID adminId);

    ResultadoReativacao reativar(UUID usuarioId, UUID adminId);
}
