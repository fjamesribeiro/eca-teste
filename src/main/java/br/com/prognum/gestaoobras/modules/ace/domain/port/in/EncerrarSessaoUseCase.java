package br.com.prognum.gestaoobras.modules.ace.domain.port.in;

import java.util.UUID;

/**
 * UC-ACE-07: Encerrar sessão remotamente (RN-ACE-12).
 * Administrador pode encerrar sessões de qualquer usuário.
 */
public interface EncerrarSessaoUseCase {

    record ResultadoEncerramento(String mensagem, UUID sessaoId) {}

    record ResultadoEncerramentoTodas(String mensagem, int sessoesEncerradas) {}

    ResultadoEncerramento encerrar(UUID usuarioId, UUID sessaoId, UUID adminId);

    ResultadoEncerramentoTodas encerrarTodas(UUID usuarioId, UUID adminId);
}
