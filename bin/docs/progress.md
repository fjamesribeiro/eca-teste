# Progress Log — Módulo ACE
## Sistema de Gestão de Obras — CDHU | Prognum Informática

---

## Instruções de uso

- Cada sessão de desenvolvimento gera uma nova entrada neste arquivo.
- O agente deve ler a **última entrada** ao iniciar cada sessão para retomar o contexto.
- Nunca remover entradas anteriores — apenas adicionar novas ao final.
- Formato obrigatório: ver estrutura abaixo.

---

<!--
## Sessão 001 | YYYY-MM-DD | Agente: Claude Code

### Iniciadas
- T-XXX: descrição breve da tarefa iniciada

### Concluídas
- T-XXX: descrição breve | artefato: `caminho/do/arquivo.ext`

### Bloqueadas
- T-XXX: motivo do bloqueio (referenciar GAP-ACE-XX)

### Pendências para próxima sessão
- Descrição livre do que precisa ser feito na próxima sessão
- Qualquer contexto importante que o agente deve saber ao retomar

---
-->

## Sessão 001 | 2026-04-02 | Agente: Claude Code

### Iniciadas
- T-001 a T-006, T-008: Todas as migrations da Fase 1 (exceto seed)

### Concluídas
- T-001: V001 `ace_usuario` | artefato: `src/main/resources/db/migration/V001__create_ace_usuario.sql`
- T-002: V002 `ace_vinculacao_empreendimento` | artefato: `src/main/resources/db/migration/V002__create_ace_vinculacao_empreendimento.sql`
- T-003: V003 `ace_sessao` | artefato: `src/main/resources/db/migration/V003__create_ace_sessao.sql`
- T-004: V004 `ace_evento_acesso` | artefato: `src/main/resources/db/migration/V004__create_ace_evento_acesso.sql`
- T-005: V005 `ace_historico_senha` | artefato: `src/main/resources/db/migration/V005__create_ace_historico_senha.sql`
- T-006: V006 `ace_perfil_permissao` | artefato: `src/main/resources/db/migration/V006__create_ace_perfil_permissao.sql`
- T-008: V008 triggers de imutabilidade | artefato: `src/main/resources/db/migration/V008__create_ace_triggers_imutabilidade.sql`

### Bloqueadas
- T-007: Seed de permissões padrão — aguardando GAP-ACE-04 (matriz não validada com PO)

### Pendências para próxima sessão
- T-007 continua bloqueada por GAP-ACE-04
- Fase 1 praticamente completa — próximo passo é Fase 2 (Domain Layer)
- Iniciar por T-009 (Enums) que não possui bloqueios

---

## Sessão 002 | 2026-04-02 | Agente: Claude Code

### Iniciadas
- T-009 a T-018: Todas as tarefas da Fase 2 (Domain Layer) exceto T-010 (bloqueada)

