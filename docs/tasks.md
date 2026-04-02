# TASKS.md — Controle de Tarefas
## Módulo ACE — Controle de Acesso

---

## Status — Legenda

| Status | Significado |
|---|---|
| `[TODO]` | Aguardando execução |
| `[IN_PROGRESS]` | Em andamento na sessão atual |
| `[DONE]` | Concluída — artefato produzido |
| `[BLOCKED]` | Bloqueada por GAP pendente de decisão humana |

**Regra:** Nunca remover linhas. Apenas atualizar o status inline. Ao mudar status, adicionar `| iniciado: YYYY-MM-DD` ou `| concluído: YYYY-MM-DD | artefato: <caminho>`.

---

## Fase 1 — Migrations (Flyway)

- `[DONE]` T-001 | V001 `ace_usuario` | fonte: ET_ACE seção 9.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V001__create_ace_usuario.sql`
- `[DONE]` T-002 | V002 `ace_vinculacao_empreendimento` | fonte: ET_ACE seção 9.3 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V002__create_ace_vinculacao_empreendimento.sql`
- `[DONE]` T-003 | V003 `ace_sessao` | fonte: ET_ACE seção 9.4 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V003__create_ace_sessao.sql`
- `[DONE]` T-004 | V004 `ace_evento_acesso` | fonte: ET_ACE seção 9.5 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V004__create_ace_evento_acesso.sql`
- `[DONE]` T-005 | V005 `ace_historico_senha` | fonte: ET_ACE seção 9.6 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V005__create_ace_historico_senha.sql`
- `[DONE]` T-006 | V006 `ace_perfil_permissao` | fonte: ET_ACE seção 9.7 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V006__create_ace_perfil_permissao.sql`
- `[DONE]` T-007 | V007 seed permissões padrão | fonte: ET_ACE seção 6.3 | bloqueio: GAP-ACE-04 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V007__seed_perfil_permissao.sql`
- `[DONE]` T-008 | V008 triggers de imutabilidade | fonte: ET_ACE seção 9.8 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V008__create_ace_triggers_imutabilidade.sql`

---

## Fase 2 — Domain Layer

