-- =====================================================================================================================
-- Regras que antes só a aplicação garantia.
-- =====================================================================================================================

-- O responsável pela vistoria é o dono do imóvel ou membro ativo da empresa dele (rn_vistoria_responsavel).
create function vistorias.tg_vistoria_responsavel()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from imoveis.imovel i
                   where i.id_imovel = new.id_imovel
                     and (i.id_usuario = new.id_usuario_responsavel
                       or (i.id_pessoa_juridica is not null and vinculos.membro(new.id_usuario_responsavel, i.id_pessoa_juridica)))) then
        call rotinas.violar('rn_vistoria_responsavel');
    end if;

    return new;
end;
$$;

create trigger tg_vistoria_responsavel
    before insert
    on vistorias.vistoria
    for each row
execute function vistorias.tg_vistoria_responsavel();

-- O tipo do catálogo usado num ambiente ou item é padrão, do responsável pela vistoria ou de um membro da empresa
-- da vistoria (rn_catalogo_uso).
create function catalogo.uso_permitido(p_id_usuario_tipo int, p_padrao boolean, p_id_vistoria int)
    returns boolean
    language sql stable
as
$$
select p_padrao
           or exists (select 1
                      from vistorias.vistoria v
                      where v.id_vistoria = p_id_vistoria
                        and (v.id_usuario_responsavel = p_id_usuario_tipo
                          or (v.id_pessoa_juridica is not null and vinculos.membro(p_id_usuario_tipo, v.id_pessoa_juridica))));
$$;

create function vistorias.tg_ambiente_catalogo()
    returns trigger
    language plpgsql
as
$$
begin
    if new.id_tipo_ambiente is not null and not exists (select 1
                                                        from catalogo.tipo_ambiente t
                                                        where t.id_tipo_ambiente = new.id_tipo_ambiente
                                                          and catalogo.uso_permitido(t.id_usuario, t.padrao, new.id_vistoria)) then
        call rotinas.violar('rn_catalogo_uso');
    end if;

    return new;
end;
$$;

create trigger tg_ambiente_catalogo
    before insert
    on vistorias.ambiente
    for each row
execute function vistorias.tg_ambiente_catalogo();

create function vistorias.tg_item_catalogo()
    returns trigger
    language plpgsql
as
$$
begin
    if new.id_tipo_item is not null and not exists (select 1
                                                    from catalogo.tipo_item t
                                                             join vistorias.ambiente a on a.id_ambiente = new.id_ambiente
                                                    where t.id_tipo_item = new.id_tipo_item
                                                      and catalogo.uso_permitido(t.id_usuario, t.padrao, a.id_vistoria)) then
        call rotinas.violar('rn_catalogo_uso');
    end if;

    return new;
end;
$$;

create trigger tg_item_catalogo
    before insert
    on vistorias.item
    for each row
execute function vistorias.tg_item_catalogo();

-- Empresa da plataforma: os vínculos e os imóveis de empresa só aceitam empresa sem titular (não o cadastro privado
-- de um usuário), e o membro é sempre a pessoa de uma conta (rn_empresa_cadastro).
create function vinculos.tg_vinculo_cadastro()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from pessoas.pessoa
                   where id_pessoa = new.id_pessoa_juridica
                     and id_usuario_titular is null)
        or not exists (select 1
                       from usuarios.usuario
                       where id_pessoa = new.id_pessoa_fisica) then
        call rotinas.violar('rn_empresa_cadastro');
    end if;

    return new;
end;
$$;

create trigger tg_vinculo_cadastro
    before insert
    on vinculos.vinculo_pessoa_empresa
    for each row
execute function vinculos.tg_vinculo_cadastro();

create function imoveis.tg_imovel_empresa()
    returns trigger
    language plpgsql