### Concluídas
- T-009: Enums do domínio (8 enums: Perfil, StatusConta, ProviderType, TipoEventoAcesso, MotivoEncerramento, Modulo, Acao, ResultadoEvento) | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/*.java`
- T-011: Domain model Sessao | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/Sessao.java`
- T-012: Domain model VinculacaoEmpreendimento | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/VinculacaoEmpreendimento.java`
- T-013: Domain model EventoAcesso | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/EventoAcesso.java`
- T-014: Domain model HistoricoSenha | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/HistoricoSenha.java`
- T-015: Port in — 7 interfaces de Use Cases | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/port/in/*.java`
- T-016: Port out — 8 interfaces de repositórios e serviços externos | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/port/out/*.java`
- T-017: Domain service PoliticaSenhaService (complexidade, anti-reuso, expiração) | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/service/PoliticaSenhaService.java`
- T-018: Domain service GestaoSessaoService (max sessões, expiração inatividade) | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/service/GestaoSessaoService.java`

### Bloqueadas
- T-010: Domain model Usuario — aguardando GAP-ACE-01 (multi-perfil)
- T-007: Seed de permissões — aguardando GAP-ACE-04 (matriz não validada)

### Decisões de Design
- `Perfil` enum inclui métodos de comportamento: `possuiVisaoGlobal()`, `exigeVinculacao()`, `providerTypePadrao()`
- Domain models usam padrão factory method (`criar()` + `reconstituir()`) sem construtores públicos
- `PoliticaSenhaService` recebe `BcryptMatcher` funcional para manter domínio livre de BCrypt
- `UsuarioRepository` definido com contratos primitivos pois T-010 (Usuario model) está bloqueado
- Adicionado `ResultadoEvento` enum (SUCESSO/FALHA/BLOQUEIO) para coluna `resultado` de ace_evento_acesso
- Adicionado `VinculacaoEmpreendimentoRepository` ao port out (não estava no escopo original de T-016 mas necessário)

### Pendências para próxima sessão
- T-010 continua bloqueada por GAP-ACE-01
- Fase 2 praticamente completa — próximo passo é Fase 3 (Application Layer / Use Cases)
- Tarefas da Fase 3 sem bloqueio: T-020 (AtivarConta), T-023 (DesativarUsuario), T-024 (ResetarSenha), T-025 (EncerrarSessao)
- Tarefas da Fase 3 bloqueadas: T-019 (GAP-ACE-03), T-021 (GAP-ACE-05), T-022 (GAP-ACE-01)

---

## Sessão 003 | 2026-04-02 | Agente: Claude Code

### Iniciadas
- T-020, T-023, T-024, T-025: Use Cases da Fase 3 sem bloqueio

### Concluídas
- T-020: UC-ACE-02 AtivarContaUseCaseImpl (definirSenha + confirmarMfa) | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/AtivarContaUseCaseImpl.java`
- T-023: UC-ACE-05 DesativarUsuarioUseCaseImpl (desativar + reativar) | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/DesativarUsuarioUseCaseImpl.java`
- T-024: UC-ACE-06 ResetarSenhaUseCaseImpl (self-service + admin forçado) | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/ResetarSenhaUseCaseImpl.java`
- T-025: UC-ACE-07 EncerrarSessaoUseCaseImpl (individual + todas) | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/EncerrarSessaoUseCaseImpl.java`

### Artefatos adicionais criados
- Exceções de domínio (8 classes): `AceException`, `UsuarioNaoEncontradoException`, `StatusInvalidoException`, `TokenInvalidoException`, `SenhasDiferentesException`, `CodigoTotpInvalidoException`, `SessaoNaoEncontradaException`, `SenhaAtualIncorretaException` | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/exception/*.java`
- Expandido `UsuarioRepository` com `DadosUsuario` record e métodos granulares de atualização (workaround para T-010 bloqueado)

### Bloqueadas (sem mudança)
- T-010: Domain model Usuario — aguardando GAP-ACE-01
- T-019: CadastrarUsuarioUseCaseImpl — aguardando GAP-ACE-03
- T-021: RealizarLoginUseCaseImpl — aguardando GAP-ACE-05
- T-022: GerenciarPerfisUseCaseImpl — aguardando GAP-ACE-01

### Decisões de Design
- `UsuarioRepository.DadosUsuario` record como projeção temporária enquanto T-010 está bloqueado
- Use cases recebem `HashEncoder` e `BcryptMatcher` como interfaces funcionais para manter camada de aplicação livre de Spring Security
- `ResetarSenhaUseCaseImpl` recebe `historicoQuantidade` como parâmetro configurável (padrão 5)
- Desativação aceita contas ATIVA ou BLOQUEADA (ambas podem ser desativadas)
- `mascararEmail()` implementado inline no ResetarSenhaUseCaseImpl

### Pendências para próxima sessão
- Fase 3 parcialmente completa — 4/7 use cases implementados, 3 bloqueados por GAPs
- Próximo passo possível: Fase 4 (Infrastructure Layer) com T-026 (JPA Entities), T-027 (JPA Repos), T-030 (SES Email), T-031 (AuditTrail), T-032 (Security Filter)
- Fase 5 (API Layer) também tem tarefas sem bloqueio: T-033, T-035, T-036, T-037, T-038

---

## Sessão 004 | 2026-04-02 | Agente: Claude Code

### Iniciadas
- T-026, T-027, T-030, T-031, T-032: Tarefas da Fase 4 sem bloqueio

### Concluídas
- T-026: JPA Entities (5 entidades mapeando tabelas da Fase 1) | artefato: `src/main/java/.../infrastructure/adapter/out/persistence/entity/*.java`
- T-027: JPA Repositories (5 Spring Data repos) + Port Adapters (5 adapter implementations) | artefato: `src/main/java/.../infrastructure/adapter/out/persistence/repository/*.java` e `*Adapter.java`
- T-030: SesEmailAdapter (templates HTML ativação + reset) | artefato: `src/main/java/.../infrastructure/adapter/out/email/SesEmailAdapter.java`
- T-031: AuditTrailAdapter (@Async, com log de falha) | artefato: `src/main/java/.../infrastructure/adapter/out/audit/AuditTrailAdapter.java`
- T-032: EmpreendimentoSecurityFilter + SecurityContextHelper (ThreadLocal) | artefato: `src/main/java/.../infrastructure/adapter/in/web/security/*.java`

### Bloqueadas (sem mudança)
- T-028: CognitoIdentityProviderAdapter — aguardando GAP-ACE-03
- T-029: ScciIdentityProviderAdapter — aguardando GAP-ACE-02

### Decisões de Design
- JPA entities usam String para enums (perfil, status, etc.) — conversão enum feita nos adapters
- Spring Data repos são interfaces internas; adapters implementam os ports de domínio
- EventoAcessoRepositoryAdapter serializa detalhes JSONB via Jackson ObjectMapper
- AuditTrailAdapter captura exceções para não bloquear fluxo principal (log.error)
- EmpreendimentoSecurityFilter extrai perfil e usuario_id do JWT (claims: `usuario_id`, `perfil`, `custom:perfil`)
- SecurityContextHelper usa ThreadLocal com clear() no finally do filter
- SesEmailAdapter recebe remetente e baseUrl via @Value properties

### Pendências para próxima sessão
- Fase 4 parcialmente completa — 5/7 tarefas, 2 bloqueadas (Cognito/SCCI adapters)
- Próximo passo: Fase 5 (API Layer) — T-033, T-035, T-036, T-037, T-038 sem bloqueio

---

## Sessão 005 | 2026-04-02 | Agente: Claude Code

### Iniciadas
- T-033, T-035, T-036, T-037, T-038: Tarefas da Fase 5 sem bloqueio

### Concluídas
- T-033: UsuarioController (POST, GET/{id}, PUT, desativar, reativar, reenviar-ativacao, forcar-reset-senha) | artefato: `src/main/java/.../infrastructure/adapter/in/web/UsuarioController.java`
- T-035: SessaoController (GET sessões ativas, DELETE individual, DELETE todas) | artefato: `src/main/java/.../infrastructure/adapter/in/web/SessaoController.java`
- T-036: SecurityConfig (Spring Security 6 FilterChain, OAuth2 Resource Server, JWT converter, stateless) | artefato: `src/main/java/.../infrastructure/adapter/in/web/security/SecurityConfig.java`
- T-037: AcePermissionEvaluator (lookup ace_perfil_permissao para @PreAuthorize) | artefato: `src/main/java/.../infrastructure/adapter/in/web/security/AcePermissionEvaluator.java`
- T-038: Endpoints adicionais — MeController (GET /usuarios/me), PerfilPermissaoController (GET/PUT /ace/perfis) | artefato: `src/main/java/.../infrastructure/adapter/in/web/{MeController,PerfilPermissaoController}.java`

### Artefatos adicionais criados
- AuthController (parcial — endpoints sem bloqueio: ativar-conta, confirmar-mfa, alterar-senha) | artefato: `src/main/java/.../infrastructure/adapter/in/web/AuthController.java`
- AceExceptionHandler (RFC 7807 Problem Details) | artefato: `src/main/java/.../infrastructure/adapter/in/web/AceExceptionHandler.java`
- PerfilPermissaoJpaEntity + PerfilPermissaoSpringDataRepository (necessário para T-037 e T-038)

### Bloqueadas (sem mudança)
- T-034: AuthController (callback + logout) — aguardando GAP-ACE-05

### Decisões de Design
- Controllers usam @PreAuthorize com @acePermissionEvaluator.hasPermission() para RBAC
- SecurityConfig: CSRF desabilitado (API stateless), sessão HTTP STATELESS
- JWT authorities extraídas do claim "perfil" com prefix "ROLE_"
- AuthController implementado parcialmente (3 endpoints sem bloqueio; callback/logout pendentes)
- reenviar-ativacao retorna placeholder (depende de T-019 CadastrarUsuarioUseCaseImpl)
- MeController retorna perfil, empreendimentos vinculados e permissões agrupadas por módulo
- Duração de sessão formatada em "Xh Ymin" para human-readable

### Pendências para próxima sessão
- Fase 5 praticamente completa — 5/6 tarefas, 1 bloqueada (T-034)
- Próximo passo: Fase 6 (Testes) — T-039, T-040, T-044, T-046, T-047 sem bloqueio

---

## Sessão 006 | 2026-04-02 | Agente: Claude Code

### Iniciadas
- T-039, T-040, T-044, T-046, T-047: Tarefas da Fase 6 sem bloqueio

### Concluídas
- T-039: PoliticaSenhaServiceTest (13 testes: complexidade, anti-reuso, expiração) | artefato: `src/test/java/.../domain/service/PoliticaSenhaServiceTest.java`
- T-040: GestaoSessaoServiceTest (10 testes: limite sessões, expiração, desativação) | artefato: `src/test/java/.../domain/service/GestaoSessaoServiceTest.java`
- T-044: AtivarContaUseCaseImplTest (9 testes: definirSenha + confirmarMfa com mocks) | artefato: `src/test/java/.../application/usecase/AtivarContaUseCaseImplTest.java`
- T-046: AuditTrailImutavelTest (4 testes: INSERT OK, UPDATE rejeitado, DELETE rejeitado, DELETE usuario rejeitado) | artefato: `src/test/java/.../infrastructure/adapter/out/persistence/AuditTrailImutavelTest.java`
- T-047: EmpreendimentoSecurityFilterTest (8 testes: visão global Admin/Superintendente/Diretor, filtro Engenheiro/AP/Analista, limpeza contexto, auth endpoints skip) | artefato: `src/test/java/.../infrastructure/adapter/in/web/security/EmpreendimentoSecurityFilterTest.java`

### Bloqueadas (sem mudança)
- T-041: Testes unitários CadastrarUsuarioUseCaseImpl — aguardando GAP-ACE-03
- T-042: Testes máquina de estado Usuario — aguardando GAP-ACE-01
- T-043: Testes integração cadastro e2e — aguardando GAP-ACE-03
- T-045: Testes integração login callback — aguardando GAP-ACE-05

### Cobertura de Testes
- 44 testes totais (13 + 10 + 9 + 4 + 8)
- Testes unitários puros: PoliticaSenhaService, GestaoSessaoService (sem framework)
- Testes com mocks (Mockito): AtivarContaUseCaseImpl
- Testes de integração com Testcontainers: AuditTrailImutavelTest (PostgreSQL real + triggers)
- Testes de filtro: EmpreendimentoSecurityFilterTest (mock-based)

### Pendências para próxima sessão
- TODAS as 6 fases concluídas (tarefas sem bloqueio)
- Restam 12 tarefas bloqueadas por 5 GAPs — requerem decisão humana
- Para prosseguir, necessário resolver GAPs com PO (Guilherme) e Lucimar

---
