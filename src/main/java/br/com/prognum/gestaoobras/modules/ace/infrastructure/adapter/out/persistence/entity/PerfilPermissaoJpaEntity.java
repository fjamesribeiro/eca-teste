package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapeando a tabela ace_perfil_permissao (V006).
 */
@Entity
@Table(name = "ace_perfil_permissao")
public class PerfilPermissaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "perfil", nullable = false, length = 50)
    private String perfil;

    @Column(name = "modulo", nullable = false, length = 10)
    private String modulo;

    @Column(name = "acao", nullable = false, length = 20)
    private String acao;

    @Column(name = "permitido", nullable = false)
    private boolean permitido;

    @Column(name = "atualizado_por")
    private UUID atualizadoPor;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected PerfilPermissaoJpaEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public boolean isPermitido() { return permitido; }
    public void setPermitido(boolean permitido) { this.permitido = permitido; }

    public UUID getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(UUID atualizadoPor) { this.atualizadoPor = atualizadoPor; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