- `[DONE]` T-009 | Enums: `Perfil`, `StatusConta`, `ProviderType`, `TipoEventoAcesso`, `MotivoEncerramento`, `Modulo`, `Acao`, `ResultadoEvento` | fonte: ET_ACE seção 2.3 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/{Perfil,StatusConta,ProviderType,TipoEventoAcesso,MotivoEncerramento,Modulo,Acao,ResultadoEvento}.java`
- `[DONE]` T-010 | Domain model: `Usuario` (com transições de estado) | fonte: ET_ACE seção 2.2.1 | bloqueio: GAP-ACE-01 RESOLVIDO (1 perfil por conta) | concluído: 2026-04-02 | artefato: `src/main/java/.../domain/model/Usuario.java`
- `[DONE]` T-011 | Domain model: `Sessao` | fonte: ET_ACE seção 2.2.3 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/Sessao.java`
- `[DONE]` T-012 | Domain model: `VinculacaoEmpreendimento` | fonte: ET_ACE seção 2.2.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/VinculacaoEmpreendimento.java`
- `[DONE]` T-013 | Domain model: `EventoAcesso` | fonte: ET_ACE seção 2.2.4 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/EventoAcesso.java`
- `[DONE]` T-014 | Domain model: `HistoricoSenha` | fonte: ET_ACE seção 2.2.5 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/model/HistoricoSenha.java`
- `[DONE]` T-015 | Port in: interfaces dos 7 Use Cases (UC-ACE-01 a UC-ACE-07) | fonte: ET_ACE seção 1.1 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/port/in/{CadastrarUsuarioUseCase,AtivarContaUseCase,RealizarLoginUseCase,GerenciarPerfisUseCase,DesativarUsuarioUseCase,ResetarSenhaUseCase,EncerrarSessaoUseCase}.java`
- `[DONE]` T-016 | Port out: `UsuarioRepository`, `SessaoRepository`, `EventoAcessoRepository`, `HistoricoSenhaRepository`, `VinculacaoEmpreendimentoRepository`, `IdentityProviderPort`, `EmailPort`, `AuditTrailPort` | fonte: ET_ACE seção 1.1 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/port/out/{UsuarioRepository,SessaoRepository,EventoAcessoRepository,HistoricoSenhaRepository,VinculacaoEmpreendimentoRepository,IdentityProviderPort,EmailPort,AuditTrailPort}.java`
- `[DONE]` T-017 | Domain service: `PoliticaSenhaService` (complexidade, anti-reuso, expiração) | fonte: ET_ACE seção 10.1 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/service/PoliticaSenhaService.java`
- `[DONE]` T-018 | Domain service: `GestaoSessaoService` (max sessões, expiração por inatividade) | fonte: ET_ACE seção 10.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/domain/service/GestaoSessaoService.java`

---

## Fase 3 — Application Layer (Use Cases)

- `[DONE]` T-019 | UC-ACE-01: `CadastrarUsuarioUseCaseImpl` | fonte: ET_ACE seções 10.3 e 3.2 | bloqueio: GAP-ACE-03 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/main/java/.../application/usecase/CadastrarUsuarioUseCaseImpl.java`
- `[DONE]` T-020 | UC-ACE-02: `AtivarContaUseCaseImpl` | fonte: ET_ACE seção 3.3 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/AtivarContaUseCaseImpl.java`
- `[DONE]` T-021 | UC-ACE-03: `RealizarLoginUseCaseImpl` | fonte: ET_ACE seção 3.4 | bloqueio: GAP-ACE-05 RESOLVIDO (JWT scope restrito) | concluído: 2026-04-02 | artefato: `src/main/java/.../application/usecase/RealizarLoginUseCaseImpl.java`
- `[DONE]` T-022 | UC-ACE-04: `GerenciarPerfisUseCaseImpl` | fonte: ET_ACE seção 3.5 | bloqueio: GAP-ACE-01 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/main/java/.../application/usecase/GerenciarPerfisUseCaseImpl.java`
- `[DONE]` T-023 | UC-ACE-05: `DesativarUsuarioUseCaseImpl` | fonte: ET_ACE seção 3.6 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/DesativarUsuarioUseCaseImpl.java`
- `[DONE]` T-024 | UC-ACE-06: `ResetarSenhaUseCaseImpl` | fonte: ET_ACE seção 3.7 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/ResetarSenhaUseCaseImpl.java`
- `[DONE]` T-025 | UC-ACE-07: `EncerrarSessaoUseCaseImpl` | fonte: ET_ACE seção 3.8 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/br/com/prognum/gestaoobras/modules/ace/application/usecase/EncerrarSessaoUseCaseImpl.java`

---

## Fase 4 — Infrastructure Layer

- `[DONE]` T-026 | JPA Entities: `UsuarioJpaEntity`, `SessaoJpaEntity`, `VinculacaoEmpreendimentoJpaEntity`, `EventoAcessoJpaEntity`, `HistoricoSenhaJpaEntity` | fonte: ET_ACE seção 1.1 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/out/persistence/entity/*.java`
- `[DONE]` T-027 | JPA Repositories + Port Adapters: Spring Data repos + domain port adapter implementations | fonte: ET_ACE seção 1.1 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/out/persistence/repository/*.java` e `src/main/java/.../infrastructure/adapter/out/persistence/*Adapter.java`
- `[DONE]` T-028 | `CognitoIdentityProviderAdapter` (AdminCreateUser, TOTP, OIDC callback, disable/enable) | fonte: ET_ACE seção 7.1 | bloqueio: GAP-ACE-03 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/out/identity/CognitoIdentityProviderAdapter.java`
- `[DONE]` T-029 | `ScciIdentityProviderAdapter` (stub UnsupportedOperationException) | fonte: ET_ACE seção 7.2 | bloqueio: GAP-ACE-02 (stub implementado) | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/out/identity/ScciIdentityProviderAdapter.java`
- `[DONE]` T-030 | `SesEmailAdapter` (templates: ativacao-conta, reset-senha) | fonte: ET_ACE seção 7.3 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/out/email/SesEmailAdapter.java`
- `[DONE]` T-031 | `AuditTrailAdapter` (implementação @Async de `AuditTrailPort`) | fonte: ET_ACE seção 4 (RN-ACE-07) | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/out/audit/AuditTrailAdapter.java`
- `[DONE]` T-032 | `EmpreendimentoSecurityFilter` + `SecurityContextHelper` | fonte: ET_ACE seção 6.4 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/in/web/security/{EmpreendimentoSecurityFilter,SecurityContextHelper}.java`

---

## Fase 5 — API Layer

- `[DONE]` T-033 | `UsuarioController`: POST /usuarios, GET /usuarios/{id}, PUT /usuarios/{id}, POST /desativar, POST /reativar, POST /reenviar-ativacao, POST /forcar-reset-senha | fonte: ET_ACE seções 3.2, 3.5, 3.6 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/in/web/UsuarioController.java`
- `[DONE]` T-034 | `AuthController`: POST /auth/callback, POST /auth/logout, POST /auth/ativar-conta, POST /auth/confirmar-mfa, POST /auth/alterar-senha | fonte: ET_ACE seções 3.3, 3.4, 3.7 | bloqueio: GAP-ACE-05 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/in/web/AuthController.java`
- `[DONE]` T-035 | `SessaoController`: GET /usuarios/{id}/sessoes, DELETE /sessoes/{sessao_id}, DELETE /sessoes (todas) | fonte: ET_ACE seção 3.8 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/in/web/SessaoController.java`
- `[DONE]` T-036 | `SecurityConfig` (Spring Security 6 — FilterChain, OAuth2 Resource Server, JWT converter) | fonte: ET_ACE seção 6.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/in/web/security/SecurityConfig.java`
- `[DONE]` T-037 | `AcePermissionEvaluator` (lookup em `ace_perfil_permissao` para @PreAuthorize) | fonte: ET_ACE seção 6.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/in/web/security/AcePermissionEvaluator.java`
- `[DONE]` T-038 | Endpoints adicionais: GET /usuarios/me, GET /ace/perfis, PUT /ace/perfis/{perfil}/permissoes | fonte: ET_ACE seção 3.9 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/java/.../infrastructure/adapter/in/web/{MeController,PerfilPermissaoController}.java`

---

## Fase 6 — Testes

- `[DONE]` T-039 | Testes unitários: `PoliticaSenhaService` (complexidade, anti-reuso, expiração) | fonte: ET_ACE seção 8.1 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/test/java/.../domain/service/PoliticaSenhaServiceTest.java`
- `[DONE]` T-040 | Testes unitários: `GestaoSessaoService` (max 2 sessões, expiração inatividade) | fonte: ET_ACE seção 8.1 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/test/java/.../domain/service/GestaoSessaoServiceTest.java`
- `[DONE]` T-041 | Testes unitários: `CadastrarUsuarioUseCaseImpl` (vinculação, CPF, e-mail duplicado) | fonte: ET_ACE seção 8.1 | bloqueio: GAP-ACE-03 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/test/java/.../application/usecase/CadastrarUsuarioUseCaseImplTest.java`
- `[DONE]` T-042 | Testes unitários: máquina de estado `Usuario` (transições válidas e inválidas) | fonte: ET_ACE seção 8.1 | bloqueio: GAP-ACE-01 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/test/java/.../domain/model/UsuarioTest.java`
- `[DONE]` T-043 | Testes de integração: cadastro e2e (POST /usuarios → banco + IdP mock + e-mail + trilha) | fonte: ET_ACE seção 8.2 | bloqueio: GAP-ACE-03 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/test/java/.../application/usecase/CadastroIntegracaoTest.java`
- `[DONE]` T-044 | Testes de integração: ativação e2e (POST /auth/ativar-conta → senha no IdP mock + MFA + status ATIVA) | fonte: ET_ACE seção 8.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/test/java/.../application/usecase/AtivarContaUseCaseImplTest.java`
- `[DONE]` T-045 | Testes de integração: login callback (POST /auth/callback → tokens + sessão + trilha) | fonte: ET_ACE seção 8.2 | bloqueio: GAP-ACE-05 RESOLVIDO | concluído: 2026-04-02 | artefato: `src/test/java/.../application/usecase/RealizarLoginUseCaseImplTest.java`
- `[DONE]` T-046 | Testes de integração: trilha imutável (trigger rejeita UPDATE/DELETE em `ace_evento_acesso`) | fonte: ET_ACE seção 8.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/test/java/.../infrastructure/adapter/out/persistence/AuditTrailImutavelTest.java`
- `[DONE]` T-047 | Testes de integração: filtro de empreendimento (Engenheiro só vê dados vinculados) | fonte: ET_ACE seção 8.2 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/test/java/.../infrastructure/adapter/in/web/security/EmpreendimentoSecurityFilterTest.java`

---

## GAPs — Aguardando Decisão Humana

- `[RESOLVED]` GAP-ACE-01 | Decisão: 1 perfil por conta, 2 contas se necessário | desbloqueou: T-010, T-022, T-042
- `[RESOLVED]` GAP-ACE-02 | Decisão: stub com UnsupportedOperationException até documentação SCCI | desbloqueou: T-029
- `[RESOLVED]` GAP-ACE-03 | Decisão: DIRETOR_FINANCEIRO e TESOURARIA = SCCI (internos CDHU) | desbloqueou: T-019, T-028, T-041, T-043
- `[RESOLVED]` GAP-ACE-04 | Decisão: seed com matriz draft da EF_ACE, ajustável via V009 | desbloqueou: T-007
- `[RESOLVED]` GAP-ACE-05 | Decisão: JWT com scope restrito "password_change_required" | desbloqueou: T-021, T-034, T-045
