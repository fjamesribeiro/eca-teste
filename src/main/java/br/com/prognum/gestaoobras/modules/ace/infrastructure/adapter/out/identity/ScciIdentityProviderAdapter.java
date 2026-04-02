package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.identity;

import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adapter stub para o Identity Provider SCCI Prognum (ET_ACE seção 7.2).
 *
 * GAP-ACE-02: API SCCI não documentada. Este stub lança UnsupportedOperationException
 * em todos os métodos até que a documentação seja fornecida pela Prognum.
 *
 * Ativado via property: ace.scci.enabled=true
 * Em dev/staging, desativar para usar apenas Cognito.
 */
@Component
@ConditionalOnProperty(name = "ace.scci.enabled", havingValue = "true")
public class ScciIdentityProviderAdapter implements IdentityProviderPort {

    private static final Logger log = LoggerFactory.getLogger(ScciIdentityProviderAdapter.class);
    private static final String MSG = "SCCI Identity Provider não implementado — aguardando documentação da API (GAP-ACE-02)";

    @Override
    public CriarUsuarioResult criarUsuario(String email, String nomeCompleto) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void definirSenha(String providerUserId, String senha) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public MfaSetupResult iniciarSetupMfa(String providerUserId) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public boolean confirmarMfa(String providerUserId, String codigoTotp) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public TokenResult trocarCodePorTokens(String code, String redirectUri) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void desativarUsuario(String providerUserId) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void reativarUsuario(String providerUserId) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void forcarResetSenha(String providerUserId) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public boolean validarSenhaAtual(String providerUserId, String senhaAtual) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void alterarSenha(String providerUserId, String novaSenha) {
        log.error(MSG);
        throw new UnsupportedOperationException(MSG);
    }
}
