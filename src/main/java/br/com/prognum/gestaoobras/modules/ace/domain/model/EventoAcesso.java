package br.com.prognum.gestaoobras.modules.ace.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro imutável de evento de acesso (RN-ACE-07, RN-AUD-01).
 * Append-only — sem UPDATE ou DELETE.
 * Retenção mínima de 10 anos.
 */
public class EventoAcesso {

    private Long id;
    private UUID usuarioId;
    private TipoEventoAcesso tipoEvento;
    private ResultadoEvento resultado;
    private String ipOrigem;
    private String dispositivo;
    private String navegador;
    private Map<String, Object> detalhes;
    private Instant criadoEm;
    private String emailTentativa;

    private EventoAcesso() {
    }

    public static EventoAcesso criar(UUID usuarioId, TipoEventoAcesso tipoEvento,
                                      ResultadoEvento resultado, String ipOrigem,
                                      String dispositivo, String navegador,
                                      Map<String, Object> detalhes, String emailTentativa) {
        Objects.requireNonNull(tipoEvento, "tipoEvento é obrigatório");
        Objects.requireNonNull(resultado, "resultado é obrigatório");
        Objects.requireNonNull(ipOrigem, "ipOrigem é obrigatório");

        var evento = new EventoAcesso();
        evento.usuarioId = usuarioId;
        evento.tipoEvento = tipoEvento;
        evento.resultado = resultado;
        evento.ipOrigem = ipOrigem;
        evento.dispositivo = dispositivo;
        evento.navegador = navegador;
        evento.detalhes = detalhes;
        evento.criadoEm = Instant.now();
        evento.emailTentativa = emailTentativa;
        return evento;
    }

    public static EventoAcesso reconstituir(Long id, UUID usuarioId, TipoEventoAcesso tipoEvento,
                                             ResultadoEvento resultado, String ipOrigem,
                                             String dispositivo, String navegador,
                                             Map<String, Object> detalhes, Instant criadoEm,
                                             String emailTentativa) {
        var evento = new EventoAcesso();
        evento.id = id;
        evento.usuarioId = usuarioId;
        evento.tipoEvento = tipoEvento;
        evento.resultado = resultado;
        evento.ipOrigem = ipOrigem;
        evento.dispositivo = dispositivo;
        evento.navegador = navegador;
        evento.detalhes = detalhes;
        evento.criadoEm = criadoEm;
        evento.emailTentativa = emailTentativa;
        return evento;
    }

    public Long getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public TipoEventoAcesso getTipoEvento() { return tipoEvento; }
    public ResultadoEvento getResultado() { return resultado; }
    public String getIpOrigem() { return ipOrigem; }
    public String getDispositivo() { return dispositivo; }
    public String getNavegador() { return navegador; }
    public Map<String, Object> getDetalhes() { return detalhes; }
    public Instant getCriadoEm() { return criadoEm; }
    public String getEmailTentativa() { return emailTentativa; }
}
