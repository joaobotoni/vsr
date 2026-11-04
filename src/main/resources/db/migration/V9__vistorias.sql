-- Remoção lógica: a aplicação não apaga linhas.
alter table vistorias.vistoria
    add column removido_em timestamptz,
    add constraint ck_vistoria_removido_em check (removido_em is null or removido_em >= created_at);

alter table vistorias.ambiente
    add column removido_em timestamptz,
    add constraint ck_ambiente_removido_em check (removido_em is null or removido_em >= created_at);

alter table vistorias.item
    add column removido_em timestamptz,
    add constraint ck_item_removido_em check (removido_em is null or removido_em >= created_at);

-- Nomes únicos só entre os ativos: um ambiente ou item removido libera o nome.
drop index vistorias.uq_ambiente_nome;
create unique index uq_ambiente_nome on vistorias.ambiente (id_vistoria, lower(nome)) where removido_em is null;

drop index vistorias.uq_item_nome;
create unique index uq_item_nome on vistorias.item (id_ambiente, lower(nome)) where removido_em is null;

-- Uma vistoria em andamento por imóvel, e uma vistoria de saída por vistoria de entrada.
create unique index uq_vistoria_andamento on vistorias.vistoria (id_imovel)
    where status = 'em_andamento' and removido_em is null;

create unique index uq_vistoria_saida on vistorias.vistoria (id_vistoria_entrada)
    where id_vistoria_entrada is not null and removido_em is null;

-- Vistoria: finalizada ou removida não muda mais (rn_vistoria_em_andamento); o imóvel precisa estar ativo
-- (rn_imovel_ativo); a vistoria de entrada referenciada precisa ser de entrada, finalizada e ativa (rn_vistoria_entrada).
create function vistorias.tg_vistoria_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if tg_op = 'UPDATE' and (old.status = 'finalizada' or old.removido_em is not null) then
        call rotinas.violar('rn_vistoria_em_andamento');
    end if;

    if tg_op = 'INSERT' and not exists (select 1
                                        from imoveis.imovel
                                        where id_imovel = new.id_imovel
                                          and removido_em is null) then
        call rotinas.violar('rn_imovel_ativo');
    end if;

    if new.id_vistoria_entrada is not null and not exists (select 1
                                                           from vistorias.vistoria e
                                                           where e.id_vistoria = new.id_vistoria_entrada
                                                             and e.tipo = 'entrada'
                                                             and e.status = 'finalizada'
                                                             and e.removido_em is null) then
        call rotinas.violar('rn_vistoria_entrada');
    end if;

    return new;
end;
$$;

create trigger tg_vistoria_regras
    before insert or update
    on vistorias.vistoria
    for each row
execute function vistorias.tg_vistoria_regras();

-- Ambiente: só muda com a vistoria em andamento; a origem é um ambiente da vistoria de entrada (rn_ambiente_origem).
create function vistorias.tg_ambiente_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from vistorias.vistoria
                   where id_vistoria = new.id_vistoria
                     and status = 'em_andamento'
                     and removido_em is null) then
        call rotinas.violar('rn_vistoria_em_andamento');
    end if;

    if new.id_ambiente_origem is not null and not exists (select 1
                                                          from vistorias.ambiente o
                                                                   join vistorias.vistoria v on v.id_vistoria = new.id_vistoria
                                                          where o.id_ambiente = new.id_ambiente_origem
                                                            and o.id_vistoria = v.id_vistoria_entrada) then
        call rotinas.violar('rn_ambiente_origem');
    end if;

    return new;
end;
$$;

create trigger tg_ambiente_regras
    before insert or update
    on vistorias.ambiente
    for each row
execute function vistorias.tg_ambiente_regras();

-- Item: só muda com a vistoria em andamento; a origem é um item do ambiente de origem (rn_item_origem).
create function vistorias.tg_item_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from vistorias.ambiente a
                            join vistorias.vistoria v on v.id_vistoria = a.id_vistoria
                   where a.id_ambiente = new.id_ambiente
                     and v.status = 'em_andamento'
                     and v.removido_em is null) then
        call rotinas.violar('rn_vistoria_em_andamento');
    end if;

    if new.id_item_origem is not null and not exists (select 1
                                                      from vistorias.item o
                                                               join vistorias.ambiente a on a.id_ambiente = new.id_ambiente
                                                      where o.id_item = new.id_item_origem
                                                        and o.id_ambiente = a.id_ambiente_origem) then
        call rotinas.violar('rn_item_origem');
    end if;

    return new;
end;
$$;

create trigger tg_item_regras
    before insert or update
    on vistorias.item
    for each row
execute function vistorias.tg_item_regras();

-- Evidência e pessoa vinculada: só mudam com a vistoria em andamento.
create function vistorias.tg_evidencia_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from vistorias.ambiente a
                            join vistorias.vistoria v on v.id_vistoria = a.id_vistoria
                   where a.id_ambiente = new.id_ambiente
                     and v.status = 'em_andamento'
                     and v.removido_em is null) then
        call rotinas.violar('rn_vistoria_em_andamento');
    end if;

    return new;
end;
$$;

create trigger tg_evidencia_regras
    before insert or update
    on vistorias.evidencia
    for each row
execute function vistorias.tg_evidencia_regras();

create function vistorias.tg_vistoria_pessoa_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from vistorias.vistoria
                   where id_vistoria = new.id_vistoria
                     and status = 'em_andamento'
                     and removido_em is null) then
        call rotinas.violar('rn_vistoria_em_andamento');
    end if;

    return new;
end;
$$;

create trigger tg_vistoria_pessoa_regras
    before insert or update
    on vistorias.vistoria_pessoa
    for each row
execute function vistorias.tg_vistoria_pessoa_regras();

-- Copia os ambientes e itens ativos da vistoria de entrada para a vistoria de saída, ligando cada cópia à origem.
-- Estado e observações ficam em branco: são avaliados de novo na saída.
create procedure vistorias.copiar_vistoria(
    p_id_vistoria int
)
    language plpgsql
as
$$
declare
    v_entrada int;
begin
    select id_vistoria_entrada
    into v_entrada
    from vistorias.vistoria
    where id_vistoria = p_id_vistoria;

    if v_entrada is null then
        return;
    end if;

    insert into vistorias.ambiente (id_vistoria, id_tipo_ambiente, nome, id_ambiente_origem)
    select p_id_vistoria, a.id_tipo_ambiente, a.nome, a.id_ambiente
    from vistorias.ambiente a
    where a.id_vistoria = v_entrada
      and a.removido_em is null
    order by a.id_ambiente;

    insert into vistorias.item (id_ambiente, id_tipo_item, nome, descricao, material, id_item_origem)
    select n.id_ambiente, i.id_tipo_item, i.nome, i.descricao, i.material, i.id_item
    from vistorias.item i
             join vistorias.ambiente n on n.id_ambiente_origem = i.id_ambiente
    where n.id_vistoria = p_id_vistoria
      and i.removido_em is null
    order by i.id_item;
end;
$$;

-- Vistorias removidas não impedem a remoção do imóvel.
create or replace procedure imoveis.remover_imovel(
    p_id_imovel int
)
    language plpgsql
as
$$
begin
    if exists (select 1
               from vistorias.vistoria
               where id_imovel = p_id_imovel
                 and status = 'em_andamento'
                 and removido_em is null) then
        call rotinas.violar('rn_imovel_vistoria_andamento');
    end if;

    update imoveis.imovel
    set removido_em = now()
    where id_imovel = p_id_imovel
      and removido_em is null;
end;
$$;
