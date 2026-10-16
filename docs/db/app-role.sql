-- Role da aplicação com o mínimo de privilégios.
--
-- Rode com psql, conectado como o dono das migrations (FLYWAY_USERNAME), depois que o Flyway criou o schema:
--   psql -v app_password='...' -f docs/db/app-role.sql
-- Depois, aponte DB_USERNAME/DB_PASSWORD para vsr_app e FLYWAY_USERNAME/FLYWAY_PASSWORD para o dono.
--
-- A aplicação só lê, insere e atualiza: não apaga linhas (sessões expiradas saem pelo job do pg_cron, que roda
-- como dono), não cria nem altera objetos e não acessa o schema public.

create role vsr_app login password :'app_password';

grant usage on schema documento, pessoas, usuarios, vinculos, enderecos, imoveis, catalogo, vistorias, rotinas to vsr_app;

grant select, insert, update on all tables in schema pessoas, usuarios, vinculos, enderecos, imoveis, vistorias to vsr_app;
grant select on all tables in schema catalogo to vsr_app;
grant usage, select on all sequences in schema pessoas, usuarios, vinculos, enderecos, imoveis, vistorias to vsr_app;

grant execute on all routines in schema documento, usuarios to vsr_app;
grant execute on procedure rotinas.violar(text) to vsr_app;
revoke execute on procedure rotinas.limpar_sessoes_expiradas() from public;

-- Objetos criados por migrations futuras recebem os mesmos privilégios.
alter default privileges in schema pessoas, usuarios, vinculos, enderecos, imoveis, vistorias
    grant select, insert, update on tables to vsr_app;
alter default privileges in schema catalogo
    grant select on tables to vsr_app;
alter default privileges in schema pessoas, usuarios, vinculos, enderecos, imoveis, vistorias
    grant usage, select on sequences to vsr_app;
alter default privileges in schema documento, usuarios
    grant execute on routines to vsr_app;
