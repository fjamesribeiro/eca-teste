package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.email;

import br.com.prognum.gestaoobras.modules.ace.domain.port.out.EmailPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

/**
 * Adapter que implementa EmailPort usando AWS SES.
 * Templates: ativação de conta e reset de senha (ET_ACE seção 7.3).
 */
@Component
public class SesEmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(SesEmailAdapter.class);

    private final SesClient sesClient;
    private final String remetente;
    private final String baseUrl;

    public SesEmailAdapter(SesClient sesClient,
                            @Value("${ace.email.remetente}") String remetente,
                            @Value("${ace.email.base-url}") String baseUrl) {
        this.sesClient = sesClient;
        this.remetente = remetente;
        this.baseUrl = baseUrl;
    }

    @Override
    public void enviarEmailAtivacao(String destinatario, String nomeCompleto,
                                     String tokenAtivacao, String linkAtivacao) {
        String assunto = "Sistema de Gestão de Obras — Ative sua conta";
        String corpo = buildCorpoAtivacao(nomeCompleto, linkAtivacao);

        enviar(destinatario, assunto, corpo);
        log.info("E-mail de ativação enviado para {}", destinatario);
    }

    @Override
    public void enviarEmailResetSenha(String destinatario, String nomeCompleto, String linkReset) {
        String assunto = "Sistema de Gestão de Obras — Redefinição de senha";
        String corpo = buildCorpoResetSenha(nomeCompleto, linkReset);

        enviar(destinatario, assunto, corpo);
        log.info("E-mail de reset de senha enviado para {}", destinatario);
    }

    private void enviar(String destinatario, String assunto, String corpoHtml) {
        SendEmailRequest request = SendEmailRequest.builder()
                .source(remetente)
                .destination(Destination.builder()
                        .toAddresses(destinatario)
                        .build())
                .message(Message.builder()
                        .subject(Content.builder().data(assunto).charset("UTF-8").build())
                        .body(Body.builder()
                                .html(Content.builder().data(corpoHtml).charset("UTF-8").build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }

    private String buildCorpoAtivacao(String nomeCompleto, String linkAtivacao) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Bem-vindo ao Sistema de Gestão de Obras — CDHU</h2>
                    <p>Olá, <strong>%s</strong>,</p>
                    <p>Sua conta foi criada pelo Administrador. Para ativá-la, clique no link abaixo:</p>
                    <p><a href="%s" style="background-color: #0066cc; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;">Ativar Conta</a></p>
                    <p>Este link é válido por <strong>72 horas</strong>.</p>
                    <p>Após ativar, você precisará:</p>
                    <ol>
                        <li>Definir sua senha (mínimo 12 caracteres com maiúscula, minúscula, número e caractere especial)</li>
                        <li>Configurar o autenticador MFA (TOTP) no seu aplicativo</li>
                    </ol>
                    <p>Se você não solicitou esta conta, ignore este e-mail.</p>
                    <hr>
                    <p style="font-size: 12px; color: #999;">Prognum Informática — CDHU</p>
                </body>
                </html>
                """.formatted(nomeCompleto, linkAtivacao);
    }

    private String buildCorpoResetSenha(String nomeCompleto, String linkReset) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Redefinição de Senha — Sistema de Gestão de Obras</h2>
                    <p>Olá, <strong>%s</strong>,</p>
                    <p>O Administrador solicitou a redefinição da sua senha. Clique no link abaixo para definir uma nova:</p>
                    <p><a href="%s" style="background-color: #0066cc; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;">Redefinir Senha</a></p>
                    <p>Se você não solicitou esta alteração, entre em contato com o Administrador.</p>
                    <hr>
                    <p style="font-size: 12px; color: #999;">Prognum Informática — CDHU</p>
                </body>
                </html>
                """.formatted(nomeCompleto, linkReset);
    }
}