as
$$
begin
    if new.id_pessoa_juridica is not null and not exists (select 1
                                                          from pessoas.pessoa
                                                          where id_pessoa = new.id_pessoa_juridica
                                                            and id_usuario_titular is null) then
        call rotinas.violar('rn_empresa_cadastro');
    end if;

    return new;
end;
$$;

create trigger tg_imovel_empresa
    before insert
    on imoveis.imovel
    for each row
execute function imoveis.tg_imovel_empresa();

-- =====================================================================================================================
-- Integridade das fotos: o SHA-256 do arquivo, conferido a cada download.
-- =====================================================================================================================
alter table vistorias.evidencia
    add column hash_sha256 bytea,
    add constraint ck_evidencia_hash check (hash_sha256 is null or length(hash_sha256) = 32);

-- =====================================================================================================================
-- Controle de concorrência: versão incrementada a cada alteração feita pela aplicação.
-- =====================================================================================================================
alter table imoveis.imovel
    add column versao int not null default 0;
alter table vistorias.vistoria
    add column versao int not null default 0;
alter table vistorias.ambiente
    add column versao int not null default 0;
alter table vistorias.item
    add column versao int not null default 0;
alter table pessoas.pessoa
    add column versao int not null default 0;

-- =====================================================================================================================
-- Auditoria: toda inclusão, alteração e exclusão em imóveis, vistorias e vínculos, com o usuário que a aplicação
-- informa na transação (set_config('vsr.usuario', <uuid>, true)). A role da aplicação não lê nem altera o histórico:
-- a gravação passa por uma função SECURITY DEFINER.
-- =====================================================================================================================
create schema auditoria;

create table auditoria.registro
(
    id_registro   bigint generated always as identity,
    tabela        text        not null,
    operacao      text        not null,
    usuario       uuid,
    antes         jsonb,
    depois        jsonb,
    registrado_em timestamptz not null default now(),
    constraint pk_registro primary key (id_registro),
    constraint ck_registro_operacao check (operacao in ('INSERT', 'UPDATE', 'DELETE'))
);

create index ix_registro_tabela_data on auditoria.registro (tabela, registrado_em);

create function auditoria.tg_registrar()
    returns trigger
    language plpgsql
    security definer
    set search_path = pg_catalog, pg_temp
as
$$
begin
    insert into auditoria.registro (tabela, operacao, usuario, antes, depois)
    values (tg_table_schema || '.' || tg_table_name,
            tg_op,
            nullif(current_setting('vsr.usuario', true), '')::uuid,
            case when tg_op <> 'INSERT' then to_jsonb(old) end,
            case when tg_op <> 'DELETE' then to_jsonb(new) end);

    return null;
end;
$$;

revoke all on function auditoria.tg_registrar() from public;

create trigger tg_imovel_auditoria
    after insert or update or delete
    on imoveis.imovel
    for each row
execute function auditoria.tg_registrar();

create trigger tg_imovel_proprietario_auditoria
    after insert or update or delete
    on imoveis.imovel_proprietario
    for each row
execute function auditoria.tg_registrar();

create trigger tg_vistoria_auditoria
    after insert or update or delete
    on vistorias.vistoria
    for each row
execute function auditoria.tg_registrar();

create trigger tg_vistoria_pessoa_auditoria
    after insert or update or delete
    on vistorias.vistoria_pessoa
    for each row
execute function auditoria.tg_registrar();

create trigger tg_ambiente_auditoria
    after insert or update or delete
    on vistorias.ambiente
    for each row
execute function auditoria.tg_registrar();

create trigger tg_item_auditoria
    after insert or update or delete
    on vistorias.item
    for each row
execute function auditoria.tg_registrar();

create trigger tg_evidencia_auditoria
    after insert or update or delete
    on vistorias.evidencia
    for each row
execute function auditoria.tg_registrar();

create trigger tg_vinculo_auditoria
    after insert or update or delete
    on vinculos.vinculo_pessoa_empresa
    for each row
execute function auditoria.tg_registrar();
