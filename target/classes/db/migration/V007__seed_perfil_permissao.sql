-- V007: Seed de permissões padrão por perfil (RN-ACE-02)
-- DRAFT: matriz derivada da EF_ACE Seção 4 — validar com PO (Guilherme) e Lucimar.
-- Se necessário ajustar, criar V009 com UPDATE.
--
-- Legenda: V=VISUALIZAR, C=CRIAR, E=EDITAR, Ap=APROVAR, Ex=EXPORTAR

-- ============================
-- ADMINISTRADOR
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('ADMINISTRADOR', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('ADMINISTRADOR', 'CAD', 'CRIAR',      TRUE, NOW()),
('ADMINISTRADOR', 'CAD', 'EDITAR',     TRUE, NOW()),
('ADMINISTRADOR', 'MED', 'VISUALIZAR', TRUE, NOW()),
('ADMINISTRADOR', 'CAL', 'VISUALIZAR', TRUE, NOW()),
('ADMINISTRADOR', 'APR', 'VISUALIZAR', TRUE, NOW()),
('ADMINISTRADOR', 'EXP', 'VISUALIZAR', TRUE, NOW()),
('ADMINISTRADOR', 'EXP', 'EXPORTAR',   TRUE, NOW()),
('ADMINISTRADOR', 'ACE', 'VISUALIZAR', TRUE, NOW()),
('ADMINISTRADOR', 'ACE', 'CRIAR',      TRUE, NOW()),
('ADMINISTRADOR', 'ACE', 'EDITAR',     TRUE, NOW());

-- ============================
-- ANALISTA_FINANCEIRO
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('ANALISTA_FINANCEIRO', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'CAD', 'CRIAR',      TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'CAD', 'EDITAR',     TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'MED', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'CAL', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'CAL', 'CRIAR',      TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'APR', 'CRIAR',      TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'APR', 'EDITAR',     TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'EXP', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO', 'EXP', 'EXPORTAR',   TRUE, NOW());

-- ============================
-- ANALISTA_FINANCEIRO_SENIOR
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('ANALISTA_FINANCEIRO_SENIOR', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'CAD', 'CRIAR',      TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'CAD', 'EDITAR',     TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'MED', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'CAL', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'CAL', 'CRIAR',      TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'APR', 'CRIAR',      TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'APR', 'EDITAR',     TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'APR', 'APROVAR',    TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'EXP', 'VISUALIZAR', TRUE, NOW()),
('ANALISTA_FINANCEIRO_SENIOR', 'EXP', 'EXPORTAR',   TRUE, NOW());

-- ============================
-- ENGENHEIRO_MEDICAO
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('ENGENHEIRO_MEDICAO', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('ENGENHEIRO_MEDICAO', 'MED', 'VISUALIZAR', TRUE, NOW()),
('ENGENHEIRO_MEDICAO', 'MED', 'CRIAR',      TRUE, NOW()),
('ENGENHEIRO_MEDICAO', 'MED', 'EDITAR',     TRUE, NOW());

-- ============================
-- DIRETOR_FINANCEIRO
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('DIRETOR_FINANCEIRO', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('DIRETOR_FINANCEIRO', 'MED', 'VISUALIZAR', TRUE, NOW()),
('DIRETOR_FINANCEIRO', 'CAL', 'VISUALIZAR', TRUE, NOW()),
('DIRETOR_FINANCEIRO', 'APR', 'VISUALIZAR', TRUE, NOW()),
('DIRETOR_FINANCEIRO', 'APR', 'APROVAR',    TRUE, NOW()),
('DIRETOR_FINANCEIRO', 'EXP', 'VISUALIZAR', TRUE, NOW()),
('DIRETOR_FINANCEIRO', 'EXP', 'EXPORTAR',   TRUE, NOW());

-- ============================
-- SUPERINTENDENTE_FOMENTO
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('SUPERINTENDENTE_FOMENTO', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('SUPERINTENDENTE_FOMENTO', 'MED', 'VISUALIZAR', TRUE, NOW()),
('SUPERINTENDENTE_FOMENTO', 'CAL', 'VISUALIZAR', TRUE, NOW()),
('SUPERINTENDENTE_FOMENTO', 'APR', 'VISUALIZAR', TRUE, NOW()),
('SUPERINTENDENTE_FOMENTO', 'APR', 'APROVAR',    TRUE, NOW()),
('SUPERINTENDENTE_FOMENTO', 'EXP', 'VISUALIZAR', TRUE, NOW()),
('SUPERINTENDENTE_FOMENTO', 'EXP', 'EXPORTAR',   TRUE, NOW());

-- ============================
-- AGENTE_PROMOTOR
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('AGENTE_PROMOTOR', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('AGENTE_PROMOTOR', 'MED', 'CRIAR',      TRUE, NOW());

-- ============================
-- TESOURARIA
-- ============================
INSERT INTO ace_perfil_permissao (perfil, modulo, acao, permitido, atualizado_em) VALUES
('TESOURARIA', 'CAD', 'VISUALIZAR', TRUE, NOW()),
('TESOURARIA', 'APR', 'VISUALIZAR', TRUE, NOW()),
('TESOURARIA', 'EXP', 'VISUALIZAR', TRUE, NOW()),
('TESOURARIA', 'EXP', 'CRIAR',      TRUE, NOW()),
('TESOURARIA', 'EXP', 'EXPORTAR',   TRUE, NOW());
