package br.com.prognum.gestaoobras.modules.ace.domain.service;

import br.com.prognum.gestaoobras.modules.ace.domain.model.MotivoEncerramento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Sessao;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * Domain service para gestão de sessões (RN-ACE-12, RN-ACE-04).
 * - Max 2 sessões simultâneas; 3ª login encerra a mais antiga.
 * - Expiração por inatividade (timeout configurável).
 *
 * Sem dependências de framework — puro domínio.
 */
public class GestaoSessaoService {

    private static final int MAX_SESSOES_SIMULTANEAS = 2;

    /**
     * Aplica a regra de max sessões (RN-ACE-12).
     * Se o usuário já possui {@code MAX_SESSOES_SIMULTANEAS} sessões ativas,
     * encerra a mais antiga com motivo {@code NOVA_SESSAO}.
     *
     * @param sessoesAtivas sessões ativas do usuário (encerrada_em IS NULL)
     * @return lista de sessões que foram encerradas (pode ser vazia)
     */
    public List<Sessao> aplicarLimiteSessoes(List<Sessao> sessoesAtivas) {
        if (sessoesAtivas.size() < MAX_SESSOES_SIMULTANEAS) {
            return List.of();
        }

        // Ordena por criada_em ASC — encerra as mais antigas até sobrar (MAX - 1)
        List<Sessao> ordenadas = sessoesAtivas.stream()
                .filter(Sessao::isAtiva)
                .sorted(Comparator.comparing(Sessao::getCriadaEm))
                .toList();

        int quantidadeEncerrar = ordenadas.size() - MAX_SESSOES_SIMULTANEAS + 1;
        List<Sessao> encerradas = ordenadas.subList(0, quantidadeEncerrar);
        encerradas.forEach(s -> s.encerrar(MotivoEncerramento.NOVA_SESSAO));
        return encerradas;
    }

    /**
     * Verifica se a sessão expirou por inatividade (RN-ACE-04).
     *
     * @param sessao sessão a verificar
     * @param timeoutMinutos timeout de inatividade em minutos (configurável, padrão 480 = 8h)
     * @return true se a sessão expirou
     */
    public boolean isSessaoExpirada(Sessao sessao, int timeoutMinutos) {
        if (!sessao.isAtiva()) {
            return false;
        }
        Instant limiteInatividade = sessao.getUltimaAtividade()
                .plus(timeoutMinutos, ChronoUnit.MINUTES);
        return Instant.now().isAfter(limiteInatividade);
    }

    /**
     * Encerra sessão por expiração de inatividade.
     */
    public void expirarSessao(Sessao sessao) {
        sessao.encerrar(MotivoEncerramento.EXPIRADA);
    }

    /**
     * Encerra todas as sessões ativas do usuário por desativação de conta (UC-ACE-05).
     *
     * @param sessoesAtivas sessões ativas do usuário
     * @return quantidade de sessões encerradas
     */
    public int encerrarTodasPorDesativacao(List<Sessao> sessoesAtivas) {
        int count = 0;
        for (Sessao sessao : sessoesAtivas) {
            if (sessao.isAtiva()) {
                sessao.encerrar(MotivoEncerramento.DESATIVACAO);
                count++;
            }
        }
        return count;
    }
}
