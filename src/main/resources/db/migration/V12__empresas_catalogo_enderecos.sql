-- =====================================================================================================================
-- Endereços: nomes comparados sem acento e sem diferenciar maiúsculas ("Sao Paulo" e "SÃO PAULO" são a mesma cidade).
-- =====================================================================================================================
create function documento.sem_acento(p_texto text)
    returns text
    language sql immutable parallel safe
as
$$
select translate(lower(p_texto),
                 'áàâãäåéèêëíìîïóòôõöúùûüçñý',
                 'aaaaaaeeeeiiiiooooouuuucny');
$$;

drop index enderecos.uq_cidade_nome;
create unique index uq_cidade_nome on enderecos.cidade (id_estado, documento.sem_acento(nome));

drop index enderecos.uq_bairro_nome;
create unique index uq_bairro_nome on enderecos.bairro (id_cidade, documento.sem_acento(nome));

drop index enderecos.uq_logradouro;
create unique index uq_logradouro on enderecos.logradouro (id_bairro, cep, documento.sem_acento(nome));

create or replace function enderecos.registrar_endereco(
    p_uf text,
    p_cidade text,
    p_bairro text,
    p_cep text,
    p_logradouro text,
    p_numero text,
    p_complemento text
)
    returns int
    language plpgsql
as
$$
declare
    v_estado     int;
    v_cidade     int;
    v_bairro     int;
    v_logradouro int;
    v_endereco   int;
begin
    select id_estado
    into v_estado
    from enderecos.estado
    where sigla = p_uf;

    if v_estado is null then
        call rotinas.violar('rn_estado_existente');
    end if;

    insert into enderecos.cidade (id_estado, nome)
    values (v_estado, p_cidade)
    on conflict (id_estado, documento.sem_acento(nome)) do nothing;

    select id_cidade
    into v_cidade
    from enderecos.cidade
    where id_estado = v_estado
      and documento.sem_acento(nome) = documento.sem_acento(p_cidade);

    insert into enderecos.bairro (id_cidade, nome)
    values (v_cidade, p_bairro)
    on conflict (id_cidade, documento.sem_acento(nome)) do nothing;

    select id_bairro
    into v_bairro
    from enderecos.bairro
    where id_cidade = v_cidade
      and documento.sem_acento(nome) = documento.sem_acento(p_bairro);

    insert into enderecos.logradouro (id_bairro, cep, nome)
    values (v_bairro, p_cep, p_logradouro)
    on conflict (id_bairro, cep, documento.sem_acento(nome)) do nothing;

    select id_logradouro
    into v_logradouro
    from enderecos.logradouro
    where id_bairro = v_bairro
      and cep = p_cep
      and documento.sem_acento(nome) = documento.sem_acento(p_logradouro);

    insert into enderecos.endereco (id_logradouro, numero, complemento)
    values (v_logradouro, p_numero, p_complemento)
    on conflict (id_logradouro, numero, coalesce(complemento, '')) do nothing;

    select id_endereco
    into v_endereco
    from enderecos.endereco
    where id_logradouro = v_logradouro
      and numero = p_numero
      and coalesce(complemento, '') = coalesce(p_complemento, '');

    return v_endereco;
end;
$$;

-- =====================================================================================================================
-- Catálogo personalizado: a aplicação só cria tipos do próprio usuário; os padrão não mudam (rn_catalogo_personalizado).
-- Um tipo de ambiente personalizado só recebe itens padrão ou do mesmo usuário (rn_catalogo_item).
-- O dono das tabelas (as migrations) fica fora das regras, para poder carregar os tipos padrão.
-- =====================================================================================================================
create function catalogo.dono(p_tabela text)
    returns boolean
    language sql stable
as
$$
select current_user = (select tableowner from pg_tables where schemaname = 'catalogo' and tablename = p_tabela);
$$;

create function catalogo.tg_tipo_personalizado()
    returns trigger
    language plpgsql
as
$$
begin
    if catalogo.dono(tg_table_name) then
        return new;
    end if;

    if new.padrao or new.id_usuario is null or new.id_pessoa_juridica is not null then
        call rotinas.violar('rn_catalogo_personalizado');
    end if;

    return new;
end;
$$;

create trigger tg_tipo_ambiente_personalizado
    before insert
    on catalogo.tipo_ambiente
    for each row
execute function catalogo.tg_tipo_personalizado();

create trigger tg_tipo_item_personalizado
    before insert
    on catalogo.tipo_item
    for each row
execute function catalogo.tg_tipo_personalizado();

