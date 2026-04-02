package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.StatusInvalidoException;
import br.com.prognum.gestaoobras.modules.ace.domain.exception.UsuarioNaoEncontradoException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.MotivoEncerramento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.DesativarUsuarioUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.SessaoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository.DadosUsuario;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC-ACE-05: Desativar/reativar usuário.
 *
 * Desativar: muda status → DESATIVADA, encerra sessões, desativa no IdP.
 * Reativar: muda status → ATIVA, reativa no IdP.
 *
 * Source: ET_ACE seção 3.6, RN-ACE-09, RN-ACE-12
 */
public class DesativarUsuarioUseCaseImpl implements DesativarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final IdentityProviderPort identityProvider;
    private final AuditTrailPort auditTrail;

    public DesativarUsuarioUseCaseImpl(UsuarioRepository usuarioRepository,
                                        SessaoRepository sessaoRepository,
                                        IdentityProviderPort identityProvider,
                                        AuditTrailPort auditTrail) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.identityProvider = identityProvider;
        this.auditTrail = auditTrail;
    }

    @Override
    public ResultadoDesativacao desativar(ComandoDesativar comando, UUID adminId) {
        // 1. Buscar usuário
        DadosUsuario usuario = usuarioRepository.findById(comando.usuarioId())
                .orElseThrow(() -> new UsuarioNaoEncontradoException(comando.usuarioId()));

        // 2. Verificar que está ATIVA ou BLOQUEADA (pode desativar ambas)
        if (usuario.statusConta() != StatusConta.ATIVA &&
                usuario.statusConta() != StatusConta.BLOQUEADA) {
            throw new StatusInvalidoException(
                    "Desativação só é possível para contas com status ATIVA ou BLOQUEADA.");
        }

        // 3. Encerrar todas as sessões ativas (RN-ACE-12)
        List<Sessao> sessoesAtivas = sessaoRepository.findAtivasByUsuarioId(usuario.id());
        int sessoesEncerradas = 0;
        for (Sessao sessao : sessoesAtivas) {
            if (sessao.isAtiva()) {
                sessao.encerrar(MotivoEncerramento.DESATIVACAO);
                sessaoRepository.salvar(sessao);
                sessoesEncerradas++;
            }
        }

        // 4. Desativar no IdP
        if (usuario.providerUserId() != null) {
            identityProvider.desativarUsuario(usuario.providerUserId());
        }

        // 5. Atualizar status no banco (RN-ACE-09)
        usuarioRepository.atualizarStatusConta(usuario.id(), StatusConta.DESATIVADA);

        // 6. Registrar motivo (opcional)
        if (comando.motivo() != null && !comando.motivo().isBlank()) {
            usuarioRepository.atualizarMotivoDesativacao(usuario.id(), comando.motivo());
        }

        // 7. Registrar evento na trilha (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuario.id(),
                TipoEventoAcesso.DESATIVACAO_CONTA,
                ResultadoEvento.SUCESSO,
                "sistema",
                null, null,
                Map.of(
                        "admin_id", adminId.toString(),
                        "motivo", comando.motivo() != null ? comando.motivo() : "",
                        "sessoes_encerradas", sessoesEncerradas
                ),
                null
        ));

        String mensagem = String.format("Conta desativada. %d sessão(ões) encerrada(s).", sessoesEncerradas);
        return new ResultadoDesativacao(usuario.id(), "DESATIVADA", sessoesEncerradas, mensagem);
    }

    @Override
    public ResultadoReativacao reativar(UUID usuarioId, UUID adminId) {
        // 1. Buscar usuário
        DadosUsuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        // 2. Verificar que está DESATIVADA
        if (usuario.statusConta() != StatusConta.DESATIVADA) {
            throw new StatusInvalidoException(usuario.statusConta(), StatusConta.DESATIVADA);
        }

        // 3. Reativar no IdP
        if (usuario.providerUserId() != null) {
            identityProvider.reativarUsuario(usuario.providerUserId());
        }

        // 4. Atualizar status no banco
        usuarioRepository.atualizarStatusConta(usuario.id(), StatusConta.ATIVA);

        // 5. Limpar motivo de desativação
        usuarioRepository.atualizarMotivoDesativacao(usuario.id(), null);

        // 6. Registrar evento na trilha (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuario.id(),
                TipoEventoAcesso.REATIVACAO_CONTA,
                ResultadoEvento.SUCESSO,
                "sistema",
                null, null,
                Map.of("admin_id", adminId.toString()),
                null
        ));

        return new ResultadoReativacao(usuario.id(), "ATIVA", "Conta reativada.");
    }
}
