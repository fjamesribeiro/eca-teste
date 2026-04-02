package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapeando a tabela ace_sessao (V003).
 */
@Entity
@Table(name = "ace_sessao")
public class SessaoJpaEntity {

    @Id
    @Column(name = "id", updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "ip_origem", nullable = false, length = 45)
    private String ipOrigem;

    @Column(name = "dispositivo")
    private String dispositivo;

    @Column(name = "navegador")
    private String navegador;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(name = "ultima_atividade", nullable = false)
    private Instant ultimaAtividade;

    @Column(name = "encerrada_em")
    private Instant encerradaEm;

    @Column(name = "motivo_encerramento", length = 50)
    private String motivoEncerramento;

    protected SessaoJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public String getIpOrigem() { return ipOrigem; }
    public void setIpOrigem(String ipOrigem) { this.ipOrigem = ipOrigem; }

    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String dispositivo) { this.dispositivo = dispositivo; }

    public String getNavegador() { return navegador; }
    public void setNavegador(String navegador) { this.navegador = navegador; }

    public Instant getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Instant criadaEm) { this.criadaEm = criadaEm; }

    public Instant getUltimaAtividade() { return ultimaAtividade; }
    public void setUltimaAtividade(Instant ultimaAtividade) { this.ultimaAtividade = ultimaAtividade; }

    public Instant getEncerradaEm() { return encerradaEm; }
    public void setEncerradaEm(Instant encerradaEm) { this.encerradaEm = encerradaEm; }

    public String getMotivoEncerramento() { return motivoEncerramento; }
    public void setMotivoEncerramento(String motivoEncerramento) { this.motivoEncerramento = motivoEncerramento; }
}
