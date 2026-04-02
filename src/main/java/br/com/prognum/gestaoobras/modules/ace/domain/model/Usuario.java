package br.com.prognum.gestaoobras.modules.ace.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio Usuario (ET_ACE seção 2.2.1).
 *
 * Design: 1 perfil por conta. Pessoa com 2 funções possui 2 contas
 * com e-mails distintos (GAP-ACE-01 RESOLVIDO).
 *
 * Máquina de estados: PENDENTE_ATIVACAO → ATIVA ↔ BLOQUEADA / DESATIVADA.
 * Transição para EXCLUÍDA não existe (RN-ACE-09).
 */
public class Usuario {

    private UUID id;
    private String nomeCompleto;
    private String email;
    private String cpf;
    private Perfil perfil;
    private StatusConta statusConta;
    private ProviderType providerType;
    private String providerUserId;
    private boolean mfaConfigurado;
    private int tentativasLoginFalhas;
    private Instant bloqueadoAte;
    private Instant ultimoAcesso;
    private Instant ultimaTrocaSenha;
    private String tokenAtivacao;
    private Instant tokenAtivacaoExpiraEm;
    private UUID criadoPor;
    private Instant criadoEm;
    private Instant atualizadoEm;
    private String motivoDesativacao;

    private Usuario() {
    }

    /**
     * Cria novo usuário no status PENDENTE_ATIVACAO (UC-ACE-01).
     * O provider_type é derivado automaticamente do perfil (RN-ACE-10).
     */
    public static Usuario criar(String nomeCompleto, String email, String cpf,
                                 Perfil perfil, String tokenAtivacao,
                                 Instant tokenAtivacaoExpiraEm, UUID criadoPor) {
        Objects.requireNonNull(nomeCompleto, "nomeCompleto é obrigatório");
        Objects.requireNonNull(email, "email é obrigatório");
        Objects.requireNonNull(cpf, "cpf é obrigatório");
        Objects.requireNonNull(perfil, "perfil é obrigatório");
        Objects.requireNonNull(tokenAtivacao, "tokenAtivacao é obrigatório");
        Objects.requireNonNull(criadoPor, "criadoPor é obrigatório");

        var usuario = new Usuario();
        usuario.id = UUID.randomUUID();
        usuario.nomeCompleto = nomeCompleto;
        usuario.email = email;
        usuario.cpf = cpf;
        usuario.perfil = perfil;
        usuario.statusConta = StatusConta.PENDENTE_ATIVACAO;
        usuario.providerType = perfil.providerTypePadrao();
        usuario.mfaConfigurado = false;
        usuario.tentativasLoginFalhas = 0;
        usuario.tokenAtivacao = tokenAtivacao;
        usuario.tokenAtivacaoExpiraEm = tokenAtivacaoExpiraEm;
        usuario.criadoPor = criadoPor;
        usuario.criadoEm = Instant.now();
        usuario.atualizadoEm = Instant.now();
        return usuario;
    }

    /** Reconstitui um usuário a partir do banco. */
    public static Usuario reconstituir(UUID id, String nomeCompleto, String email, String cpf,
                                        Perfil perfil, StatusConta statusConta,
                                        ProviderType providerType, String providerUserId,
                                        boolean mfaConfigurado, int tentativasLoginFalhas,
                                        Instant bloqueadoAte, Instant ultimoAcesso,
                                        Instant ultimaTrocaSenha, String tokenAtivacao,
                                        Instant tokenAtivacaoExpiraEm, UUID criadoPor,
                                        Instant criadoEm, Instant atualizadoEm,
                                        String motivoDesativacao) {
        var u = new Usuario();
        u.id = id;
        u.nomeCompleto = nomeCompleto;
        u.email = email;
        u.cpf = cpf;
        u.perfil = perfil;
        u.statusConta = statusConta;
        u.providerType = providerType;
        u.providerUserId = providerUserId;
        u.mfaConfigurado = mfaConfigurado;
        u.tentativasLoginFalhas = tentativasLoginFalhas;
        u.bloqueadoAte = bloqueadoAte;
        u.ultimoAcesso = ultimoAcesso;
        u.ultimaTrocaSenha = ultimaTrocaSenha;
        u.tokenAtivacao = tokenAtivacao;
        u.tokenAtivacaoExpiraEm = tokenAtivacaoExpiraEm;
        u.criadoPor = criadoPor;
        u.criadoEm = criadoEm;
        u.atualizadoEm = atualizadoEm;
        u.motivoDesativacao = motivoDesativacao;
        return u;
    }

    // ===== Transições de estado (ET_ACE seção 5.1) =====

    /** PENDENTE_ATIVACAO → ATIVA: senha definida + MFA configurado (UC-ACE-02). */
    public void ativar() {
        validarTransicao(StatusConta.PENDENTE_ATIVACAO, StatusConta.ATIVA);
        this.statusConta = StatusConta.ATIVA;
        this.mfaConfigurado = true;
        this.tokenAtivacao = null;
        this.tokenAtivacaoExpiraEm = null;
        this.atualizadoEm = Instant.now();
    }

