# BUSINESS.md — Contexto de Negócio
## Módulo ACE — Controle de Acesso

> **Documento fonte:** `ET_ACE_Controle_Acesso_v1_0.md` (versão 1.0, Abril/2026)
> Toda referência a seções, regras e validações neste arquivo aponta para esse documento.
> Em caso de dúvida ou conflito, o documento fonte é a fonte de verdade.

---

## 1. Atores do Sistema

| Ator | Tipo | Provedor de Identidade | Escopo de Visibilidade |
|---|---|---|---|
| Administrador | Interno CDHU | SCCI | Global — todos os empreendimentos |
| Analista Financeiro | Interno CDHU | SCCI | Empreendimentos vinculados |
| Analista Financeiro Sênior | Interno CDHU | SCCI | Empreendimentos vinculados |
| Engenheiro de Medição | Interno CDHU | SCCI | Empreendimentos vinculados |
| Diretor Financeiro | Interno CDHU | SCCI ⚠️ GAP-ACE-03 | Global — todos os empreendimentos |
| Superintendente de Fomento | Interno CDHU | SCCI | Global — todos os empreendimentos |
| Tesouraria | Interno CDHU | SCCI ⚠️ GAP-ACE-03 | Empreendimentos vinculados |
| Agente Promotor (AP) | Externo | AWS Cognito | Apenas próprios empreendimentos vinculados |

---

## 2. Regras de Negócio Críticas

| RN-ID | Nome Curto | Resumo | Camada de Enforcement |
|---|---|---|---|
| RN-ACE-01 | Perfis de Acesso | 8 perfis fixos, não criáveis dinamicamente | Database (CHECK) + Application (enum) |
| RN-ACE-02 | Matriz de Permissões | Permissões por perfil × módulo × ação, configuráveis pelo Admin | Database (tabela) + Security Filter (@PreAuthorize) |
| RN-ACE-03 | Provisioning pelo Admin | Somente Administrador cria usuários; e-mail de ativação com token válido 72h | Application (@PreAuthorize) + Database |
| RN-ACE-04 | MFA Obrigatório + Bloqueio | TOTP obrigatório; 5 falhas → bloqueio 30 min; timeout de sessão configurável (padrão 8h) | IdP + Application |
| RN-ACE-05 | Segregação Interno × Externo | AP só visualiza/opera nos próprios empreendimentos vinculados | Application (EmpreendimentoSecurityFilter) + Query (Specification JPA) |
| RN-ACE-06 | Vinculação Usuário-Empreendimento | Perfis operacionais exigem ≥1 empreendimento; Admin e Superintendente têm visão global | Database + Application (Use Case) |
| RN-ACE-07 | Log de Acesso na Trilha | Todo evento de autenticação registrado em `ace_evento_acesso` com IP, dispositivo, resultado | Database (append-only) + Application (@Async) |
| RN-ACE-08 | Política de Senhas | 12+ chars, maiúscula+minúscula+número+especial; sem reusar 5 últimas; expira em 90 dias | Application (PoliticaSenhaService) + IdP |
| RN-ACE-09 | Desativação sem Exclusão | Contas nunca deletadas — apenas status `DESATIVADA`; registros históricos preservados | Database (trigger anti-DELETE) + Application |
| RN-ACE-10 | Dual Identity Provider | AP → Cognito; Internos → SCCI; roteamento por `provider_type` | Application (IdentityProviderPort routing) |
| RN-ACE-11 | Menor Privilégio | Cada usuário tem 1 perfil; pessoa com 2 funções tem 2 contas ⚠️ GAP-ACE-01 | Application (validação no Use Case) |
| RN-ACE-12 | Gestão de Sessões | Máximo 2 sessões simultâneas; 3ª login encerra a sessão mais antiga | Database + Application (GestaoSessaoService) |
| RN-AUD-01 | Trilha Imutável | `ace_evento_acesso` é append-only; retenção 10 anos; sem purge | Database (triggers + GRANT restrito) |

---

## 3. Máquinas de Estado

### 3.1 StatusConta — Transições (ET_ACE seção 5.1)

| De | Para | Trigger | Guard |
|---|---|---|---|
| `PENDENTE_ATIVACAO` | `ATIVA` | UC-ACE-02: senha definida + MFA configurado | Token válido (< 72h), complexidade OK, TOTP validado |
| `ATIVA` | `BLOQUEADA` | 5 falhas consecutivas de login | `tentativas_login_falhas >= 5` |
| `BLOQUEADA` | `ATIVA` | Timeout 30 min expirado | `bloqueado_ate < NOW()` |
| `BLOQUEADA` | `ATIVA` | Admin desbloqueia manualmente | Admin autenticado |
| `ATIVA` | `DESATIVADA` | UC-ACE-05: Admin desativa | Admin autenticado |
| `DESATIVADA` | `ATIVA` | Admin reativa | Admin autenticado |
| `PENDENTE_ATIVACAO` | `PENDENTE_ATIVACAO` | Admin reenvia e-mail | Token anterior expirado |

