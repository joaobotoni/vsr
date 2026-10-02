create
extension if not exists pgcrypto;

create schema documento;
create schema pessoas;
create schema usuarios;
create schema vinculos;
create schema enderecos;
create schema imoveis;
create schema catalogo;
create schema vistorias;

create function public.tg_set_updated_at()
    returns trigger
    language plpgsql
as
$$
begin
    new.updated_at
:= now();
return new;
end;
$$;

create function documento.dv_modulo11(p_base text, p_peso_maximo int)
    returns int
    language plpgsql
    immutable strict parallel safe
as
$$
declare
v_tam   int := length(p_base);
    v_ciclo
int := p_peso_maximo - 1;
    v_soma
int := 0;
    v_peso
int;
    v_valor
int;
    v_resto
int;
    i
int;
begin
for i in 1..v_tam
        loop
            v_peso := ((v_tam - i) % v_ciclo) + 2;
            v_valor
:= ascii(substr(p_base, i, 1)) - 48;
            v_soma
:= v_soma + v_valor * v_peso;
end loop;

    v_resto
:= v_soma % 11;

    if
v_resto < 2 then
        return 0;
else
        return 11 - v_resto;
end if;
end;
$$;

create function documento.cpf_valido(p_cpf text)
    returns boolean
    language plpgsql
    immutable parallel safe
as
$$
begin
    if
p_cpf is null or p_cpf !~ '^[0-9]{11}$' then
        return false;
end if;

    if
p_cpf = repeat(left(p_cpf, 1), 11) then
        return false;
end if;

    if
documento.dv_modulo11(substr(p_cpf, 1, 9), 11) <> ascii(substr(p_cpf, 10, 1)) - 48 then
        return false;
end if;

    if
documento.dv_modulo11(substr(p_cpf, 1, 10), 11) <> ascii(substr(p_cpf, 11, 1)) - 48 then
        return false;
end if;

return true;
end;
$$;

create function documento.cnpj_valido(p_cnpj text)
    returns boolean
    language plpgsql
    immutable parallel safe
as
$$
begin
    if
p_cnpj is null or p_cnpj !~ '^[0-9A-Z]{12}[0-9]{2}$' then
        return false;
end if;

    if
p_cnpj = repeat(left(p_cnpj, 1), 14) then
        return false;
end if;

    if
documento.dv_modulo11(substr(p_cnpj, 1, 12), 9) <> ascii(substr(p_cnpj, 13, 1)) - 48 then
        return false;
end if;

    if
documento.dv_modulo11(substr(p_cnpj, 1, 13), 9) <> ascii(substr(p_cnpj, 14, 1)) - 48 then
        return false;
end if;

return true;
end;
$$;

create function documento.cep_valido(p_cep text)
    returns boolean
    language sql immutable parallel safe
as
$$
select p_cep is not null
           and p_cep ~ '^[0-9]{8}$'
           and p_cep <> '00000000';
$$;

create function documento.uf_valida(p_uf text)
    returns boolean
    language sql immutable parallel safe
as
$$
select p_uf is not null
           and p_uf = any (array['AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
                           'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO']);
$$;

create function documento.telefone_valido(p_telefone text)
    returns boolean
    language sql immutable parallel safe
as
$$
select p_telefone is not null
           and p_telefone ~ '^[1-9][1-9](9[0-9]{8}|[2-5][0-9]{7})$';
$$;

create function documento.texto_valido(p_texto text, p_min int, p_max int)
    returns boolean
    language sql immutable parallel safe
as
$$
select p_texto is not null
           and p_texto = btrim(p_texto)
           and length(p_texto) between p_min and p_max;
$$;

create function usuarios.email_valido(p_email text)
    returns boolean
    language sql immutable parallel safe
as
$$
select p_email is not null
           and p_email = lower(btrim(p_email))
           and length(p_email) between 6 and 254
           and p_email ~ '^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$';
$$;

create type pessoas.tipo_pessoa as enum ('pf', 'pj');

create table pessoas.pessoa
(
    id_pessoa  int generated always as identity,
    tipo       pessoas.tipo_pessoa not null,
    nome       text                not null,
    created_at timestamptz         not null default now(),
    updated_at timestamptz         not null default now(),
    constraint pk_pessoa primary key (id_pessoa),
    constraint uq_pessoa_id_tipo unique (id_pessoa, tipo)
);

create table pessoas.pessoa_fisica
(
    id_pessoa  int                 not null,
    tipo       pessoas.tipo_pessoa not null default 'pf',
    cpf        text                not null,
    created_at timestamptz         not null default now(),
    updated_at timestamptz         not null default now(),
    constraint pk_pessoa_fisica primary key (id_pessoa),
    constraint fk_pessoa_fisica_pessoa foreign key (id_pessoa, tipo) references pessoas.pessoa (id_pessoa, tipo) on delete cascade
);

