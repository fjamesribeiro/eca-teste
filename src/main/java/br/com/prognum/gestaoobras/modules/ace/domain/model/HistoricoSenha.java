package br.com.prognum.gestaoobras.modules.ace.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Hash de senha armazenado para política de anti-reuso (RN-ACE-08).
 * Mantém as últimas N senhas (configurável, padrão 5).
 */
public class HistoricoSenha {

    private Long id;
    private UUID usuarioId;
    private String senhaHash;
    private Instant criadoEm;

    private HistoricoSenha() {
    }

    public static HistoricoSenha criar(UUID usuarioId, String senhaHash) {
        Objects.requireNonNull(usuarioId, "usuarioId é obrigatório");
        Objects.requireNonNull(senhaHash, "senhaHash é obrigatório");

        var historico = new HistoricoSenha();
        historico.usuarioId = usuarioId;
        historico.senhaHash = senhaHash;
        historico.criadoEm = Instant.now();
        return historico;
    }

    public static HistoricoSenha reconstituir(Long id, UUID usuarioId, String senhaHash, Instant criadoEm) {
        var historico = new HistoricoSenha();
        historico.id = id;
        historico.usuarioId = usuarioId;
        historico.senhaHash = senhaHash;
        historico.criadoEm = criadoEm;
        return historico;
    }

    public Long getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getSenhaHash() { return senhaHash; }
    public Instant getCriadoEm() { return criadoEm; }
}
