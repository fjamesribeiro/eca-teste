-- V006: Create ace_perfil_permissao table
-- Source: ET_ACE seção 9.7
-- Rules: RN-ACE-02 (matriz de permissões por perfil × módulo × ação)

CREATE TABLE ace_perfil_permissao (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    perfil          VARCHAR(50)  NOT NULL,
    modulo          VARCHAR(10)  NOT NULL,
    acao            VARCHAR(20)  NOT NULL,
    permitido       BOOLEAN      NOT NULL DEFAULT FALSE,
    atualizado_por  UUID REFERENCES ace_usuario(id),
    atualizado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_ace_perfil_permissao UNIQUE (perfil, modulo, acao),
    CONSTRAINT chk_ace_pp_perfil CHECK (perfil IN (
        'ADMINISTRADOR', 'ANALISTA_FINANCEIRO', 'ANALISTA_FINANCEIRO_SENIOR',
        'ENGENHEIRO_MEDICAO', 'DIRETOR_FINANCEIRO', 'SUPERINTENDENTE_FOMENTO',
        'AGENTE_PROMOTOR', 'TESOURARIA'
    )),
    CONSTRAINT chk_ace_pp_modulo CHECK (modulo IN ('CAD', 'MED', 'CAL', 'APR', 'EXP', 'ACE')),
    CONSTRAINT chk_ace_pp_acao CHECK (acao IN ('VISUALIZAR', 'CRIAR', 'EDITAR', 'APROVAR', 'EXPORTAR'))
);