create table pessoas.pessoa_juridica
(
    id_pessoa     int                 not null,
    tipo          pessoas.tipo_pessoa not null default 'pj',
    cnpj          text                not null,
    nome_fantasia text                not null,
    created_at    timestamptz         not null default now(),
    updated_at    timestamptz         not null default now(),
    constraint pk_pessoa_juridica primary key (id_pessoa),
    constraint fk_pessoa_juridica_pessoa foreign key (id_pessoa, tipo) references pessoas.pessoa (id_pessoa, tipo) on delete cascade
);

create table usuarios.usuario
(
    id_usuario          int generated always as identity,
    id_pessoa           int         not null,
    email               text        not null,
    email_verificado_em timestamptz,
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now(),
    constraint pk_usuario primary key (id_usuario),
    constraint fk_usuario_pessoa foreign key (id_pessoa) references pessoas.pessoa_fisica (id_pessoa) on delete cascade
);

create type usuarios.provedor_social as enum ('google');
create type usuarios.plataforma_dispositivo as enum ('android', 'ios');

create table usuarios.credencial_local
(
    id_usuario          int         not null,
    senha_hash          text        not null,
    senha_atualizada_em timestamptz not null default now(),
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now(),
    constraint pk_credencial_local primary key (id_usuario),
    constraint fk_credencial_local_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade
);

create table usuarios.credencial_social
(
    id_usuario            int                      not null,
    provedor              usuarios.provedor_social not null,
    identificador_externo text                     not null,
    created_at            timestamptz              not null default now(),
    updated_at            timestamptz              not null default now(),
    constraint pk_credencial_social primary key (id_usuario, provedor),
    constraint fk_credencial_social_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade
);

create table usuarios.dispositivo
(
    id_dispositivo   int generated always as identity,
    id_usuario       int                             not null,
    identificador    uuid                            not null,
    plataforma       usuarios.plataforma_dispositivo not null,
    fabricante       text                            not null,
    modelo           text                            not null,
    versao_so        text                            not null,
    ultimo_ip        inet                            not null,
    ultimo_acesso_em timestamptz                     not null default now(),
    created_at       timestamptz                     not null default now(),
    updated_at       timestamptz                     not null default now(),
    constraint pk_dispositivo primary key (id_dispositivo),
    constraint fk_dispositivo_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint uq_dispositivo_usuario_identificador unique (id_usuario, identificador),
    constraint ck_dispositivo_fabricante check (length(btrim(fabricante)) between 1 and 100),
    constraint ck_dispositivo_modelo check (length(btrim(modelo)) between 1 and 100),
    constraint ck_dispositivo_versao_so check (length(btrim(versao_so)) between 1 and 50)
);
create table usuarios.sessao
(
    id_sessao      int generated always as identity,
    id_dispositivo int         not null,
    expira_em      timestamptz not null,
    revogada_em    timestamptz,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now(),
    constraint pk_sessao primary key (id_sessao),
    constraint fk_sessao_dispositivo foreign key (id_dispositivo) references usuarios.dispositivo (id_dispositivo) on delete cascade
);

create index ix_sessao_dispositivo on usuarios.sessao (id_dispositivo) where revogada_em is null;
create index ix_sessao_expira_em on usuarios.sessao (expira_em);
create index ix_sessao_revogada_em on usuarios.sessao (revogada_em) where revogada_em is not null;

create table usuarios.refresh_token
(
    id_refresh_token bigint generated always as identity,
    id_sessao        int         not null,
    token_hash       text        not null,
    usado_em         timestamptz,
    created_at       timestamptz not null default now(),
    constraint pk_refresh_token primary key (id_refresh_token),
    constraint fk_refresh_token_sessao foreign key (id_sessao) references usuarios.sessao (id_sessao) on delete cascade
);

create index ix_refresh_token_sessao on usuarios.refresh_token (id_sessao) where usado_em is null;
create index ix_refresh_token_usado_em on usuarios.refresh_token (usado_em) where usado_em is not null;

create type vinculos.tipo_vinculo as enum ('socio', 'representante', 'mei', 'funcionario');

