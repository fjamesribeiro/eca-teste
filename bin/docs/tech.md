# TECH.md — Contexto Técnico
## Módulo ACE — Controle de Acesso

---

## 1. Stack

| Componente | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.x | Framework de aplicação |
| Spring Security | 6.x | Autenticação, autorização, filtros |
| AWS Cognito | SDK v2 | IdP para usuários externos (AP) |
| SCCI Prognum | ⚠️ PENDENTE: API não documentada (GAP-ACE-02) | IdP para usuários internos |
| PostgreSQL | 16 | Banco principal |
| Flyway | — | Migrations versionadas |
| JPA / Hibernate | — | ORM (camada de infraestrutura) |
| Testcontainers | — | Testes de integração com PostgreSQL real |
| WireMock | — | Mock de Cognito e SCCI em testes |

---

## 2. Arquitetura Hexagonal (ADR-005)

Estrutura de pacotes sob `modules/ace/`:

| Camada | Pacote | Responsabilidade |
|---|---|---|
| **Domain** | `domain/model`, `domain/service`, `domain/port` | Entidades, Value Objects, Domain Services e interfaces de ports (in/out). Sem dependências de framework. |
| **Application** | `application/usecase` | Implementações dos Use Cases. Orquestram domain services e ports. Contêm a lógica de negócio aplicacional. |
| **Infrastructure** | `infrastructure/adapter/in/web`, `infrastructure/adapter/out/...` | Controllers REST, JPA Repositories, adapters para Cognito, SCCI, SES e AuditTrail. Dependem do Spring. |

Regra de dependência: `infrastructure` → `application` → `domain`. O domínio não conhece Spring, JPA ou AWS.

---

## 3. Dual Identity Provider (ADR-007, RN-ACE-10)

| Tipo de Usuário | Provedor | Protocolo | `provider_type` |
|---|---|---|---|
| Externo (Agente Promotor) | AWS Cognito | OIDC nativo | `COGNITO` |
| Interno (CDHU — todos os outros perfis) | SCCI Prognum | OpenID Connect | `SCCI` |

**Routing:** A interface `IdentityProviderPort` abstrai ambos. O roteamento é feito com base em `provider_type` persistido no `ace_usuario`. Regra de atribuição no cadastro: perfil `AGENTE_PROMOTOR` → `COGNITO`; todos os demais → `SCCI`. ⚠️ PENDENTE: confirmar DIRETOR_FINANCEIRO e TESOURARIA (GAP-ACE-03).

**MFA:** TOTP obrigatório para ambos os provedores (PV-P04 RESOLVIDA). Fonte: RN-ACE-04.

---

## 4. Separação de Responsabilidades (ET_ACE seção 1.3)

| Responsabilidade | Onde vive |
|---|---|
| Autenticação (senha + MFA) | Identity Provider (Cognito/SCCI) |
| Hash de senha e política de complexidade | Identity Provider |
| Dados de perfil e vinculação | Banco local (PostgreSQL) |
| Sessões aplicativas | Banco local (`ace_sessao`) |
| Trilha de auditoria | Banco local — append-only (`ace_evento_acesso`) |
| Status da conta (Ativa/Desativada) | Ambos — sincronizado |
| Anti-reuso de senhas (últimas 5) | Banco local (`ace_historico_senha`) — validado antes de enviar ao IdP |

---

## 5. Enumerações Críticas (ET_ACE seção 2.3)

**`Perfil`** (8 valores fixos — RN-ACE-01):
`ADMINISTRADOR`, `ANALISTA_FINANCEIRO`, `ANALISTA_FINANCEIRO_SENIOR`, `ENGENHEIRO_MEDICAO`, `DIRETOR_FINANCEIRO`, `SUPERINTENDENTE_FOMENTO`, `AGENTE_PROMOTOR`, `TESOURARIA`

**`StatusConta`** (RN-ACE-03, RN-ACE-04, RN-ACE-09):
`PENDENTE_ATIVACAO`, `ATIVA`, `DESATIVADA`, `BLOQUEADA`

**`TipoEventoAcesso`** (RN-ACE-07):
`LOGIN`, `LOGOUT`, `FALHA_LOGIN`, `BLOQUEIO_CONTA`, `DESBLOQUEIO_CONTA`, `RESET_SENHA`, `TROCA_SENHA`, `ALTERACAO_PERFIL`, `ATIVACAO_CONTA`, `DESATIVACAO_CONTA`, `REATIVACAO_CONTA`, `CRIACAO_USUARIO`, `ALTERACAO_VINCULACAO`, `ENCERRAMENTO_SESSAO`, `CONFIGURACAO_MFA`

**`MotivoEncerramento`** (RN-ACE-12):
`LOGOUT`, `EXPIRADA`, `ADMIN_REMOTO`, `NOVA_SESSAO`, `DESATIVACAO`

**`ProviderType`**: `COGNITO`, `SCCI`

**`Modulo`**: `CAD`, `MED`, `CAL`, `APR`, `EXP`, `ACE`

**`Acao`**: `VISUALIZAR`, `CRIAR`, `EDITAR`, `APROVAR`, `EXPORTAR`

---

## 6. Índices e Constraints Críticos em Runtime

| Tabela | Constraint / Índice | Tipo | Impacto em Runtime |
|---|---|---|---|
| `ace_usuario` | `uq_ace_usuario_email` | UNIQUE | Login lookup e validação V-ACE-01 |
| `ace_usuario` | `uq_ace_usuario_cpf` | UNIQUE | Validação V-ACE-02 |
| `ace_usuario` | `chk_ace_usuario_perfil` | CHECK | Rejeita perfis fora do enum |
| `ace_usuario` | `chk_ace_usuario_status` | CHECK | Rejeita status inválido |
| `ace_vinculacao_empreendimento` | `uq_ace_vinc_ativo` | UNIQUE PARTIAL (`WHERE desvinculado_em IS NULL`) | Impede vinculação duplicada ativa |
| `ace_sessao` | `idx_ace_sessao_usuario_ativa` | B-TREE PARTIAL (`WHERE encerrada_em IS NULL`) | Contagem de sessões ativas para RN-ACE-12 |
| `ace_evento_acesso` | `trg_ace_evento_acesso_no_update/delete` | TRIGGER | Rejeita UPDATE/DELETE — append-only (RN-AUD-01) |
| `ace_usuario` | `trg_ace_usuario_no_delete` | TRIGGER | Rejeita DELETE em qualquer registro (RN-ACE-09) |
| `ace_usuario` | `trg_ace_usuario_criado_em` | TRIGGER | Rejeita alteração de `criado_em` (EF_ACE 5.1) |
| `ace_perfil_permissao` | `uq_ace_perfil_permissao` | UNIQUE (`perfil, modulo, acao`) | Garante 1 linha por combinação |

---

## 7. Parâmetros Configuráveis (ET_ACE seção 12)

| Chave | Default | Faixa | Regra |
|---|---|---|---|
| `ace.session.timeout-minutes` | 480 (8h) | 60–1440 | RN-ACE-04 |
| `ace.activation.link-hours` | 72 | — | RN-ACE-03 |
| `ace.login.max-attempts` | 5 | — | RN-ACE-04 |
| `ace.login.lockout-minutes` | 30 | — | RN-ACE-04 |
| `ace.password.expiration-days` | 90 | — | RN-ACE-08 |
| `ace.password.history-count` | 5 | — | RN-ACE-08 |
| `ace.session.max-simultaneous` | 2 | — | RN-ACE-12 |

Armazenados em tabela de configuração com audit trail (ADR-003). Lidos via componente de configuração dinâmica — não hardcodar valores no código.
