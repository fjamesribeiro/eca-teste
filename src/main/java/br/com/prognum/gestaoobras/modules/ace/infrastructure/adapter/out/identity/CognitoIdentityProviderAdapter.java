package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.identity;

import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

/**
 * Adapter do Identity Provider para AWS Cognito (ET_ACE seção 7.1).
 * Usado por usuários externos (AGENTE_PROMOTOR) com provider_type = COGNITO.
 *
 * GAP-ACE-03 RESOLVIDO: DIRETOR_FINANCEIRO e TESOURARIA usam SCCI, não Cognito.
 */
@Component
@ConditionalOnProperty(name = "ace.cognito.enabled", havingValue = "true", matchIfMissing = true)
public class CognitoIdentityProviderAdapter implements IdentityProviderPort {

    private static final Logger log = LoggerFactory.getLogger(CognitoIdentityProviderAdapter.class);

    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;
    private final String clientId;
    private final String clientSecret;
    private final String tokenEndpoint;

    public CognitoIdentityProviderAdapter(
            CognitoIdentityProviderClient cognitoClient,
            @Value("${ace.cognito.user-pool-id}") String userPoolId,
            @Value("${ace.cognito.client-id}") String clientId,
            @Value("${ace.cognito.client-secret:}") String clientSecret,
            @Value("${ace.cognito.token-endpoint:}") String tokenEndpoint) {
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpoint = tokenEndpoint;
    }

    @Override
    public CriarUsuarioResult criarUsuario(String email, String nomeCompleto) {
        AdminCreateUserRequest request = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .userAttributes(
                        AttributeType.builder().name("email").value(email).build(),
                        AttributeType.builder().name("email_verified").value("true").build(),
                        AttributeType.builder().name("name").value(nomeCompleto).build()
                )
                .messageAction(MessageActionType.SUPPRESS) // não enviar e-mail do Cognito
                .build();

        AdminCreateUserResponse response = cognitoClient.adminCreateUser(request);
        String providerUserId = response.user().username();

        log.info("Usuário criado no Cognito: {}", providerUserId);
        return new CriarUsuarioResult(providerUserId);
    }

    @Override
    public void definirSenha(String providerUserId, String senha) {
        AdminSetUserPasswordRequest request = AdminSetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(providerUserId)
                .password(senha)
                .permanent(true)
                .build();

        cognitoClient.adminSetUserPassword(request);
        log.info("Senha definida no Cognito para: {}", providerUserId);
    }

    @Override
    public MfaSetupResult iniciarSetupMfa(String providerUserId) {
        AssociateSoftwareTokenRequest request = AssociateSoftwareTokenRequest.builder()
                .accessToken(providerUserId) // Em produção, usar session token do challenge
                .build();

        AssociateSoftwareTokenResponse response = cognitoClient.associateSoftwareToken(request);
        String secret = response.secretCode();
        String qrCodeUri = "otpauth://totp/GestaoObras:" + providerUserId + "?secret=" + secret + "&issuer=CDHU";

        log.info("MFA setup iniciado para: {}", providerUserId);
        return new MfaSetupResult(qrCodeUri, secret);
    }

    @Override
    public boolean confirmarMfa(String providerUserId, String codigoTotp) {
        try {
            VerifySoftwareTokenRequest request = VerifySoftwareTokenRequest.builder()
                    .accessToken(providerUserId)
                    .userCode(codigoTotp)
                    .friendlyDeviceName("TOTP-App")
                    .build();

            VerifySoftwareTokenResponse response = cognitoClient.verifySoftwareToken(request);
            return response.status() == VerifySoftwareTokenResponseType.SUCCESS;
        } catch (Exception e) {
            log.warn("Falha na verificação TOTP para {}: {}", providerUserId, e.getMessage());
            return false;
        }
    }

    @Override
    public TokenResult trocarCodePorTokens(String code, String redirectUri) {
        // Em produção: HTTP POST para o token endpoint do Cognito
        // com grant_type=authorization_code, code, redirect_uri, client_id, client_secret.
        // Simplificação: delegar para Spring Security OAuth2 Client.
        log.info("Trocando authorization code por tokens no Cognito");
        // Placeholder — a integração real usa RestTemplate/WebClient para o token endpoint
        return new TokenResult("access-token-placeholder", "refresh-token-placeholder", 28800);
    }

    @Override
    public void desativarUsuario(String providerUserId) {
        AdminDisableUserRequest request = AdminDisableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(providerUserId)
                .build();

        cognitoClient.adminDisableUser(request);
        log.info("Usuário desativado no Cognito: {}", providerUserId);
    }

    @Override
    public void reativarUsuario(String providerUserId) {
        AdminEnableUserRequest request = AdminEnableUserRequest.builder()
                .userPoolId(userPoolId)
                .username(providerUserId)
                .build();

        cognitoClient.adminEnableUser(request);
        log.info("Usuário reativado no Cognito: {}", providerUserId);
    }

    @Override
    public void forcarResetSenha(String providerUserId) {
        AdminResetUserPasswordRequest request = AdminResetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(providerUserId)
                .build();

        cognitoClient.adminResetUserPassword(request);
        log.info("Reset de senha forçado no Cognito: {}", providerUserId);
    }

    @Override
    public boolean validarSenhaAtual(String providerUserId, String senhaAtual) {
        try {
            AdminInitiateAuthRequest request = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .authParameters(java.util.Map.of(
                            "USERNAME", providerUserId,
                            "PASSWORD", senhaAtual
                    ))
                    .build();

            cognitoClient.adminInitiateAuth(request);
            return true;
        } catch (NotAuthorizedException e) {
            return false;
        }
    }

    @Override
    public void alterarSenha(String providerUserId, String novaSenha) {
        definirSenha(providerUserId, novaSenha);
    }
}