create table vinculos.vinculo_pessoa_empresa
(
    id_vinculo_pessoa_empresa int generated always as identity,
    id_pessoa_fisica          int                   not null,
    id_pessoa_juridica        int                   not null,
    tipo                      vinculos.tipo_vinculo not null,
    created_at                timestamptz           not null default now(),
    updated_at                timestamptz           not null default now(),
    constraint pk_vinculo_pessoa_empresa primary key (id_vinculo_pessoa_empresa),
    constraint fk_vinculo_pessoa_fisica foreign key (id_pessoa_fisica) references pessoas.pessoa_fisica (id_pessoa) on delete cascade,
    constraint fk_vinculo_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa) on delete cascade
);

create index ix_vinculo_pessoa_juridica on vinculos.vinculo_pessoa_empresa (id_pessoa_juridica);

create table pessoas.pessoa_contato
(
    id_pessoa  int         not null,
    telefone   text        not null,
    email      text        not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint pk_pessoa_contato primary key (id_pessoa),
    constraint fk_pessoa_contato_pessoa foreign key (id_pessoa) references pessoas.pessoa (id_pessoa) on delete cascade
);

create table pessoas.pessoa_titularidade
(
    id_pessoa_titularidade int generated always as identity,
    id_pessoa              int         not null,
    id_usuario             int,
    id_pessoa_juridica     int,
    created_at             timestamptz not null default now(),
    updated_at             timestamptz not null default now(),
    constraint pk_pessoa_titularidade primary key (id_pessoa_titularidade),
    constraint fk_pessoa_titularidade_pessoa foreign key (id_pessoa) references pessoas.pessoa (id_pessoa) on delete cascade,
    constraint fk_pessoa_titularidade_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint fk_pessoa_titularidade_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa) on delete cascade
);

create unique index uq_pessoa_titularidade on pessoas.pessoa_titularidade (id_pessoa, coalesce(id_usuario, 0),
                                                                           coalesce(id_pessoa_juridica, 0));
create index ix_pessoa_titularidade_usuario on pessoas.pessoa_titularidade (id_usuario);
create index ix_pessoa_titularidade_pessoa_juridica on pessoas.pessoa_titularidade (id_pessoa_juridica);

create type imoveis.tipo_imovel as enum ('apartamento', 'casa', 'sala_comercial', 'galpao', 'outro');
create type imoveis.categoria_imovel as enum ('residencial', 'comercial', 'alto_padrao');

create table enderecos.estado
(
    id_estado  int generated always as identity,
    sigla      text        not null,
    nome       text        not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint pk_estado primary key (id_estado)
);

create table enderecos.cidade
(
    id_cidade  int generated always as identity,
    id_estado  int         not null,
    nome       text        not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint pk_cidade primary key (id_cidade),
    constraint fk_cidade_estado foreign key (id_estado) references enderecos.estado (id_estado)
);

create unique index uq_cidade_nome on enderecos.cidade (id_estado, lower(nome));

create table enderecos.bairro
(
    id_bairro  int generated always as identity,
    id_cidade  int         not null,
    nome       text        not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint pk_bairro primary key (id_bairro),
    constraint fk_bairro_cidade foreign key (id_cidade) references enderecos.cidade (id_cidade)
);

create unique index uq_bairro_nome on enderecos.bairro (id_cidade, lower(nome));

create table enderecos.logradouro
(
    id_logradouro int generated always as identity,
    id_bairro     int         not null,
    cep           text        not null,
    nome          text        not null,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    constraint pk_logradouro primary key (id_logradouro),
    constraint fk_logradouro_bairro foreign key (id_bairro) references enderecos.bairro (id_bairro)
);

create unique index uq_logradouro on enderecos.logradouro (id_bairro, cep, lower(nome));

create table enderecos.endereco
(
    id_endereco   int generated always as identity,
    id_logradouro int         not null,
    numero        text        not null,
    complemento   text,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    constraint pk_endereco primary key (id_endereco),
    constraint fk_endereco_logradouro foreign key (id_logradouro) references enderecos.logradouro (id_logradouro)
);

create unique index uq_endereco on enderecos.endereco (id_logradouro, numero, coalesce(complemento, ''));

create table imoveis.imovel
(
    id_imovel          int generated always as identity,
    uuid               uuid                     not null default gen_random_uuid(),
    id_endereco        int                      not null,
    id_usuario         int,
    id_pessoa_juridica int,
    descricao          text,
    tipo               imoveis.tipo_imovel      not null,
    categoria          imoveis.categoria_imovel not null,
    created_at         timestamptz              not null default now(),
    updated_at         timestamptz              not null default now(),
    constraint pk_imovel primary key (id_imovel),
    constraint fk_imovel_endereco foreign key (id_endereco) references enderecos.endereco (id_endereco),
    constraint fk_imovel_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario),
    constraint fk_imovel_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa)
);

