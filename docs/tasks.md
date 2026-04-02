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
- `[BLOCKED]` T-007 | V007 seed permissões padrão | fonte: ET_ACE seção 6.3 | bloqueio: GAP-ACE-04
- `[DONE]` T-008 | V008 triggers de imutabilidade | fonte: ET_ACE seção 9.8 | bloqueio: nenhum | concluído: 2026-04-02 | artefato: `src/main/resources/db/migration/V008__create_ace_triggers_imutabilidade.sql`

---

## Fase 2 — Domain Layer

- `[TODO]` T-009 | Enums: `Perfil`, `StatusConta`, `ProviderType`, `TipoEventoAcesso`, `MotivoEncerramento`, `Modulo`, `Acao` | fonte: ET_ACE seção 2.3 | bloqueio: nenhum
- `[TODO]` T-010 | Domain model: `Usuario` (com transições de estado) | fonte: ET_ACE seção 2.2.1 | bloqueio: GAP-ACE-01
- `[TODO]` T-011 | Domain model: `Sessao` | fonte: ET_ACE seção 2.2.3 | bloqueio: nenhum
- `[TODO]` T-012 | Domain model: `VinculacaoEmpreendimento` | fonte: ET_ACE seção 2.2.2 | bloqueio: nenhum
- `[TODO]` T-013 | Domain model: `EventoAcesso` | fonte: ET_ACE seção 2.2.4 | bloqueio: nenhum
- `[TODO]` T-014 | Domain model: `HistoricoSenha` | fonte: ET_ACE seção 2.2.5 | bloqueio: nenhum
- `[TODO]` T-015 | Port in: interfaces dos 7 Use Cases (UC-ACE-01 a UC-ACE-07) | fonte: ET_ACE seção 1.1 | bloqueio: nenhum
- `[TODO]` T-016 | Port out: `UsuarioRepository`, `SessaoRepository`, `EventoAcessoRepository`, `HistoricoSenhaRepository`, `IdentityProviderPort`, `EmailPort`, `AuditTrailPort` | fonte: ET_ACE seção 1.1 | bloqueio: nenhum
- `[TODO]` T-017 | Domain service: `PoliticaSenhaService` (complexidade, anti-reuso, expiração) | fonte: ET_ACE seção 10.1 | bloqueio: nenhum
- `[TODO]` T-018 | Domain service: `GestaoSessaoService` (max sessões, expiração por inatividade) | fonte: ET_ACE seção 10.2 | bloqueio: nenhum

---

## Fase 3 — Application Layer (Use Cases)

- `[TODO]` T-019 | UC-ACE-01: `CadastrarUsuarioUseCaseImpl` | fonte: ET_ACE seções 10.3 e 3.2 | bloqueio: GAP-ACE-03
- `[TODO]` T-020 | UC-ACE-02: `AtivarContaUseCaseImpl` | fonte: ET_ACE seção 3.3 | bloqueio: nenhum
- `[TODO]` T-021 | UC-ACE-03: `RealizarLoginUseCaseImpl` | fonte: ET_ACE seção 3.4 | bloqueio: GAP-ACE-05
- `[TODO]` T-022 | UC-ACE-04: `GerenciarPerfisUseCaseImpl` | fonte: ET_ACE seção 3.5 | bloqueio: GAP-ACE-01
- `[TODO]` T-023 | UC-ACE-05: `DesativarUsuarioUseCaseImpl` | fonte: ET_ACE seção 3.6 | bloqueio: nenhum
- `[TODO]` T-024 | UC-ACE-06: `ResetarSenhaUseCaseImpl` | fonte: ET_ACE seção 3.7 | bloqueio: nenhum
- `[TODO]` T-025 | UC-ACE-07: `EncerrarSessaoUseCaseImpl` | fonte: ET_ACE seção 3.8 | bloqueio: nenhum

---

## Fase 4 — Infrastructure Layer

- `[TODO]` T-026 | JPA Entities: `UsuarioJpaEntity`, `SessaoJpaEntity`, `VinculacaoEmpreendimentoJpaEntity`, `EventoAcessoJpaEntity`, `HistoricoSenhaJpaEntity` | fonte: ET_ACE seção 1.1 | bloqueio: nenhum
- `[TODO]` T-027 | JPA Repositories: `UsuarioJpaRepository`, `SessaoJpaRepository`, `EventoAcessoJpaRepository`, `HistoricoSenhaJpaRepository` | fonte: ET_ACE seção 1.1 | bloqueio: nenhum
- `[TODO]` T-028 | `CognitoIdentityProviderAdapter` (AdminCreateUser, TOTP, OIDC callback, disable/enable) | fonte: ET_ACE seção 7.1 | bloqueio: GAP-ACE-03
- `[TODO]` T-029 | `ScciIdentityProviderAdapter` (stub vazio até documentação disponível) | fonte: ET_ACE seção 7.2 | bloqueio: GAP-ACE-02
- `[TODO]` T-030 | `SesEmailAdapter` (templates: ativacao-conta, reset-senha) | fonte: ET_ACE seção 7.3 | bloqueio: nenhum
- `[TODO]` T-031 | `AuditTrailAdapter` (implementação @Async de `AuditTrailPort`) | fonte: ET_ACE seção 4 (RN-ACE-07) | bloqueio: nenhum
- `[TODO]` T-032 | `EmpreendimentoSecurityFilter` + `SecurityContextHelper` | fonte: ET_ACE seção 6.4 | bloqueio: nenhum

