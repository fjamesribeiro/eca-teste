package br.com.prognum.gestaoobras.modules.ace.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Associação entre usuário e empreendimento (RN-ACE-06).
 * Soft-delete via {@code desvinculadoEm}.
 * Constraint: não pode existir vinculação ativa duplicada (mesmo usuario + empreendimento).
 */
public class VinculacaoEmpreendimento {

    private UUID id;
    private UUID usuarioId;
    private UUID empreendimentoId;
    private UUID vinculadoPor;
    private Instant vinculadoEm;
    private Instant desvinculadoEm;

    private VinculacaoEmpreendimento() {
    }

    public static VinculacaoEmpreendimento criar(UUID usuarioId, UUID empreendimentoId, UUID vinculadoPor) {
        Objects.requireNonNull(usuarioId, "usuarioId é obrigatório");
        Objects.requireNonNull(empreendimentoId, "empreendimentoId é obrigatório");
        Objects.requireNonNull(vinculadoPor, "vinculadoPor é obrigatório");

        var vinculacao = new VinculacaoEmpreendimento();
        vinculacao.id = UUID.randomUUID();
        vinculacao.usuarioId = usuarioId;
        vinculacao.empreendimentoId = empreendimentoId;
        vinculacao.vinculadoPor = vinculadoPor;
        vinculacao.vinculadoEm = Instant.now();
        return vinculacao;
    }

    public static VinculacaoEmpreendimento reconstituir(UUID id, UUID usuarioId, UUID empreendimentoId,
                                                         UUID vinculadoPor, Instant vinculadoEm,
                                                         Instant desvinculadoEm) {
        var vinculacao = new VinculacaoEmpreendimento();
        vinculacao.id = id;
        vinculacao.usuarioId = usuarioId;
        vinculacao.empreendimentoId = empreendimentoId;
        vinculacao.vinculadoPor = vinculadoPor;
        vinculacao.vinculadoEm = vinculadoEm;
        vinculacao.desvinculadoEm = desvinculadoEm;
        return vinculacao;
    }

    public boolean isAtiva() {
        return desvinculadoEm == null;
    }

    public void desvincular() {
        if (!isAtiva()) {
            throw new IllegalStateException("Vinculação já desfeita");
        }
        this.desvinculadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getEmpreendimentoId() { return empreendimentoId; }
    public UUID getVinculadoPor() { return vinculadoPor; }
    public Instant getVinculadoEm() { return vinculadoEm; }
    public Instant getDesvinculadoEm() { return desvinculadoEm; }
}