create unique index uq_imovel_endereco_titular on imoveis.imovel (id_endereco, coalesce(id_usuario, 0),
                                                                  coalesce(id_pessoa_juridica, 0));
create index ix_imovel_usuario on imoveis.imovel (id_usuario);
create index ix_imovel_pessoa_juridica on imoveis.imovel (id_pessoa_juridica);

create table imoveis.imovel_proprietario
(
    id_imovel  int         not null,
    id_pessoa  int         not null,
    created_at timestamptz not null default now(),
    constraint pk_imovel_proprietario primary key (id_imovel, id_pessoa),
    constraint fk_imovel_proprietario_imovel foreign key (id_imovel) references imoveis.imovel (id_imovel) on delete cascade,
    constraint fk_imovel_proprietario_pessoa foreign key (id_pessoa) references pessoas.pessoa (id_pessoa) on delete restrict
);

create index ix_imovel_proprietario_pessoa on imoveis.imovel_proprietario (id_pessoa);

create table catalogo.tipo_ambiente
(
    id_tipo_ambiente   int generated always as identity,
    id_pessoa_juridica int,
    id_usuario         int,
    nome               text        not null,
    padrao             boolean     not null default false,
    created_at         timestamptz not null default now(),
    updated_at         timestamptz not null default now(),
    constraint pk_tipo_ambiente primary key (id_tipo_ambiente),
    constraint fk_tipo_ambiente_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa) on delete cascade,
    constraint fk_tipo_ambiente_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade
);

create unique index uq_tipo_ambiente_nome on catalogo.tipo_ambiente (coalesce(id_pessoa_juridica, 0),
                                                                     coalesce(id_usuario, 0), lower(nome));
create index ix_tipo_ambiente_pessoa_juridica on catalogo.tipo_ambiente (id_pessoa_juridica);
create index ix_tipo_ambiente_usuario on catalogo.tipo_ambiente (id_usuario);

create table catalogo.tipo_item
(
    id_tipo_item       int generated always as identity,
    id_pessoa_juridica int,
    id_usuario         int,
    nome               text        not null,
    created_at         timestamptz not null default now(),
    updated_at         timestamptz not null default now(),
    constraint pk_tipo_item primary key (id_tipo_item),
    constraint fk_tipo_item_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa) on delete cascade,
    constraint fk_tipo_item_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade
);

create unique index uq_tipo_item_nome on catalogo.tipo_item (coalesce(id_pessoa_juridica, 0), coalesce(id_usuario, 0),
                                                             lower(nome));
create index ix_tipo_item_pessoa_juridica on catalogo.tipo_item (id_pessoa_juridica);
create index ix_tipo_item_usuario on catalogo.tipo_item (id_usuario);

create table catalogo.tipo_ambiente_item
(
    id_tipo_ambiente int         not null,
    id_tipo_item     int         not null,
    created_at       timestamptz not null default now(),
    constraint pk_tipo_ambiente_item primary key (id_tipo_ambiente, id_tipo_item),
    constraint fk_tipo_ambiente_item_tipo_ambiente foreign key (id_tipo_ambiente) references catalogo.tipo_ambiente (id_tipo_ambiente) on delete cascade,
    constraint fk_tipo_ambiente_item_tipo_item foreign key (id_tipo_item) references catalogo.tipo_item (id_tipo_item) on delete cascade
);

create index ix_tipo_ambiente_item_tipo_item on catalogo.tipo_ambiente_item (id_tipo_item);

create type vistorias.tipo_vistoria as enum ('entrada', 'saida');
create type vistorias.status_vistoria as enum ('em_andamento', 'finalizada');
create type vistorias.estado_item as enum ('novo', 'excelente', 'bom', 'regular', 'ruim', 'danificado');
create type vistorias.tipo_vinculo_pessoa as enum ('proprietario', 'inquilino');

create table vistorias.vistoria
(
    id_vistoria            int generated always as identity,
    uuid                   uuid                      not null default gen_random_uuid(),
    id_usuario_responsavel int                       not null,
    id_pessoa_juridica     int,
    id_imovel              int                       not null,
    tipo                   vistorias.tipo_vistoria   not null,
    status                 vistorias.status_vistoria not null default 'em_andamento',
    id_vistoria_entrada    int,
    observacoes_iniciais   text,
    iniciada_em            timestamptz               not null default now(),
    finalizada_em          timestamptz,
    created_at             timestamptz               not null default now(),
    updated_at             timestamptz               not null default now(),
    constraint pk_vistoria primary key (id_vistoria),
    constraint uq_vistoria_id_imovel unique (id_vistoria, id_imovel),
    constraint fk_vistoria_usuario foreign key (id_usuario_responsavel) references usuarios.usuario (id_usuario),
    constraint fk_vistoria_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa),
    constraint fk_vistoria_imovel foreign key (id_imovel) references imoveis.imovel (id_imovel),
    constraint fk_vistoria_entrada foreign key (id_vistoria_entrada, id_imovel) references vistorias.vistoria (id_vistoria, id_imovel)
);

