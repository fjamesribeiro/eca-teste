-- V005: Create ace_historico_senha table
-- Source: ET_ACE seção 9.6
-- Rules: RN-ACE-08 (anti-reuso das últimas 5 senhas)

CREATE TABLE ace_historico_senha (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id  UUID         NOT NULL REFERENCES ace_usuario(id),
    senha_hash  VARCHAR(255) NOT NULL,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ace_hist_senha_usuario ON ace_historico_senha (usuario_id, criado_em DESC);