**Guards absolutos:** PENDENTE_ATIVACAO → ATIVA impossível sem MFA configurado. DESATIVADA → ATIVA é exclusivamente ação do Admin. Transição para EXCLUÍDA não existe.

### 3.2 Sessão — Motivos de Encerramento (ET_ACE seção 5.2)

| Motivo (`motivo_encerramento`) | Trigger |
|---|---|
| `LOGOUT` | Usuário encerra voluntariamente |
| `EXPIRADA` | Timeout de inatividade atingido |
| `ADMIN_REMOTO` | Administrador encerra remotamente (UC-ACE-07) |
| `NOVA_SESSAO` | 3ª sessão criada — a mais antiga é encerrada automaticamente |
| `DESATIVACAO` | Conta do usuário desativada (UC-ACE-05) |

Sessão é considerada ATIVA quando `encerrada_em IS NULL`.

---

## 4. Validações de Entrada

| V-ID | Campo | Regra | Erro HTTP / Código |
|---|---|---|---|
| V-ACE-01 | `email` | Formato e-mail válido + único no sistema | 400 / `V-ACE-01` |
| V-ACE-02 | `cpf` | 11 dígitos numéricos + dígito verificador mod 11 | 400 / `V-ACE-02` |
| V-ACE-03 | `senha` | Mínimo 12 chars, 1 maiúscula, 1 minúscula, 1 número, 1 especial | 400 / `V-ACE-03` |
| V-ACE-04 | `token` (ativação) | Token de ativação válido e não expirado (< 72h) | 400 / `V-ACE-04` |
| V-ACE-05 | `codigo_totp` | Código TOTP de 6 dígitos válido no app autenticador | 400 / `V-ACE-05` |
| V-ACE-06 | — | Conta não pode estar no status `BLOQUEADA` ao tentar login | 403 / `V-ACE-06` |
| V-ACE-07 | — | Sessão não expirada por inatividade | 401 (sessão encerrada automaticamente) |
| V-ACE-08 | — | Usuário possui permissão para o módulo/ação solicitado | 403 / `ACESSO_NEGADO` |
| V-ACE-09 | — | ⚠️ PENDENTE: não identificada explicitamente no ET_ACE v1.0 | — |
| V-ACE-10 | — | Senha não expirada (< 90 dias desde `ultima_troca_senha`) | 403 / `SENHA_EXPIRADA` → redirect ⚠️ GAP-ACE-05 |

---

## 5. Matriz de Permissões Padrão (ET_ACE seção 6.3)

⚠️ PENDENTE GAP-ACE-04: matriz derivada da EF_ACE Seção 4 — validar com Guilherme (PO) e Lucimar antes de usar como seed.

| Perfil | CAD | MED | CAL | APR | EXP | ACE |
|---|---|---|---|---|---|---|
| Administrador | V,C,E | V | V | V | V,Ex | V,C,E |
| Analista Financeiro | V,C,E | V | V,C | C,E | V,Ex | — |
| Analista Financeiro Sênior | V,C,E | V | V,C | C,E,Ap | V,Ex | — |
| Engenheiro de Medição | V | V,C,E | — | — | — | — |
| Diretor Financeiro | V | V | V | V,Ap | V,Ex | — |
| Superintendente de Fomento | V | V | V | V,Ap | V,Ex | — |
| Agente Promotor | V* | C** | — | — | — | — |
| Tesouraria | V | — | — | V | V,C,Ex | — |

**Legenda:** V=Visualizar · C=Criar · E=Editar · Ap=Aprovar · Ex=Exportar
`*` AP: apenas empreendimentos próprios (RN-ACE-05)
`**` AP: apenas PMS dos próprios empreendimentos (RN-ACE-05)

---

## 6. Vinculação a Empreendimentos (RN-ACE-06)

**Perfis que EXIGEM `empreendimento_ids` (≥1 obrigatório) no cadastro e na edição:**
`ENGENHEIRO_MEDICAO`, `ANALISTA_FINANCEIRO`, `ANALISTA_FINANCEIRO_SENIOR`, `AGENTE_PROMOTOR`, `TESOURARIA`

**Perfis com VISÃO GLOBAL (não possuem registros em `ace_vinculacao_empreendimento`):**
`ADMINISTRADOR`, `SUPERINTENDENTE_FOMENTO`, `DIRETOR_FINANCEIRO`

O `EmpreendimentoSecurityFilter` trata os perfis globais como exceção — `visaoGlobal = true` → sem filtro de `WHERE empreendimento_id IN (...)`. Ver TECH.md seção 2 para detalhes de implementação do filtro.
