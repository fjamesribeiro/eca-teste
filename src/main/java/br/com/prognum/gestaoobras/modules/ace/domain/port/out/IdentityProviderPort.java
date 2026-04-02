package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import java.util.UUID;

/**
 * Port de saída para operações no Identity Provider (Cognito ou SCCI).
 * Abstrai ambos os provedores (ADR-007, RN-ACE-10).
 */
public interface IdentityProviderPort {

    record CriarUsuarioResult(String providerUserId) {}

    record TokenResult(String accessToken, String refreshToken, long expiresIn) {}

    record MfaSetupResult(String qrCodeUri, String secret) {}

    /** Cria o usuário no IdP (AdminCreateUser no Cognito). */
    CriarUsuarioResult criarUsuario(String email, String nomeCompleto);

    /** Define a senha no IdP (AdminSetUserPassword no Cognito). */
    void definirSenha(String providerUserId, String senha);

    /** Inicia setup de MFA TOTP (AssociateSoftwareToken no Cognito). */
    MfaSetupResult iniciarSetupMfa(String providerUserId);

    /** Confirma MFA validando primeiro código TOTP (VerifySoftwareToken no Cognito). */
    boolean confirmarMfa(String providerUserId, String codigoTotp);

    /** Troca authorization code por tokens JWT. */
    TokenResult trocarCodePorTokens(String code, String redirectUri);

    /** Desativa o usuário no IdP (AdminDisableUser no Cognito). */
    void desativarUsuario(String providerUserId);

    /** Reativa o usuário no IdP (AdminEnableUser no Cognito). */
    void reativarUsuario(String providerUserId);

    /** Força reset de senha no IdP (AdminResetUserPassword no Cognito). */
    void forcarResetSenha(String providerUserId);

    /** Valida a senha atual do usuário no IdP. */
    boolean validarSenhaAtual(String providerUserId, String senhaAtual);

    /** Altera a senha do usuário no IdP. */
    void alterarSenha(String providerUserId, String novaSenha);
}
