-- V008: Triggers de imutabilidade
-- Source: ET_ACE seção 9.8
-- Rules: RN-AUD-01 (append-only), RN-ACE-09 (sem DELETE), EF_ACE 5.1 (criado_em imutável)

-- =============================================================================
-- Trigger: ace_evento_acesso é append-only (RN-AUD-01)
-- =============================================================================
CREATE OR REPLACE FUNCTION fn_ace_evento_acesso_immutable()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'ace_evento_acesso is append-only. UPDATE and DELETE are prohibited. Source: RN-AUD-01';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ace_evento_acesso_no_update
    BEFORE UPDATE ON ace_evento_acesso
    FOR EACH ROW
    EXECUTE FUNCTION fn_ace_evento_acesso_immutable();

CREATE TRIGGER trg_ace_evento_acesso_no_delete
    BEFORE DELETE ON ace_evento_acesso
    FOR EACH ROW
    EXECUTE FUNCTION fn_ace_evento_acesso_immutable();

-- =============================================================================
-- Trigger: ace_usuario.criado_em é imutável após INSERT (EF_ACE 5.1)
-- =============================================================================
CREATE OR REPLACE FUNCTION fn_ace_usuario_criado_em_immutable()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.criado_em <> NEW.criado_em THEN
        RAISE EXCEPTION 'ace_usuario.criado_em is immutable after INSERT. Source: EF_ACE 5.1';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ace_usuario_criado_em
    BEFORE UPDATE ON ace_usuario
    FOR EACH ROW
    EXECUTE FUNCTION fn_ace_usuario_criado_em_immutable();

-- =============================================================================
-- Trigger: nenhum usuário pode ser deletado (RN-ACE-09)
-- =============================================================================
CREATE OR REPLACE FUNCTION fn_ace_usuario_no_delete()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'ace_usuario does not allow DELETE. Use desativacao (status_conta = DESATIVADA). Source: RN-ACE-09';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_ace_usuario_no_delete
    BEFORE DELETE ON ace_usuario
    FOR EACH ROW
    EXECUTE FUNCTION fn_ace_usuario_no_delete();