create index ix_vistoria_usuario_status on vistorias.vistoria (id_usuario_responsavel, status);
create index ix_vistoria_pessoa_juridica_status on vistorias.vistoria (id_pessoa_juridica, status);
create index ix_vistoria_imovel on vistorias.vistoria (id_imovel, iniciada_em desc);
create index ix_vistoria_entrada on vistorias.vistoria (id_vistoria_entrada);

create table vistorias.vistoria_pessoa
(
    id_vistoria  int                           not null,
    id_pessoa    int                           not null,
    tipo_vinculo vistorias.tipo_vinculo_pessoa not null,
    created_at   timestamptz                   not null default now(),
    updated_at   timestamptz                   not null default now(),
    constraint pk_vistoria_pessoa primary key (id_vistoria, id_pessoa),
    constraint fk_vistoria_pessoa_vistoria foreign key (id_vistoria) references vistorias.vistoria (id_vistoria) on delete cascade,
    constraint fk_vistoria_pessoa_pessoa foreign key (id_pessoa) references pessoas.pessoa (id_pessoa) on delete restrict
);

create index ix_vistoria_pessoa_pessoa on vistorias.vistoria_pessoa (id_pessoa);

create table vistorias.ambiente
(
    id_ambiente        int generated always as identity,
    uuid               uuid        not null default gen_random_uuid(),
    id_vistoria        int         not null,
    id_tipo_ambiente   int,
    nome               text        not null,
    observacoes        text,
    id_ambiente_origem int,
    created_at         timestamptz not null default now(),
    updated_at         timestamptz not null default now(),
    constraint pk_ambiente primary key (id_ambiente),
    constraint fk_ambiente_vistoria foreign key (id_vistoria) references vistorias.vistoria (id_vistoria) on delete cascade,
    constraint fk_ambiente_tipo_ambiente foreign key (id_tipo_ambiente) references catalogo.tipo_ambiente (id_tipo_ambiente),
    constraint fk_ambiente_origem foreign key (id_ambiente_origem) references vistorias.ambiente (id_ambiente) on delete set null
);

create unique index uq_ambiente_nome on vistorias.ambiente (id_vistoria, lower(nome));
create index ix_ambiente_tipo_ambiente on vistorias.ambiente (id_tipo_ambiente);
create index ix_ambiente_origem on vistorias.ambiente (id_ambiente_origem);

create table vistorias.item
(
    id_item        int generated always as identity,
    uuid           uuid        not null default gen_random_uuid(),
    id_ambiente    int         not null,
    id_tipo_item   int,
    nome           text        not null,
    descricao      text,
    estado         vistorias.estado_item,
    material       text,
    observacoes    text,
    id_item_origem int,
    created_at     timestamptz not null default now(),
    updated_at     timestamptz not null default now(),
    constraint pk_item primary key (id_item),
    constraint uq_item_id_ambiente unique (id_item, id_ambiente),
    constraint fk_item_ambiente foreign key (id_ambiente) references vistorias.ambiente (id_ambiente) on delete cascade,
    constraint fk_item_tipo_item foreign key (id_tipo_item) references catalogo.tipo_item (id_tipo_item),
    constraint fk_item_origem foreign key (id_item_origem) references vistorias.item (id_item) on delete set null
);

create unique index uq_item_nome on vistorias.item (id_ambiente, lower(nome));
create index ix_item_tipo_item on vistorias.item (id_tipo_item);
create index ix_item_origem on vistorias.item (id_item_origem);

create table vistorias.evidencia
(
    id_evidencia    int generated always as identity,
    uuid            uuid        not null default gen_random_uuid(),
    id_ambiente     int         not null,
    id_item         int,
    caminho_arquivo text        not null,
    tipo_arquivo    text        not null,
    tamanho_bytes   int         not null,
    recebida_em     timestamptz,
    descricao       text,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    constraint pk_evidencia primary key (id_evidencia),
    constraint fk_evidencia_ambiente foreign key (id_ambiente) references vistorias.ambiente (id_ambiente) on delete cascade,
    constraint fk_evidencia_item foreign key (id_item, id_ambiente) references vistorias.item (id_item, id_ambiente) on delete cascade
);

