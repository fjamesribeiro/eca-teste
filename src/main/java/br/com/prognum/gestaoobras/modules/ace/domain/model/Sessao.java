package br.com.prognum.gestaoobras.modules.ace.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Sessão ativa de um usuário autenticado (RN-ACE-12).
 * Uma sessão é considerada ativa quando {@code encerradaEm == null}.
 * Máximo 2 sessões simultâneas por usuário.
 */
public class Sessao {

    private UUID id;
    private UUID usuarioId;
    private String tokenHash;
    private String ipOrigem;
    private String dispositivo;
    private String navegador;
    private Instant criadaEm;
    private Instant ultimaAtividade;
    private Instant encerradaEm;
    private MotivoEncerramento motivoEncerramento;

    private Sessao() {
    }

    public static Sessao criar(UUID usuarioId, String tokenHash, String ipOrigem,
                                String dispositivo, String navegador) {
        Objects.requireNonNull(usuarioId, "usuarioId é obrigatório");
        Objects.requireNonNull(tokenHash, "tokenHash é obrigatório");
        Objects.requireNonNull(ipOrigem, "ipOrigem é obrigatório");

        var sessao = new Sessao();
        sessao.id = UUID.randomUUID();
        sessao.usuarioId = usuarioId;
        sessao.tokenHash = tokenHash;
        sessao.ipOrigem = ipOrigem;
        sessao.dispositivo = dispositivo;
        sessao.navegador = navegador;
        sessao.criadaEm = Instant.now();
        sessao.ultimaAtividade = Instant.now();
        return sessao;
    }

    /** Reconstitui uma sessão a partir do banco (sem validações de criação). */
    public static Sessao reconstituir(UUID id, UUID usuarioId, String tokenHash,
                                       String ipOrigem, String dispositivo, String navegador,
                                       Instant criadaEm, Instant ultimaAtividade,
                                       Instant encerradaEm, MotivoEncerramento motivoEncerramento) {
        var sessao = new Sessao();
        sessao.id = id;
        sessao.usuarioId = usuarioId;
        sessao.tokenHash = tokenHash;
        sessao.ipOrigem = ipOrigem;
        sessao.dispositivo = dispositivo;
        sessao.navegador = navegador;
        sessao.criadaEm = criadaEm;
        sessao.ultimaAtividade = ultimaAtividade;
        sessao.encerradaEm = encerradaEm;
        sessao.motivoEncerramento = motivoEncerramento;
        return sessao;
    }

    public boolean isAtiva() {
        return encerradaEm == null;
    }

    public void encerrar(MotivoEncerramento motivo) {
        if (!isAtiva()) {
            throw new IllegalStateException("Sessão já encerrada");
        }
        Objects.requireNonNull(motivo, "motivo é obrigatório");
        this.encerradaEm = Instant.now();
        this.motivoEncerramento = motivo;
    }

    public void registrarAtividade() {
        if (!isAtiva()) {
            throw new IllegalStateException("Sessão encerrada não pode registrar atividade");
        }
        this.ultimaAtividade = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getTokenHash() { return tokenHash; }
    public String getIpOrigem() { return ipOrigem; }
    public String getDispositivo() { return dispositivo; }
    public String getNavegador() { return navegador; }
    public Instant getCriadaEm() { return criadaEm; }
    public Instant getUltimaAtividade() { return ultimaAtividade; }
    public Instant getEncerradaEm() { return encerradaEm; }
    public MotivoEncerramento getMotivoEncerramento() { return motivoEncerramento; }
}
