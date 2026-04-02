package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapeando a tabela ace_vinculacao_empreendimento (V002).
 */
@Entity
@Table(name = "ace_vinculacao_empreendimento")
public class VinculacaoEmpreendimentoJpaEntity {

    @Id
    @Column(name = "id", updatable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "empreendimento_id", nullable = false, updatable = false)
    private UUID empreendimentoId;

    @Column(name = "vinculado_por", nullable = false, updatable = false)
    private UUID vinculadoPor;

    @Column(name = "vinculado_em", nullable = false, updatable = false)
    private Instant vinculadoEm;

    @Column(name = "desvinculado_em")
    private Instant desvinculadoEm;

    protected VinculacaoEmpreendimentoJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    public UUID getEmpreendimentoId() { return empreendimentoId; }
    public void setEmpreendimentoId(UUID empreendimentoId) { this.empreendimentoId = empreendimentoId; }

    public UUID getVinculadoPor() { return vinculadoPor; }
    public void setVinculadoPor(UUID vinculadoPor) { this.vinculadoPor = vinculadoPor; }

    public Instant getVinculadoEm() { return vinculadoEm; }
    public void setVinculadoEm(Instant vinculadoEm) { this.vinculadoEm = vinculadoEm; }

    public Instant getDesvinculadoEm() { return desvinculadoEm; }
    public void setDesvinculadoEm(Instant desvinculadoEm) { this.desvinculadoEm = desvinculadoEm; }
}
