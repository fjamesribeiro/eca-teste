package br.com.prognum.gestaoobras.modules.ace.application.usecase;

import br.com.prognum.gestaoobras.modules.ace.domain.exception.AceException;
import br.com.prognum.gestaoobras.modules.ace.domain.model.EventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.Perfil;
import br.com.prognum.gestaoobras.modules.ace.domain.model.ResultadoEvento;
import br.com.prognum.gestaoobras.modules.ace.domain.model.TipoEventoAcesso;
import br.com.prognum.gestaoobras.modules.ace.domain.model.VinculacaoEmpreendimento;
import br.com.prognum.gestaoobras.modules.ace.domain.port.in.CadastrarUsuarioUseCase;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.AuditTrailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.EmailPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.IdentityProviderPort;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.UsuarioRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.port.out.VinculacaoEmpreendimentoRepository;
import br.com.prognum.gestaoobras.modules.ace.domain.service.CpfValidator;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.entity.UsuarioJpaEntity;
import br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.out.persistence.repository.UsuarioSpringDataRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC-ACE-01: Cadastrar novo usuário.
 *
 * Regra de provider: AGENTE_PROMOTOR → COGNITO, todos os demais → SCCI
 * (GAP-ACE-03 RESOLVIDO: DIRETOR_FINANCEIRO e TESOURARIA = SCCI).
 *
 * Source: ET_ACE seções 10.3 e 3.2
 */
public class CadastrarUsuarioUseCaseImpl implements CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioSpringDataRepository usuarioJpaRepo;
    private final VinculacaoEmpreendimentoRepository vinculacaoRepository;
    private final IdentityProviderPort identityProvider;
    private final EmailPort emailPort;
    private final AuditTrailPort auditTrail;
    private final int horasAtivacao;

    public CadastrarUsuarioUseCaseImpl(UsuarioRepository usuarioRepository,
                                        UsuarioSpringDataRepository usuarioJpaRepo,
                                        VinculacaoEmpreendimentoRepository vinculacaoRepository,
                                        IdentityProviderPort identityProvider,
                                        EmailPort emailPort,
                                        AuditTrailPort auditTrail,
                                        int horasAtivacao) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioJpaRepo = usuarioJpaRepo;
        this.vinculacaoRepository = vinculacaoRepository;
        this.identityProvider = identityProvider;
        this.emailPort = emailPort;
        this.auditTrail = auditTrail;
        this.horasAtivacao = horasAtivacao;
    }

    @Override
    public Resultado executar(Comando comando, UUID adminId) {
        // 1. Validar perfil (RN-ACE-01)
        Perfil perfil;
        try {
            perfil = Perfil.valueOf(comando.perfil());
        } catch (IllegalArgumentException e) {
            throw new PerfilInvalidoException(comando.perfil());
        }

        // 2. Validar CPF (V-ACE-02)
        if (!CpfValidator.isValid(comando.cpf())) {
            throw new CpfInvalidoException();
        }

        // 3. Verificar e-mail duplicado (V-ACE-01)
        if (usuarioRepository.existsByEmail(comando.email())) {
            throw new EmailDuplicadoException(comando.email());
        }

        // 4. Verificar CPF duplicado
        if (usuarioRepository.existsByCpf(comando.cpf())) {
            throw new CpfDuplicadoException();
        }

        // 5. Validar vinculação (RN-ACE-06)
        if (perfil.exigeVinculacao() &&
                (comando.empreendimentoIds() == null || comando.empreendimentoIds().isEmpty())) {
            throw new VinculacaoObrigatoriaException(perfil);
        }

        // 6. Gerar token de ativação
        String tokenAtivacao = UUID.randomUUID().toString();
        Instant tokenExpiraEm = Instant.now().plus(horasAtivacao, ChronoUnit.HOURS);

        // 7. Persistir usuário
        var entity = new UsuarioJpaEntity();
        UUID usuarioId = UUID.randomUUID();
        entity.setId(usuarioId);
        entity.setNomeCompleto(comando.nomeCompleto());
        entity.setEmail(comando.email());
        entity.setCpf(comando.cpf());
        entity.setPerfil(perfil.name());
        entity.setStatusConta("PENDENTE_ATIVACAO");
        entity.setProviderType(perfil.providerTypePadrao().name());
        entity.setMfaConfigurado(false);
        entity.setTentativasLoginFalhas(0);
        entity.setTokenAtivacao(tokenAtivacao);
        entity.setTokenAtivacaoExpiraEm(tokenExpiraEm);
        entity.setCriadoPor(adminId);
        entity.setCriadoEm(Instant.now());
        entity.setAtualizadoEm(Instant.now());
        usuarioJpaRepo.save(entity);

        // 8. Criar no IdP
        var idpResult = identityProvider.criarUsuario(comando.email(), comando.nomeCompleto());
        usuarioRepository.atualizarProviderUserId(usuarioId, idpResult.providerUserId());

        // 9. Criar vinculações
        List<UUID> empreendimentoIds = comando.empreendimentoIds() != null
                ? comando.empreendimentoIds() : List.of();
        for (UUID empId : empreendimentoIds) {
            vinculacaoRepository.salvar(
                    VinculacaoEmpreendimento.criar(usuarioId, empId, adminId));
        }

        // 10. Enviar e-mail de ativação (RN-ACE-03)
        String linkAtivacao = "/auth/ativar-conta?token=" + tokenAtivacao;
        emailPort.enviarEmailAtivacao(comando.email(), comando.nomeCompleto(),
                tokenAtivacao, linkAtivacao);

        // 11. Registrar evento (RN-ACE-07)
        auditTrail.registrar(EventoAcesso.criar(
                usuarioId,
                TipoEventoAcesso.CRIACAO_USUARIO,
                ResultadoEvento.SUCESSO,
                "sistema", null, null,
                Map.of("perfil", perfil.name(),
                        "admin_id", adminId.toString(),
                        "empreendimentos", empreendimentoIds.size()),
                null
        ));

        return new Resultado(
                usuarioId,
                comando.nomeCompleto(),
                comando.email(),
                comando.cpf(),
                perfil.name(),
                "PENDENTE_ATIVACAO",
                empreendimentoIds,
                entity.getCriadoEm().toString(),
                adminId
        );
    }

    // Exceções específicas do cadastro

    public static class EmailDuplicadoException extends AceException {
        public EmailDuplicadoException(String email) {
            super("V-ACE-01", "E-mail " + email + " já cadastrado.");
        }
    }

    public static class CpfInvalidoException extends AceException {
        public CpfInvalidoException() {
            super("V-ACE-02", "CPF inválido. Verifique os dígitos.");
        }
    }

    public static class CpfDuplicadoException extends AceException {
        public CpfDuplicadoException() {
            super("V-ACE-02", "CPF já cadastrado no sistema.");
        }
    }

    public static class PerfilInvalidoException extends AceException {
        public PerfilInvalidoException(String perfil) {
            super("PERFIL_INVALIDO", "Perfil inválido: " + perfil + ". Valores aceitos: " +
                    java.util.Arrays.toString(Perfil.values()));
        }
    }

    public static class VinculacaoObrigatoriaException extends AceException {
        public VinculacaoObrigatoriaException(Perfil perfil) {
            super("VINCULACAO_OBRIGATORIA",
                    "Perfil " + perfil + " exige vinculação a pelo menos 1 empreendimento.");
        }
    }
}
