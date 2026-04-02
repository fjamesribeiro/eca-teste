package br.com.prognum.gestaoobras.modules.ace.domain.port.in;

import java.util.UUID;

/**
 * UC-ACE-06: Resetar/alterar senha (RN-ACE-08).
 * Self-service (alterar própria senha) ou forçado pelo Admin.
 */
public interface ResetarSenhaUseCase {

    record ComandoAlterarSenha(
            String senhaAtual,
            String novaSenha,
            String confirmarNovaSenha,
            String codigoTotp
    ) {}

    record Resultado(String mensagem) {}

    Resultado alterarSenha(ComandoAlterarSenha comando, UUID usuarioId);

    Resultado forcarReset(UUID usuarioId, UUID adminId);
}
