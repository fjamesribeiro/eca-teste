-- V004: Create ace_evento_acesso table (append-only)
-- Source: ET_ACE seção 9.5
-- Rules: RN-ACE-07 (log de acesso), RN-AUD-01 (trilha imutável)

CREATE TABLE ace_evento_acesso (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id       UUID REFERENCES ace_usuario(id),
    tipo_evento      VARCHAR(50)  NOT NULL,
    resultado        VARCHAR(20)  NOT NULL,
    ip_origem        VARCHAR(45)  NOT NULL,
    dispositivo      VARCHAR(255),
    navegador        VARCHAR(255),
    detalhes         JSONB,
    criado_em        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    email_tentativa  VARCHAR(255),

    CONSTRAINT chk_ace_evento_tipo CHECK (tipo_evento IN (
        'LOGIN', 'LOGOUT', 'FALHA_LOGIN', 'BLOQUEIO_CONTA', 'DESBLOQUEIO_CONTA',
        'RESET_SENHA', 'TROCA_SENHA', 'ALTERACAO_PERFIL', 'ATIVACAO_CONTA',
        'DESATIVACAO_CONTA', 'REATIVACAO_CONTA', 'CRIACAO_USUARIO',
        'ALTERACAO_VINCULACAO', 'ENCERRAMENTO_SESSAO', 'CONFIGURACAO_MFA'
    )),
    CONSTRAINT chk_ace_evento_resultado CHECK (resultado IN ('SUCESSO', 'FALHA', 'BLOQUEIO'))
);

CREATE INDEX idx_ace_evento_usuario ON ace_evento_acesso (usuario_id, criado_em);
CREATE INDEX idx_ace_evento_tipo ON ace_evento_acesso (tipo_evento, criado_em);
CREATE INDEX idx_ace_evento_data ON ace_evento_acesso (criado_em);