create index ix_evidencia_ambiente on vistorias.evidencia (id_ambiente);
create index ix_evidencia_item on vistorias.evidencia (id_item);

alter table enderecos.estado
    add constraint uq_estado_sigla unique (sigla);

alter table pessoas.pessoa_fisica
    add constraint uq_pessoa_fisica_cpf unique (cpf);

alter table pessoas.pessoa_juridica
    add constraint uq_pessoa_juridica_cnpj unique (cnpj);

alter table usuarios.usuario
    add constraint uq_usuario_pessoa unique (id_pessoa),
    add constraint uq_usuario_email unique (email);

alter table usuarios.credencial_social
    add constraint uq_credencial_social_identidade unique (provedor, identificador_externo);

alter table usuarios.dispositivo
    add constraint uq_dispositivo_usuario_identificador unique (id_usuario, identificador);

alter table usuarios.refresh_token
    add constraint uq_refresh_token_hash unique (token_hash);

alter table vinculos.vinculo_pessoa_empresa
    add constraint uq_vinculo unique (id_pessoa_fisica, id_pessoa_juridica);

alter table imoveis.imovel
    add constraint uq_imovel_uuid unique (uuid);

alter table vistorias.vistoria
    add constraint uq_vistoria_uuid unique (uuid);

alter table vistorias.ambiente
    add constraint uq_ambiente_uuid unique (uuid);

alter table vistorias.item
    add constraint uq_item_uuid unique (uuid);

alter table vistorias.evidencia
    add constraint uq_evidencia_uuid unique (uuid),
    add constraint uq_evidencia_caminho_arquivo unique (caminho_arquivo);

alter table pessoas.pessoa
    add constraint ck_pessoa_nome check (nome = btrim(nome) and length(nome) between 1 and 200);

alter table pessoas.pessoa_fisica
    add constraint ck_pessoa_fisica_tipo check (tipo = 'pf'),
    add constraint ck_pessoa_fisica_cpf check (documento.cpf_valido(cpf));

alter table pessoas.pessoa_juridica
    add constraint ck_pessoa_juridica_tipo check (tipo = 'pj'),
    add constraint ck_pessoa_juridica_cnpj check (documento.cnpj_valido(cnpj)),
    add constraint ck_pessoa_juridica_fantasia check (nome_fantasia = btrim(nome_fantasia) and
                                                      length(nome_fantasia) between 1 and 200);

alter table usuarios.usuario
    add constraint ck_usuario_email check (usuarios.email_valido(email));

alter table usuarios.credencial_local
    add constraint ck_credencial_local_hash check (senha_hash ~ '^\$(2[aby]|argon2(i|d|id))\$');

alter table usuarios.dispositivo
    add constraint ck_dispositivo_fabricante check (fabricante is null or documento.texto_valido(fabricante, 1, 64)),
    add constraint ck_dispositivo_modelo check (modelo is null or documento.texto_valido(modelo, 1, 64)),
    add constraint ck_dispositivo_versao_so check (versao_so is null or documento.texto_valido(versao_so, 1, 16));

alter table usuarios.sessao
    add constraint ck_sessao_expira_em check (expira_em > created_at),
    add constraint ck_sessao_revogada_em check (revogada_em is null or revogada_em >= created_at);

alter table usuarios.refresh_token
    add constraint ck_refresh_token_hash check (token_hash ~ '^[0-9a-f]{64}$'),
    add constraint ck_refresh_token_usado_em check (usado_em is null or usado_em >= created_at);

alter table pessoas.pessoa_contato
    add constraint ck_pessoa_contato_telefone check (documento.telefone_valido(telefone)),
    add constraint ck_pessoa_contato_email check (usuarios.email_valido(email));

alter table pessoas.pessoa_titularidade
    add constraint ck_pessoa_titularidade_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) = 1);

alter table enderecos.estado
    add constraint ck_estado_sigla check (documento.uf_valida(sigla)),
    add constraint ck_estado_nome check (documento.texto_valido(nome, 1, 50));

alter table enderecos.cidade
    add constraint ck_cidade_nome check (documento.texto_valido(nome, 1, 100));

alter table enderecos.bairro
    add constraint ck_bairro_nome check (documento.texto_valido(nome, 1, 100));

alter table enderecos.logradouro
    add constraint ck_logradouro_cep check (documento.cep_valido(cep)),
    add constraint ck_logradouro_nome check (documento.texto_valido(nome, 1, 200));

alter table enderecos.endereco
    add constraint ck_endereco_numero check (documento.texto_valido(numero, 1, 20)),
    add constraint ck_endereco_complemento check (complemento is null or documento.texto_valido(complemento, 1, 100));

