-- V001: Create ace_usuario table
-- Source: ET_ACE seção 9.2
-- Rules: RN-ACE-01 (perfis fixos), RN-ACE-03 (status), RN-ACE-10 (provider)

CREATE TABLE ace_usuario (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_completo             VARCHAR(255)     NOT NULL,
    email                     VARCHAR(255)     NOT NULL,
    cpf                       VARCHAR(11)      NOT NULL,
    perfil                    VARCHAR(50)      NOT NULL,
    status_conta              VARCHAR(30)      NOT NULL DEFAULT 'PENDENTE_ATIVACAO',
    provider_type             VARCHAR(20)      NOT NULL,
    provider_user_id          VARCHAR(255),
    mfa_configurado           BOOLEAN          NOT NULL DEFAULT FALSE,
    tentativas_login_falhas   INTEGER          NOT NULL DEFAULT 0,
    bloqueado_ate             TIMESTAMPTZ,
    ultimo_acesso             TIMESTAMPTZ,
    ultima_troca_senha        TIMESTAMPTZ,
    token_ativacao            VARCHAR(255),
    token_ativacao_expira_em  TIMESTAMPTZ,
    criado_por                UUID             NOT NULL REFERENCES ace_usuario(id),
    criado_em                 TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    atualizado_em             TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    motivo_desativacao        VARCHAR(500),

    CONSTRAINT uq_ace_usuario_email UNIQUE (email),
    CONSTRAINT uq_ace_usuario_cpf   UNIQUE (cpf),
    CONSTRAINT chk_ace_usuario_cpf_len CHECK (length(cpf) = 11),
    CONSTRAINT chk_ace_usuario_perfil CHECK (perfil IN (
        'ADMINISTRADOR', 'ANALISTA_FINANCEIRO', 'ANALISTA_FINANCEIRO_SENIOR',
        'ENGENHEIRO_MEDICAO', 'DIRETOR_FINANCEIRO', 'SUPERINTENDENTE_FOMENTO',
        'AGENTE_PROMOTOR', 'TESOURARIA'
    )),
    CONSTRAINT chk_ace_usuario_status CHECK (status_conta IN (
        'PENDENTE_ATIVACAO', 'ATIVA', 'DESATIVADA', 'BLOQUEADA'
    )),
    CONSTRAINT chk_ace_usuario_provider CHECK (provider_type IN ('COGNITO', 'SCCI')),
    CONSTRAINT chk_ace_usuario_tentativas CHECK (tentativas_login_falhas >= 0)
);

-- Nota: criado_por tem FK self-referencing. O primeiro usuário (Admin seed)
-- deve ser inserido com criado_por = id próprio (bootstrap).

CREATE INDEX idx_ace_usuario_perfil ON ace_usuario (perfil);
CREATE INDEX idx_ace_usuario_status ON ace_usuario (status_conta);
CREATE INDEX idx_ace_usuario_provider ON ace_usuario (provider_type, provider_user_id);
