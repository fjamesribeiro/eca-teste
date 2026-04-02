# CLAUDE.md — Arquivo Mestre de Contexto
## Módulo ACE — Controle de Acesso | Sistema de Gestão de Obras — CDHU

---

## 1. Mapa de Arquivos do Workspace

| Arquivo | Propósito | Quando consultar |
|---|---|---|
| `CLAUDE.md` | Contexto mestre, protocolos e restrições do agente | Obrigatório ao iniciar qualquer sessão |
| `TECH.md` | Stack, arquitetura, enums, parâmetros técnicos | Antes de qualquer implementação de infraestrutura, JPA, Spring Security ou Flyway |
| `BUSINESS.md` | Regras de negócio, máquinas de estado, validações, permissões | Antes de implementar qualquer Use Case ou lógica de domínio |
| `TASKS.md` | Lista de tarefas com status, fontes e bloqueios | Ao iniciar/encerrar sessão e ao escolher próxima tarefa |
| `PROGRESS.md` | Changelog de sessões com artefatos produzidos | Ao iniciar sessão (ler última entrada) e ao encerrar sessão (registrar entrada) |

---

## 2. Skills Necessárias por Fase

**Fase 1 — Migrations**
PostgreSQL 16 (tipos, constraints, índices parciais, UNIQUE parcial), Flyway (naming convention, versionamento), PL/pgSQL (funções e triggers), design de tabelas append-only.

**Fase 2 — Domain Layer**
Java 21 (records, sealed classes, pattern matching), arquitetura hexagonal (Ports & Adapters), DDD (Entities, Value Objects, Domain Services), design de enums com comportamento.

**Fase 3 — Application Layer**
Implementação de Use Cases (Command/Result pattern), validações de negócio orquestradas, transações, tratamento de exceções de domínio, integração entre ports.

**Fase 4 — Infrastructure Layer**
Spring Security 6.x (SecurityFilterChain, OncePerRequestFilter), AWS Cognito SDK (AdminCreateUser, TOTP, OIDC), Spring Data JPA (Specifications, repositórios), BCrypt, adapters para ports de saída.

**Fase 5 — API Layer**
Spring MVC (controllers, @PreAuthorize, ResponseEntity), RFC 7807 (Problem Details para erros), OIDC/OAuth2 callback flow, paginação Spring Data, PermissionEvaluator customizado.

**Fase 6 — Testes**
JUnit 5, Testcontainers (PostgreSQL), WireMock (mock Cognito/SCCI), Spring Boot Test, convenção de nomes `test_RN_ACE_XX_descricao`.

---

## 3. Contexto Negativo — O Que NÃO Fazer

- **NUNCA** executar `DELETE` em `ace_usuario` ou `ace_evento_acesso` — usar soft-delete (status `DESATIVADA`) ou deixar o trigger rejeitar. Fonte: RN-ACE-09, RN-AUD-01.
- **NUNCA** implementar `UPDATE` ou `DELETE` em tabelas append-only (`ace_evento_acesso`). Os triggers rejeitam — não contornar. Fonte: RN-AUD-01.
- **NUNCA** armazenar senha em plaintext — apenas hash BCrypt em `ace_historico_senha`. O plaintext vai ao IdP e é descartado. Fonte: RN-ACE-08.
- **NUNCA** expor os campos `cpf` ou `senha_hash` em responses de API (exceto `GET /usuarios/{id}` que expõe CPF mascarado, a confirmar com PO).
- **NUNCA** tomar decisão sobre os GAPs listados na seção 4 — sinalizar `[BLOCKED]` e aguardar o desenvolvedor.
- **NUNCA** criar perfis além dos 8 definidos no enum `Perfil`. O CHECK constraint no banco rejeita qualquer outro valor. Fonte: RN-ACE-01.
- **NUNCA** alterar `criado_em` após INSERT em `ace_usuario` — o trigger `trg_ace_usuario_criado_em` rejeita. Fonte: EF_ACE 5.1.
- **NÃO** assumir comportamento do SCCI (endpoints, claims, formato de token) sem documentação formal da API. Fonte: GAP-ACE-02.
- **NÃO** implementar o `ScciIdentityProviderAdapter` além de um stub vazio até GAP-ACE-02 ser resolvido.
- **NÃO** tomar decisão sobre multi-perfil por usuário (GAP-ACE-01) — o design atual assume 1 perfil por conta.

---

## 4. GAPs Bloqueantes — Aguardam Decisão Humana

1. **GAP-ACE-01** — Usuário com 2 funções: 2 contas ou 1 conta com múltiplos perfis? | Bloqueia: `T-010` (domain model Usuario), `T-022` (UC-ACE-04)
2. **GAP-ACE-02** — API SCCI (endpoints, claims, formato de token) não documentada | Bloqueia: `T-029` (ScciIdentityProviderAdapter)
3. **GAP-ACE-03** — Confirmação de que DIRETOR_FINANCEIRO e TESOURARIA são internos (SCCI) | Bloqueia: `T-019` (UC-ACE-01), `T-028` (CognitoAdapter)
4. **GAP-ACE-04** — Matriz de permissões não validada com PO (Guilherme) e Lucimar | Bloqueia: `T-007` (seed permissões)
5. **GAP-ACE-05** — Fluxo de token parcial para senha expirada (scope `password_change_required`) | Bloqueia: `T-021` (UC-ACE-03), `T-034` (AuthController)

---

## 5. Protocolo de Atualização de Tarefas

### Em `TASKS.md`
- **Ao iniciar tarefa:** alterar status para `[IN_PROGRESS]` e adicionar `| iniciado: YYYY-MM-DD`
- **Ao concluir tarefa:** alterar para `[DONE]` e adicionar `| concluído: YYYY-MM-DD | artefato: <caminho/do/arquivo>`
- **Ao bloquear tarefa:** alterar para `[BLOCKED]` e adicionar `| bloqueio: GAP-ACE-XX`
- **Nunca remover linhas** — apenas atualizar o status inline.

### Em `PROGRESS.md`
- Ao encerrar cada sessão, adicionar uma nova entrada com:
  - Número da sessão (sequencial)
  - Data
  - Tarefas iniciadas, concluídas e bloqueadas nesta sessão
  - Artefatos produzidos (caminhos de arquivo)
  - Pendências para a próxima sessão

---

## 6. Regra de Início de Sessão

**Ao iniciar qualquer nova sessão, o agente DEVE executar esta sequência antes de qualquer ação:**

1. Ler `CLAUDE.md` — recarregar restrições e protocolo
2. Ler `PROGRESS.md` — última entrada: verificar o que foi feito e pendências
3. Ler `TASKS.md` — filtrar tarefas com status `[IN_PROGRESS]` e `[BLOCKED]`
4. Retomar tarefa `[IN_PROGRESS]` existente OU escolher a próxima `[TODO]` na sequência de fases
5. Nunca iniciar uma tarefa cujo bloqueio (GAP) ainda não foi resolvido
