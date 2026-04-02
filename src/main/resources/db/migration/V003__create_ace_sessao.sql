-- V003: Create ace_sessao table
-- Source: ET_ACE seção 9.4
-- Rules: RN-ACE-12 (gestão de sessões, max 2 simultâneas)

CREATE TABLE ace_sessao (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id            UUID         NOT NULL REFERENCES ace_usuario(id),
    token_hash            VARCHAR(64)  NOT NULL,
    ip_origem             VARCHAR(45)  NOT NULL,
    dispositivo           VARCHAR(255),
    navegador             VARCHAR(255),
    criada_em             TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ultima_atividade      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    encerrada_em          TIMESTAMPTZ,
    motivo_encerramento   VARCHAR(50),

    CONSTRAINT uq_ace_sessao_token UNIQUE (token_hash),
    CONSTRAINT chk_ace_sessao_motivo CHECK (
        motivo_encerramento IS NULL OR
        motivo_encerramento IN ('LOGOUT', 'EXPIRADA', 'ADMIN_REMOTO', 'NOVA_SESSAO', 'DESATIVACAO')
    )
);

CREATE INDEX idx_ace_sessao_usuario_ativa
    ON ace_sessao (usuario_id)
    WHERE encerrada_em IS NULL;

CREATE INDEX idx_ace_sessao_atividade
    ON ace_sessao (ultima_atividade)
    WHERE encerrada_em IS NULL;
