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
 * JPA entity mapeando a tabela ace_evento_acesso (V004).
 * Append-only — sem UPDATE ou DELETE (RN-AUD-01).
 */
@Entity
@Table(name = "ace_evento_acesso")
public class EventoAcessoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento;

    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado;

    @Column(name = "ip_origem", nullable = false, length = 45)
    private String ipOrigem;

    @Column(name = "dispositivo")
    private String dispositivo;

    @Column(name = "navegador")
    private String navegador;

    @Column(name = "detalhes", columnDefinition = "jsonb")
    private String detalhes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "email_tentativa")
    private String emailTentativa;

    protected EventoAcessoJpaEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getUsuarioId() { return usuarioId; }
    public void setUsuarioId(UUID usuarioId) { this.usuarioId = usuarioId; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public String getIpOrigem() { return ipOrigem; }
    public void setIpOrigem(String ipOrigem) { this.ipOrigem = ipOrigem; }

    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String dispositivo) { this.dispositivo = dispositivo; }

    public String getNavegador() { return navegador; }
    public void setNavegador(String navegador) { this.navegador = navegador; }

    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public String getEmailTentativa() { return emailTentativa; }
    public void setEmailTentativa(String emailTentativa) { this.emailTentativa = emailTentativa; }
}
