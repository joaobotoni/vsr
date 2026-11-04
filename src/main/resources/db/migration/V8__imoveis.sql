-- Identificadores públicos: a API expõe UUID, nunca o id sequencial.
alter table pessoas.pessoa
    add column uuid uuid not null default gen_random_uuid(),
    add constraint uq_pessoa_uuid unique (uuid);

alter table catalogo.tipo_ambiente
    add column uuid uuid not null default gen_random_uuid(),
    add constraint uq_tipo_ambiente_uuid unique (uuid);

alter table catalogo.tipo_item
    add column uuid uuid not null default gen_random_uuid(),
    add constraint uq_tipo_item_uuid unique (uuid);

-- Remoção lógica do imóvel: a aplicação não apaga linhas.
alter table imoveis.imovel
    add column removido_em timestamptz,
    add constraint ck_imovel_removido_em check (removido_em is null or removido_em >= created_at);

-- O titular não repete o endereço entre os imóveis ativos; um imóvel removido libera o endereço.
drop index imoveis.uq_imovel_endereco_titular;
create unique index uq_imovel_endereco_titular on imoveis.imovel (id_endereco, coalesce(id_usuario, 0), coalesce(id_pessoa_juridica, 0))
    where removido_em is null;

drop index imoveis.ix_imovel_usuario;
create index ix_imovel_usuario on imoveis.imovel (id_usuario) where removido_em is null;

drop index imoveis.ix_imovel_pessoa_juridica;
create index ix_imovel_pessoa_juridica on imoveis.imovel (id_pessoa_juridica) where removido_em is null;

-- Localiza o endereço pela cadeia estado, cidade, bairro, logradouro e endereço, criando o que faltar.
-- Os nomes são comparados sem diferenciar maiúsculas de minúsculas, como nos índices únicos.
create function enderecos.registrar_endereco(
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
    on conflict (id_estado, lower(nome)) do nothing;

    select id_cidade
    into v_cidade
    from enderecos.cidade
    where id_estado = v_estado
      and lower(nome) = lower(p_cidade);

    insert into enderecos.bairro (id_cidade, nome)
    values (v_cidade, p_bairro)
    on conflict (id_cidade, lower(nome)) do nothing;

    select id_bairro
    into v_bairro
    from enderecos.bairro
    where id_cidade = v_cidade
      and lower(nome) = lower(p_bairro);

    insert into enderecos.logradouro (id_bairro, cep, nome)
    values (v_bairro, p_cep, p_logradouro)
    on conflict (id_bairro, cep, lower(nome)) do nothing;

    select id_logradouro
    into v_logradouro
    from enderecos.logradouro
    where id_bairro = v_bairro
      and cep = p_cep
      and lower(nome) = lower(p_logradouro);

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

-- Remove o imóvel logicamente. Recusa se houver vistoria em andamento (rn_imovel_vistoria_andamento).
create procedure imoveis.remover_imovel(
    p_id_imovel int
)
    language plpgsql
as
$$
begin
    if exists (select 1
               from vistorias.vistoria
               where id_imovel = p_id_imovel
                 and status = 'em_andamento') then
        call rotinas.violar('rn_imovel_vistoria_andamento');
    end if;

    update imoveis.imovel
    set removido_em = now()
    where id_imovel = p_id_imovel
      and removido_em is null;
end;
$$;
