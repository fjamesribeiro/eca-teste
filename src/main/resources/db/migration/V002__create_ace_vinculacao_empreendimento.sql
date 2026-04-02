-- V002: Create ace_vinculacao_empreendimento table
-- Source: ET_ACE seção 9.3
-- Rules: RN-ACE-06 (vinculação usuário-empreendimento)

CREATE TABLE ace_vinculacao_empreendimento (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID        NOT NULL REFERENCES ace_usuario(id),
    empreendimento_id   UUID        NOT NULL, -- FK para cad_empreendimento (criada quando CAD existir)
    vinculado_por       UUID        NOT NULL REFERENCES ace_usuario(id),
    vinculado_em        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    desvinculado_em     TIMESTAMPTZ
);

CREATE INDEX idx_ace_vinc_usuario ON ace_vinculacao_empreendimento (usuario_id);
CREATE INDEX idx_ace_vinc_empreendimento ON ace_vinculacao_empreendimento (empreendimento_id);
CREATE UNIQUE INDEX uq_ace_vinc_ativo
    ON ace_vinculacao_empreendimento (usuario_id, empreendimento_id)
    WHERE desvinculado_em IS NULL;