alter table imoveis.imovel
    add constraint ck_imovel_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) = 1),
    add constraint ck_imovel_descricao check (descricao is null or documento.texto_valido(descricao, 1, 200));

alter table catalogo.tipo_ambiente
    add constraint ck_tipo_ambiente_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) <= 1),
    add constraint ck_tipo_ambiente_nome check (documento.texto_valido(nome, 1, 100));

alter table catalogo.tipo_item
    add constraint ck_tipo_item_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) <= 1),
    add constraint ck_tipo_item_nome check (documento.texto_valido(nome, 1, 100));

alter table vistorias.vistoria
    add constraint ck_vistoria_observacoes check (observacoes_iniciais is null or
                                                  documento.texto_valido(observacoes_iniciais, 1, 2000)),
    add constraint ck_vistoria_entrada check (id_vistoria_entrada is null or tipo = 'saida'),
    add constraint ck_vistoria_finalizada check ((status = 'finalizada') = (finalizada_em is not null)),
    add constraint ck_vistoria_datas check (finalizada_em is null or finalizada_em >= iniciada_em);

alter table vistorias.ambiente
    add constraint ck_ambiente_nome check (documento.texto_valido(nome, 1, 100)),
    add constraint ck_ambiente_observacoes check (observacoes is null or documento.texto_valido(observacoes, 1, 2000)),
    add constraint ck_ambiente_origem check (id_ambiente_origem is null or id_ambiente_origem <> id_ambiente);

alter table vistorias.item
    add constraint ck_item_nome check (documento.texto_valido(nome, 1, 100)),
    add constraint ck_item_descricao check (descricao is null or documento.texto_valido(descricao, 1, 500)),
    add constraint ck_item_material check (material is null or documento.texto_valido(material, 1, 100)),
    add constraint ck_item_observacoes check (observacoes is null or documento.texto_valido(observacoes, 1, 2000)),
    add constraint ck_item_origem check (id_item_origem is null or id_item_origem <> id_item);

alter table vistorias.evidencia
    add constraint ck_evidencia_caminho_arquivo check (documento.texto_valido(caminho_arquivo, 1, 500)),
    add constraint ck_evidencia_tipo_arquivo check (tipo_arquivo in ('image/jpeg', 'image/png')),
    add constraint ck_evidencia_tamanho check (tamanho_bytes between 1 and 52428800),
    add constraint ck_evidencia_descricao check (descricao is null or documento.texto_valido(descricao, 1, 500));

create trigger tg_pessoa_updated_at
    before update
    on pessoas.pessoa
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_fisica_updated_at
    before update
    on pessoas.pessoa_fisica
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_juridica_updated_at
    before update
    on pessoas.pessoa_juridica
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_usuario_updated_at
    before update
    on usuarios.usuario
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_credencial_local_updated_at
    before update
    on usuarios.credencial_local
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_credencial_social_updated_at
    before update
    on usuarios.credencial_social
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_dispositivo_updated_at
    before update
    on usuarios.dispositivo
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_sessao_updated_at
    before update
    on usuarios.sessao
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_vinculo_updated_at
    before update
    on vinculos.vinculo_pessoa_empresa
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_contato_updated_at
    before update
    on pessoas.pessoa_contato
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_titularidade_updated_at
    before update
    on pessoas.pessoa_titularidade
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_estado_updated_at
    before update
    on enderecos.estado
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_cidade_updated_at
    before update
    on enderecos.cidade
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_bairro_updated_at
    before update
    on enderecos.bairro
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_logradouro_updated_at
    before update
    on enderecos.logradouro
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_endereco_updated_at
    before update
    on enderecos.endereco
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_imovel_updated_at
    before update
    on imoveis.imovel
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_tipo_ambiente_updated_at
    before update
    on catalogo.tipo_ambiente
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_tipo_item_updated_at
    before update
    on catalogo.tipo_item
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_vistoria_updated_at
    before update
    on vistorias.vistoria
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_vistoria_pessoa_updated_at
    before update
    on vistorias.vistoria_pessoa
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_ambiente_updated_at
    before update
    on vistorias.ambiente
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_item_updated_at
    before update
    on vistorias.item
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_evidencia_updated_at
    before update
    on vistorias.evidencia
    for each row
    when (old is distinct from new)
execute function public.tg_set_updated_at();

