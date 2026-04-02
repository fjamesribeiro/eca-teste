package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import br.com.prognum.gestaoobras.modules.ace.domain.model.HistoricoSenha;

import java.util.List;
import java.util.UUID;

/**
 * Port de saída para persistência de histórico de senhas (RN-ACE-08).
 */
public interface HistoricoSenhaRepository {

    HistoricoSenha salvar(HistoricoSenha historico);

    /** Retorna as últimas N senhas do usuário, ordenadas por criado_em DESC. */
    List<HistoricoSenha> findUltimasByUsuarioId(UUID usuarioId, int quantidade);
}
