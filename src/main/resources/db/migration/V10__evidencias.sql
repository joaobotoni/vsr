-- Remoção lógica da evidência: a aplicação não apaga linhas nem arquivos. O trigger tg_evidencia_regras (V9) já exige
-- a vistoria em andamento para incluir, alterar ou remover.
alter table vistorias.evidencia
    add column removido_em timestamptz,
    add constraint ck_evidencia_removido_em check (removido_em is null or removido_em >= created_at);

drop index vistorias.ix_evidencia_ambiente;
create index ix_evidencia_ambiente on vistorias.evidencia (id_ambiente) where removido_em is null;
