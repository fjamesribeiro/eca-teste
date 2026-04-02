package br.com.prognum.gestaoobras.modules.ace.domain.port.out;

import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.model.StatusConta;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de saída para persistência de usuários.
 * Implementado na camada de infraestrutura via JPA.
 */
public interface UsuarioRepository {

    // Note: Usuario domain model (T-010) está bloqueado por GAP-ACE-01.
    // Quando T-010 for implementado, os métodos abaixo usarão o tipo Usuario.
    // Por ora, definimos os contratos com tipos primitivos para os lookups essenciais.

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<UUID> findIdByEmail(String email);

    Optional<Perfil> findPerfilById(UUID id);

    Optional<StatusConta> findStatusContaById(UUID id);
}