    /** ATIVA → BLOQUEADA: 5 falhas consecutivas (RN-ACE-04). */
    public void bloquear(int minutosLockout) {
        validarTransicao(StatusConta.ATIVA, StatusConta.BLOQUEADA);
        this.statusConta = StatusConta.BLOQUEADA;
        this.bloqueadoAte = Instant.now().plus(minutosLockout, ChronoUnit.MINUTES);
        this.atualizadoEm = Instant.now();
    }

    /** BLOQUEADA → ATIVA: timeout expirado ou admin desbloqueia. */
    public void desbloquear() {
        validarTransicao(StatusConta.BLOQUEADA, StatusConta.ATIVA);
        this.statusConta = StatusConta.ATIVA;
        this.tentativasLoginFalhas = 0;
        this.bloqueadoAte = null;
        this.atualizadoEm = Instant.now();
    }

    /** ATIVA ou BLOQUEADA → DESATIVADA (UC-ACE-05). */
    public void desativar(String motivo) {
        if (statusConta != StatusConta.ATIVA && statusConta != StatusConta.BLOQUEADA) {
            throw new TransicaoInvalidaException(statusConta, StatusConta.DESATIVADA);
        }
        this.statusConta = StatusConta.DESATIVADA;
        this.motivoDesativacao = motivo;
        this.atualizadoEm = Instant.now();
    }

    /** DESATIVADA → ATIVA: Admin reativa. */
    public void reativar() {
        validarTransicao(StatusConta.DESATIVADA, StatusConta.ATIVA);
        this.statusConta = StatusConta.ATIVA;
        this.motivoDesativacao = null;
        this.atualizadoEm = Instant.now();
    }

    // ===== Comportamentos =====

    /** Registra falha de login. Retorna true se atingiu o limite (RN-ACE-04). */
    public boolean registrarFalhaLogin(int maxTentativas) {
        this.tentativasLoginFalhas++;
        this.atualizadoEm = Instant.now();
        return this.tentativasLoginFalhas >= maxTentativas;
    }

    /** Reseta contador de falhas após login bem-sucedido. */
    public void registrarLoginSucesso() {
        this.tentativasLoginFalhas = 0;
        this.ultimoAcesso = Instant.now();
        this.atualizadoEm = Instant.now();
    }

    /** Verifica se o bloqueio expirou (auto-desbloqueio). */
    public boolean isBloqueioExpirado() {
        return statusConta == StatusConta.BLOQUEADA
                && bloqueadoAte != null
                && Instant.now().isAfter(bloqueadoAte);
    }

    /** Altera o perfil (UC-ACE-04). CPF não pode ser alterado. */
    public void alterarPerfil(Perfil novoPerfil) {
        Objects.requireNonNull(novoPerfil, "perfil é obrigatório");
        this.perfil = novoPerfil;
        this.providerType = novoPerfil.providerTypePadrao();
        this.atualizadoEm = Instant.now();
    }

    /** Gera novo token de ativação para reenvio (UC-ACE-01 7a). */
    public void reenviarAtivacao(String novoToken, Instant novaExpiracao) {
        if (statusConta != StatusConta.PENDENTE_ATIVACAO) {
            throw new TransicaoInvalidaException("Reenvio só é possível para contas PENDENTE_ATIVACAO.");
        }
        this.tokenAtivacao = novoToken;
        this.tokenAtivacaoExpiraEm = novaExpiracao;
        this.atualizadoEm = Instant.now();
    }

    // ===== Validação =====

    private void validarTransicao(StatusConta esperado, StatusConta destino) {
        if (this.statusConta != esperado) {
            throw new TransicaoInvalidaException(this.statusConta, destino);
        }
    }

    // ===== Getters =====

    public UUID getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getEmail() { return email; }
    public String getCpf() { return cpf; }
    public Perfil getPerfil() { return perfil; }
    public StatusConta getStatusConta() { return statusConta; }
    public ProviderType getProviderType() { return providerType; }
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
    public boolean isMfaConfigurado() { return mfaConfigurado; }
    public int getTentativasLoginFalhas() { return tentativasLoginFalhas; }
    public Instant getBloqueadoAte() { return bloqueadoAte; }
    public Instant getUltimoAcesso() { return ultimoAcesso; }
    public Instant getUltimaTrocaSenha() { return ultimaTrocaSenha; }
    public void setUltimaTrocaSenha(Instant ultimaTrocaSenha) { this.ultimaTrocaSenha = ultimaTrocaSenha; }
    public String getTokenAtivacao() { return tokenAtivacao; }
    public Instant getTokenAtivacaoExpiraEm() { return tokenAtivacaoExpiraEm; }
    public UUID getCriadoPor() { return criadoPor; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public String getMotivoDesativacao() { return motivoDesativacao; }

    // ===== Exceção de transição =====

    public static class TransicaoInvalidaException extends RuntimeException {
        public TransicaoInvalidaException(StatusConta atual, StatusConta destino) {
            super("Transição inválida: " + atual + " → " + destino);
        }

        public TransicaoInvalidaException(String mensagem) {
            super(mensagem);
        }
    }
}
