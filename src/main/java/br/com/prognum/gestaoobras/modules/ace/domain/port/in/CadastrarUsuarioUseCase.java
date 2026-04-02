package br.com.prognum.gestaoobras.modules.ace.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * UC-ACE-01: Cadastrar novo usuário (RN-ACE-03).
 * Somente Administrador pode executar.
 */
public interface CadastrarUsuarioUseCase {

    record Comando(
            String nomeCompleto,
            String email,
            String cpf,
            String perfil,
            List<UUID> empreendimentoIds
    ) {}

    record Resultado(
            UUID id,
            String nomeCompleto,
            String email,
            String cpf,
            String perfil,
            String statusConta,
            List<UUID> empreendimentoIds,
            String criadoEm,
            UUID criadoPor
    ) {}

    Resultado executar(Comando comando, UUID adminId);
}
