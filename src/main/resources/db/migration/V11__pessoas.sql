-- Pessoas privadas: cada usuário tem o próprio cadastro dos proprietários e inquilinos com que trabalha.
-- id_usuario_titular nulo é a pessoa de uma conta (o próprio usuário); preenchido, é o usuário dono do cadastro.
-- Nada de um cadastro vaza para outro usuário: nem o nome, nem o fato de o CPF ou o CNPJ já existir.
alter table pessoas.pessoa
    add column id_usuario_titular int,
    add constraint fk_pessoa_usuario_titular foreign key (id_usuario_titular) references usuarios.usuario (id_usuario) on delete cascade;

create index ix_pessoa_usuario_titular on pessoas.pessoa (id_usuario_titular) where id_usuario_titular is not null;

-- O titular é copiado para as tabelas de documento, para o CPF e o CNPJ serem únicos por titular.
alter table pessoas.pessoa_fisica
    add column id_usuario_titular int;

alter table pessoas.pessoa_juridica
    add column id_usuario_titular int;

create function pessoas.tg_documento_titular()
    returns trigger
    language plpgsql
as
$$
begin
    select id_usuario_titular
    into new.id_usuario_titular
    from pessoas.pessoa
    where id_pessoa = new.id_pessoa;

    return new;
end;
$$;

create trigger tg_pessoa_fisica_titular
    before insert or update
    on pessoas.pessoa_fisica
    for each row
execute function pessoas.tg_documento_titular();

create trigger tg_pessoa_juridica_titular
    before insert or update
    on pessoas.pessoa_juridica
    for each row
execute function pessoas.tg_documento_titular();

-- O titular não muda depois do cadastro (rn_pessoa_titular_fixo).
create function pessoas.tg_pessoa_titular_fixo()
    returns trigger
    language plpgsql
as
$$
begin
    if new.id_usuario_titular is distinct from old.id_usuario_titular then
        call rotinas.violar('rn_pessoa_titular_fixo');
    end if;

    return new;
end;
$$;

create trigger tg_pessoa_titular_fixo
    before update
    on pessoas.pessoa
    for each row
execute function pessoas.tg_pessoa_titular_fixo();

-- CPF e CNPJ: únicos entre as contas, e únicos dentro do cadastro de cada usuário.
alter table pessoas.pessoa_fisica
    drop constraint uq_pessoa_fisica_cpf;
create unique index uq_pessoa_fisica_cpf on pessoas.pessoa_fisica (cpf) where id_usuario_titular is null;
create unique index uq_pessoa_fisica_titular_cpf on pessoas.pessoa_fisica (id_usuario_titular, cpf) where id_usuario_titular is not null;

alter table pessoas.pessoa_juridica
    drop constraint uq_pessoa_juridica_cnpj;
create unique index uq_pessoa_juridica_cnpj on pessoas.pessoa_juridica (cnpj) where id_usuario_titular is null;
create unique index uq_pessoa_juridica_titular_cnpj on pessoas.pessoa_juridica (id_usuario_titular, cnpj) where id_usuario_titular is not null;

-- Contato: telefone ou e-mail, ao menos um.
alter table pessoas.pessoa_contato
    alter column telefone drop not null,
    alter column email drop not null,
    drop constraint ck_pessoa_contato_telefone,
    drop constraint ck_pessoa_contato_email,
    add constraint ck_pessoa_contato_telefone check (telefone is null or documento.telefone_valido(telefone)),
    add constraint ck_pessoa_contato_email check (email is null or documento.email_valido(email)),
    add constraint ck_pessoa_contato_meio check (telefone is not null or email is not null);

-- Vínculos desfeitos logicamente: a aplicação não apaga linhas. Vincular de novo reativa o vínculo.
alter table imoveis.imovel_proprietario
    add column removido_em timestamptz;

alter table vistorias.vistoria_pessoa
    add column removido_em timestamptz;

-- Só pessoas do cadastro do próprio usuário entram como proprietárias do imóvel dele ou como pessoas da vistoria
-- dele (rn_pessoa_titular).
create function imoveis.tg_imovel_proprietario_regras()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from pessoas.pessoa p
                            join imoveis.imovel i on i.id_imovel = new.id_imovel
                   where p.id_pessoa = new.id_pessoa
                     and p.id_usuario_titular = i.id_usuario) then
        call rotinas.violar('rn_pessoa_titular');
    end if;

    return new;
end;
$$;

create trigger tg_imovel_proprietario_regras
    before insert or update
    on imoveis.imovel_proprietario
    for each row
execute function imoveis.tg_imovel_proprietario_regras();

create function vistorias.tg_vistoria_pessoa_titular()
    returns trigger
    language plpgsql
as
$$
begin
    if not exists (select 1
                   from pessoas.pessoa p
                            join vistorias.vistoria v on v.id_vistoria = new.id_vistoria
                   where p.id_pessoa = new.id_pessoa
                     and p.id_usuario_titular = v.id_usuario_responsavel) then
        call rotinas.violar('rn_pessoa_titular');
    end if;

    return new;
end;
$$;

create trigger tg_vistoria_pessoa_titular
    before insert or update
    on vistorias.vistoria_pessoa
    for each row
execute function vistorias.tg_vistoria_pessoa_titular();
