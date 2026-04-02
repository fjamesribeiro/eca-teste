package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.SessaoNaoEncontradaException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.StatusInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.MotivoEncerramento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.EncerrarSessaoUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.SessaoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC-ACE-07: Encerrar sessão remotamente.
 *
 * Administrador pode encerrar sessão individual ou todas as sessões de um usuário.
 *
 * Source: ET_ACE seção 3.8, RN-ACE-12
 */
public class EncerrarSessaoUseCaseImpl implements EncerrarSessaoUseCase {

    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final AuditTrailPort auditTrail;

    public EncerrarSessaoUseCaseImpl(UsuarioRepository usuarioRepository,
                                      SessaoRepository sessaoRepository,
                                      AuditTrailPort auditTrail) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.auditTrail = auditTrail;
    }

    @Override
    public ResultadoEncerramento encerrar(UUID usuarioId, UUID sessaoId, UUID adminId) {
        // 1. Verificar que o usuário existe
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        // 2. Buscar sessão
        Sessao sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new SessaoNaoEncontradaException(sessaoId));

        // 3. Verificar que a sessão pertence ao usuário
        if (!sessao.getUsuarioId().equals(usuarioId)) {
            throw new SessaoNaoEncontradaException(sessaoId);
        }

        // 4. Verificar que está ativa
        if (!sessao.isAtiva()) {
            throw new StatusInvalidoException("Sessão já encerrada.");
        }

        // 5. Encerrar sessão
        sessao.encerrar(MotivoEncerramento.ADMIN_REMOTO);
        sessaoRepository.salvar(sessao);

        // 6. Registrar evento (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuarioId,
                TipoEventoAcesso.ENCERRAMENTO_SESSAO,
                ResultadoEvento.SUCESSO,
                "sistema",
                null, null,
                Map.of(
                        "sessao_id", sessaoId.toString(),
                        "motivo", MotivoEncerramento.ADMIN_REMOTO.name(),
                        "admin_id", adminId.toString()
                ),
                null
        ));

        return new ResultadoEncerramento("Sessão encerrada.", sessaoId);
    }

    @Override
    public ResultadoEncerramentoTodas encerrarTodas(UUID usuarioId, UUID adminId) {
        // 1. Verificar que o usuário existe
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        // 2. Buscar sessões ativas
        List<Sessao> sessoesAtivas = sessaoRepository.findAtivasByUsuarioId(usuarioId);

        // 3. Encerrar todas
        int count = 0;
        for (Sessao sessao : sessoesAtivas) {
            if (sessao.isAtiva()) {
                sessao.encerrar(MotivoEncerramento.ADMIN_REMOTO);
                sessaoRepository.salvar(sessao);
                count++;
            }
        }

        // 4. Registrar evento (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuarioId,
                TipoEventoAcesso.ENCERRAMENTO_SESSAO,
                ResultadoEvento.SUCESSO,
                "sistema",
                null, null,
                Map.of(
                        "motivo", MotivoEncerramento.ADMIN_REMOTO.name(),
                        "admin_id", adminId.toString(),
                        "sessoes_encerradas", count
                ),
                null
        ));

        String mensagem = String.format("%d sessão(ões) encerrada(s).", count);
        return new ResultadoEncerramentoTodas(mensagem, count);
    }
}
