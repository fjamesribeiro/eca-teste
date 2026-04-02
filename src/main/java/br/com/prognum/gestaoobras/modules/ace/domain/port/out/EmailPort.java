package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

/**
 * Port de saída para envio de e-mails (AWS SES).
 * Templates: ativação de conta e reset de senha.
 */
public interface EmailPort {

    /** Envia e-mail de ativação de conta com link contendo token (RN-ACE-03). */
    void enviarEmailAtivacao(String destinatario, String nomeCompleto, String tokenAtivacao, String linkAtivacao);

    /** Envia e-mail de reset de senha com link de redefinição (UC-ACE-06). */
    void enviarEmailResetSenha(String destinatario, String nomeCompleto, String linkReset);
}