create function catalogo.tg_tipo_ambiente_item_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if catalogo.dono(tg_table_name) then
        return new;
    end if;

    if not exists (select 1
                   from catalogo.tipo_ambiente a
                            join catalogo.tipo_item i on i.id_tipo_item = new.id_tipo_item
                   where a.id_tipo_ambiente = new.id_tipo_ambiente
                     and not a.padrao
                     and (i.padrao or i.id_usuario = a.id_usuario)) then
        call rotinas.violar('rn_catalogo_item');
    end if;

    return new;
end;
$$;

create trigger tg_tipo_ambiente_item_regras
    before insert
    on catalogo.tipo_ambiente_item
    for each row
execute function catalogo.tg_tipo_ambiente_item_regras();

-- =====================================================================================================================
-- Empresas e vínculos: a empresa é um cadastro compartilhado (pessoa jurídica sem titular, CNPJ único na plataforma);
-- os membros são as pessoas das contas, vinculadas a ela. Sócio, representante e MEI administram; funcionário opera.
-- =====================================================================================================================
alter table vinculos.vinculo_pessoa_empresa
    add column removido_em timestamptz;

create index ix_vinculo_pessoa_fisica on vinculos.vinculo_pessoa_empresa (id_pessoa_fisica) where removido_em is null;

-- O usuário é membro ativo da empresa.
create function vinculos.membro(p_id_usuario int, p_id_pessoa_juridica int)
    returns boolean
    language sql stable
as
$$
select exists (select 1
               from vinculos.vinculo_pessoa_empresa v
                        join usuarios.usuario u on u.id_pessoa = v.id_pessoa_fisica
               where u.id_usuario = p_id_usuario
                 and v.id_pessoa_juridica = p_id_pessoa_juridica
                 and v.removido_em is null);
$$;

-- A empresa nunca fica sem administrador ativo (rn_empresa_administrador).
create function vinculos.tg_vinculo_administrador()
    returns trigger
    language plpgsql
as
$$
begin
    if old.removido_em is not null or old.tipo = 'funcionario' then
        return new;
    end if;

    if new.removido_em is null and new.tipo <> 'funcionario' then
        return new;
    end if;

    perform 1
    from pessoas.pessoa_juridica
    where id_pessoa = old.id_pessoa_juridica
        for update;

    if not exists (select 1
                   from vinculos.vinculo_pessoa_empresa
                   where id_pessoa_juridica = old.id_pessoa_juridica
                     and id_vinculo_pessoa_empresa <> old.id_vinculo_pessoa_empresa
                     and tipo <> 'funcionario'
                     and removido_em is null) then
        call rotinas.violar('rn_empresa_administrador');
    end if;

    return new;
end;
$$;

create trigger tg_vinculo_administrador
    before update
    on vinculos.vinculo_pessoa_empresa
    for each row
execute function vinculos.tg_vinculo_administrador();

-- A vistoria herda a empresa do imóvel.
create function vistorias.tg_vistoria_empresa()
    returns trigger
    language plpgsql
as
$$
begin
    select id_pessoa_juridica
    into new.id_pessoa_juridica
    from imoveis.imovel
    where id_imovel = new.id_imovel;

    return new;
end;
$$;

create trigger tg_vistoria_empresa
    before insert
    on vistorias.vistoria
    for each row
execute function vistorias.tg_vistoria_empresa();

-- Proprietário e pessoa da vistoria: do cadastro do dono do imóvel ou do responsável pela vistoria, ou de um membro da
-- empresa do imóvel ou da vistoria (rn_pessoa_titular).
create or replace function imoveis.tg_imovel_proprietario_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from pessoas.pessoa p
                            join imoveis.imovel i on i.id_imovel = new.id_imovel
                   where p.id_pessoa = new.id_pessoa
                     and (p.id_usuario_titular = i.id_usuario
                       or (i.id_pessoa_juridica is not null and vinculos.membro(p.id_usuario_titular, i.id_pessoa_juridica)))) then
        call rotinas.violar('rn_pessoa_titular');
    end if;

    return new;
end;
$$;

create or replace function vistorias.tg_vistoria_pessoa_titular()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from pessoas.pessoa p
                            join vistorias.vistoria v on v.id_vistoria = new.id_vistoria
                   where p.id_pessoa = new.id_pessoa
                     and (p.id_usuario_titular = v.id_usuario_responsavel
                       or (v.id_pessoa_juridica is not null and vinculos.membro(p.id_usuario_titular, v.id_pessoa_juridica)))) then
        call rotinas.violar('rn_pessoa_titular');
    end if;

    return new;
end;
$$;