---

## Fase 5 — API Layer

- `[TODO]` T-033 | `UsuarioController`: POST /usuarios, GET /usuarios, GET /usuarios/{id}, PUT /usuarios/{id}, POST /desativar, POST /reativar, POST /reenviar-ativacao, POST /forcar-reset-senha | fonte: ET_ACE seções 3.2, 3.5, 3.6 | bloqueio: nenhum
- `[TODO]` T-034 | `AuthController`: POST /auth/callback, POST /auth/logout, POST /auth/ativar-conta, POST /auth/confirmar-mfa, POST /auth/alterar-senha | fonte: ET_ACE seções 3.3, 3.4, 3.7 | bloqueio: GAP-ACE-05
- `[TODO]` T-035 | `SessaoController`: GET /usuarios/{id}/sessoes, DELETE /sessoes/{sessao_id}, DELETE /sessoes (todas) | fonte: ET_ACE seção 3.8 | bloqueio: nenhum
- `[TODO]` T-036 | `SecurityConfig` (Spring Security 6 — FilterChain, OAuth2 Resource Server, JWT converter) | fonte: ET_ACE seção 6.2 | bloqueio: nenhum
- `[TODO]` T-037 | `AcePermissionEvaluator` (lookup em `ace_perfil_permissao` para @PreAuthorize) | fonte: ET_ACE seção 6.2 | bloqueio: nenhum
- `[TODO]` T-038 | Endpoints adicionais: GET /usuarios/me, GET /ace/perfis, PUT /ace/perfis/{perfil}/permissoes | fonte: ET_ACE seção 3.9 | bloqueio: nenhum

---

## Fase 6 — Testes

- `[TODO]` T-039 | Testes unitários: `PoliticaSenhaService` (complexidade, anti-reuso, expiração) | fonte: ET_ACE seção 8.1 | bloqueio: nenhum
- `[TODO]` T-040 | Testes unitários: `GestaoSessaoService` (max 2 sessões, expiração inatividade) | fonte: ET_ACE seção 8.1 | bloqueio: nenhum
- `[TODO]` T-041 | Testes unitários: `CadastrarUsuarioUseCaseImpl` (vinculação, CPF, e-mail duplicado) | fonte: ET_ACE seção 8.1 | bloqueio: GAP-ACE-03
- `[TODO]` T-042 | Testes unitários: máquina de estado `Usuario` (transições válidas e inválidas) | fonte: ET_ACE seção 8.1 | bloqueio: GAP-ACE-01
- `[TODO]` T-043 | Testes de integração: cadastro e2e (POST /usuarios → banco + IdP mock + e-mail + trilha) | fonte: ET_ACE seção 8.2 | bloqueio: GAP-ACE-03
- `[TODO]` T-044 | Testes de integração: ativação e2e (POST /auth/ativar-conta → senha no IdP mock + MFA + status ATIVA) | fonte: ET_ACE seção 8.2 | bloqueio: nenhum
- `[TODO]` T-045 | Testes de integração: login callback (POST /auth/callback → tokens + sessão + trilha) | fonte: ET_ACE seção 8.2 | bloqueio: GAP-ACE-05
- `[TODO]` T-046 | Testes de integração: trilha imutável (trigger rejeita UPDATE/DELETE em `ace_evento_acesso`) | fonte: ET_ACE seção 8.2 | bloqueio: nenhum
- `[TODO]` T-047 | Testes de integração: filtro de empreendimento (Engenheiro só vê dados vinculados) | fonte: ET_ACE seção 8.2 | bloqueio: nenhum

---

## GAPs — Aguardando Decisão Humana

- `[BLOCKED]` GAP-ACE-01 | Usuário com 2 funções: 2 contas separadas ou 1 conta com múltiplos perfis? | bloqueia: T-010, T-022, T-042
- `[BLOCKED]` GAP-ACE-02 | API SCCI não documentada (endpoints, claims, formato de token) | bloqueia: T-029
- `[BLOCKED]` GAP-ACE-03 | Confirmar que DIRETOR_FINANCEIRO e TESOURARIA usam SCCI (internos CDHU) | bloqueia: T-019, T-028, T-041, T-043
- `[BLOCKED]` GAP-ACE-04 | Matriz de permissões não validada com PO (Guilherme) e Lucimar | bloqueia: T-007
- `[BLOCKED]` GAP-ACE-05 | Fluxo de token parcial (scope limitado) para senha expirada não especificado | bloqueia: T-021, T-034, T-045
