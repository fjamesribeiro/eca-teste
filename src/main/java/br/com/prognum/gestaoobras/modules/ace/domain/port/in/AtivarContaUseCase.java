package br.com.prognum.gestaoobras.modules.ace.domain.port.in;

/**
 * UC-ACE-02: Ativar conta e configurar MFA (RN-ACE-03, RN-ACE-04).
 * Executado pelo novo usuário via token de ativação.
 */
public interface AtivarContaUseCase {

    record ComandoDefinirSenha(
            String token,
            String senha,
            String confirmarSenha
    ) {}

    record ResultadoMfaSetup(
            String qrCodeUri,
            String secret
    ) {}

    record ComandoConfirmarMfa(
            String token,
            String codigoTotp
    ) {}

    record ResultadoAtivacao(
            String status,
            String mensagem
    ) {}

    ResultadoMfaSetup definirSenha(ComandoDefinirSenha comando);

    ResultadoAtivacao confirmarMfa(ComandoConfirmarMfa comando);
}