insert into enderecos.estado (sigla, nome)
values ('AC', 'Acre'),
       ('AL', 'Alagoas'),
       ('AP', 'Amapá'),
       ('AM', 'Amazonas'),
       ('BA', 'Bahia'),
       ('CE', 'Ceará'),
       ('DF', 'Distrito Federal'),
       ('ES', 'Espírito Santo'),
       ('GO', 'Goiás'),
       ('MA', 'Maranhão'),
       ('MT', 'Mato Grosso'),
       ('MS', 'Mato Grosso do Sul'),
       ('MG', 'Minas Gerais'),
       ('PA', 'Pará'),
       ('PB', 'Paraíba'),
       ('PR', 'Paraná'),
       ('PE', 'Pernambuco'),
       ('PI', 'Piauí'),
       ('RJ', 'Rio de Janeiro'),
       ('RN', 'Rio Grande do Norte'),
       ('RS', 'Rio Grande do Sul'),
       ('RO', 'Rondônia'),
       ('RR', 'Roraima'),
       ('SC', 'Santa Catarina'),
       ('SP', 'São Paulo'),
       ('SE', 'Sergipe'),
       ('TO', 'Tocantins');

insert into catalogo.tipo_ambiente (nome, padrao)
values ('Sala', true),
       ('Cozinha', true),
       ('Quarto', true),
       ('Banheiro', true),
       ('Área de serviço', true);

insert into catalogo.tipo_item (nome)
values ('Piso'),
       ('Rodapé'),
       ('Paredes'),
       ('Pintura'),
       ('Teto'),
       ('Portas'),
       ('Fechaduras'),
       ('Janelas'),
       ('Vidros'),
       ('Tomadas'),
       ('Interruptores'),
       ('Iluminação'),
       ('Revestimento'),
       ('Armários'),
       ('Bancada'),
       ('Pia'),
       ('Torneira'),
       ('Ralo'),
       ('Box'),
       ('Vaso sanitário'),
       ('Chuveiro'),
       ('Espelho'),
       ('Registro'),
       ('Tanque');

insert into catalogo.tipo_ambiente_item (id_tipo_ambiente, id_tipo_item)
select ta.id_tipo_ambiente, ti.id_tipo_item
from catalogo.tipo_ambiente ta
         cross join catalogo.tipo_item ti
where ta.nome = 'Sala'
  and ti.nome in ('Piso', 'Rodapé', 'Paredes', 'Pintura', 'Teto', 'Portas', 'Fechaduras', 'Janelas',
                  'Vidros', 'Tomadas', 'Interruptores', 'Iluminação');

insert into catalogo.tipo_ambiente_item (id_tipo_ambiente, id_tipo_item)
select ta.id_tipo_ambiente, ti.id_tipo_item
from catalogo.tipo_ambiente ta
         cross join catalogo.tipo_item ti
where ta.nome = 'Cozinha'
  and ti.nome in ('Piso', 'Paredes', 'Revestimento', 'Teto', 'Portas', 'Janelas', 'Vidros', 'Tomadas',
                  'Interruptores', 'Iluminação', 'Armários', 'Bancada', 'Pia', 'Torneira', 'Ralo');

insert into catalogo.tipo_ambiente_item (id_tipo_ambiente, id_tipo_item)
select ta.id_tipo_ambiente, ti.id_tipo_item
from catalogo.tipo_ambiente ta
         cross join catalogo.tipo_item ti
where ta.nome = 'Quarto'
  and ti.nome in ('Piso', 'Rodapé', 'Paredes', 'Pintura', 'Teto', 'Portas', 'Fechaduras', 'Janelas',
                  'Vidros', 'Tomadas', 'Interruptores', 'Iluminação', 'Armários');

insert into catalogo.tipo_ambiente_item (id_tipo_ambiente, id_tipo_item)
select ta.id_tipo_ambiente, ti.id_tipo_item
from catalogo.tipo_ambiente ta
         cross join catalogo.tipo_item ti
where ta.nome = 'Banheiro'
  and ti.nome in ('Piso', 'Revestimento', 'Teto', 'Portas', 'Fechaduras', 'Janelas', 'Vidros', 'Tomadas',
                  'Interruptores', 'Iluminação', 'Bancada', 'Pia', 'Torneira', 'Box', 'Vaso sanitário',
                  'Chuveiro', 'Espelho', 'Ralo', 'Registro');

insert into catalogo.tipo_ambiente_item (id_tipo_ambiente, id_tipo_item)
select ta.id_tipo_ambiente, ti.id_tipo_item
from catalogo.tipo_ambiente ta
         cross join catalogo.tipo_item ti
where ta.nome = 'Área de serviço'
  and ti.nome in ('Piso', 'Revestimento', 'Teto', 'Portas', 'Janelas', 'Tomadas', 'Iluminação', 'Tanque',
                  'Torneira', 'Ralo', 'Registro');