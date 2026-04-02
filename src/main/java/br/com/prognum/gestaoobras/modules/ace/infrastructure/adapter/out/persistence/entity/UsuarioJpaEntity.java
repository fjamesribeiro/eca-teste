package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapeando a tabela ace_usuario (V001).
 * Não contém lógica de domínio — apenas mapeamento ORM.
 */
@Entity
@Table(name = "ace_usuario")
public class UsuarioJpaEntity {

    @Id
    @Column(name = "id", updatable = false)
    private UUID id;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "perfil", nullable = false, length = 50)
    private String perfil;

    @Column(name = "status_conta", nullable = false, length = 30)
    private String statusConta;

    @Column(name = "provider_type", nullable = false, length = 20)
    private String providerType;

    @Column(name = "provider_user_id")
    private String providerUserId;

    @Column(name = "mfa_configurado", nullable = false)
    private boolean mfaConfigurado;

    @Column(name = "tentativas_login_falhas", nullable = false)
    private int tentativasLoginFalhas;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    @Column(name = "ultimo_acesso")
    private Instant ultimoAcesso;

    @Column(name = "ultima_troca_senha")
    private Instant ultimaTrocaSenha;

    @Column(name = "token_ativacao")
    private String tokenAtivacao;

    @Column(name = "token_ativacao_expira_em")
    private Instant tokenAtivacaoExpiraEm;

    @Column(name = "criado_por", nullable = false, updatable = false)
    private UUID criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "motivo_desativacao", length = 500)
    private String motivoDesativacao;

    protected UsuarioJpaEntity() {
    }

    // Getters e setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getStatusConta() { return statusConta; }
    public void setStatusConta(String statusConta) { this.statusConta = statusConta; }

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }

    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    public boolean isMfaConfigurado() { return mfaConfigurado; }
    public void setMfaConfigurado(boolean mfaConfigurado) { this.mfaConfigurado = mfaConfigurado; }

    public int getTentativasLoginFalhas() { return tentativasLoginFalhas; }
    public void setTentativasLoginFalhas(int tentativasLoginFalhas) { this.tentativasLoginFalhas = tentativasLoginFalhas; }

    public Instant getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(Instant bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }

    public Instant getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(Instant ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }

    public Instant getUltimaTrocaSenha() { return ultimaTrocaSenha; }
    public void setUltimaTrocaSenha(Instant ultimaTrocaSenha) { this.ultimaTrocaSenha = ultimaTrocaSenha; }

    public String getTokenAtivacao() { return tokenAtivacao; }
    public void setTokenAtivacao(String tokenAtivacao) { this.tokenAtivacao = tokenAtivacao; }

    public Instant getTokenAtivacaoExpiraEm() { return tokenAtivacaoExpiraEm; }
    public void setTokenAtivacaoExpiraEm(Instant tokenAtivacaoExpiraEm) { this.tokenAtivacaoExpiraEm = tokenAtivacaoExpiraEm; }

    public UUID getCriadoPor() { return criadoPor; }
    public void setCriadoPor(UUID criadoPor) { this.criadoPor = criadoPor; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getMotivoDesativacao() { return motivoDesativacao; }
    public void setMotivoDesativacao(String motivoDesativacao) { this.motivoDesativacao = motivoDesativacao; }
}
