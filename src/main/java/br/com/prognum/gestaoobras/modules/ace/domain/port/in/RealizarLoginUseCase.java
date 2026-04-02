package br.com.prognum.gestaoobras.modules.ace.domain.port.in;

import java.util.List;
import java.util.UUID;

/**
 * UC-ACE-03: Realizar login via callback OIDC (RN-ACE-04, RN-ACE-12).
 * Processa authorization code do IdP, aplica regras de sessão e bloqueio.
 */
public interface RealizarLoginUseCase {

    record Comando(
            String code,
            String redirectUri,
            String provider,
            String ipOrigem,
            String dispositivo,
            String navegador
    ) {}

    record Resultado(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UsuarioResumo usuario
    ) {}

    record UsuarioResumo(
            UUID id,
            String nomeCompleto,
            String perfil,
            List<UUID> empreendimentoIds
    ) {}

    Resultado executar(Comando comando);
}
