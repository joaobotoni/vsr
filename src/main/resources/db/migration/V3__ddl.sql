create type pessoas.tipo_pessoa as enum ('pf', 'pj');

create table pessoas.pessoa
(
    id_pessoa  int generated always as identity,
    tipo       pessoas.tipo_pessoa not null,
    nome       text                not null,
    created_at timestamptz         not null default now(),
    updated_at timestamptz         not null default now(),
    constraint pk_pessoa primary key (id_pessoa),
    constraint uq_pessoa_id_tipo unique (id_pessoa, tipo),
    constraint ck_pessoa_nome check (documento.texto_valido(nome, 1, 200))
);

create table pessoas.pessoa_fisica
(
    id_pessoa  int                 not null,
    tipo       pessoas.tipo_pessoa not null default 'pf',
    cpf        text                not null,
    created_at timestamptz         not null default now(),
    updated_at timestamptz         not null default now(),
    constraint pk_pessoa_fisica primary key (id_pessoa),
    constraint fk_pessoa_fisica_pessoa foreign key (id_pessoa, tipo) references pessoas.pessoa (id_pessoa, tipo) on delete cascade,
    constraint uq_pessoa_fisica_cpf unique (cpf),
    constraint ck_pessoa_fisica_tipo check (tipo = 'pf'),
    constraint ck_pessoa_fisica_cpf check (documento.cpf_valido(cpf))
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
    constraint fk_pessoa_juridica_pessoa foreign key (id_pessoa, tipo) references pessoas.pessoa (id_pessoa, tipo) on delete cascade,
    constraint uq_pessoa_juridica_cnpj unique (cnpj),
    constraint ck_pessoa_juridica_tipo check (tipo = 'pj'),
    constraint ck_pessoa_juridica_cnpj check (documento.cnpj_valido(cnpj)),
    constraint ck_pessoa_juridica_fantasia check (documento.texto_valido(nome_fantasia, 1, 200))
);

create type usuarios.provedor_social as enum ('google');
create type usuarios.plataforma_dispositivo as enum ('android', 'ios');

create table usuarios.usuario
(
    id_usuario int generated always as identity,
    uuid       uuid        not null default gen_random_uuid(),
    id_pessoa  int         not null,
    email      text        not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint pk_usuario primary key (id_usuario),
    constraint uq_usuario_uuid unique (uuid),
    constraint fk_usuario_pessoa foreign key (id_pessoa) references pessoas.pessoa_fisica (id_pessoa) on delete cascade,
    constraint uq_usuario_pessoa unique (id_pessoa),
    constraint uq_usuario_email unique (email),
    constraint ck_usuario_email check (documento.email_valido(email))
);

create table usuarios.credencial_local
(
    id_usuario          int         not null,
    senha_hash          text        not null,
    senha_atualizada_em timestamptz not null default now(),
    created_at          timestamptz not null default now(),
    updated_at          timestamptz not null default now(),
    constraint pk_credencial_local primary key (id_usuario),
    constraint fk_credencial_local_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint ck_credencial_local_hash check (senha_hash ~ '^\$(2[aby]|argon2(i|d|id))\$')
);

create table usuarios.credencial_social
(
    id_usuario            int                      not null,
    provedor              usuarios.provedor_social not null,
    identificador_externo text                     not null,
    created_at            timestamptz              not null default now(),
    updated_at            timestamptz              not null default now(),
    constraint pk_credencial_social primary key (id_usuario, provedor),
    constraint fk_credencial_social_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint uq_credencial_social_identidade unique (provedor, identificador_externo)
);

create table usuarios.dispositivo
(
    id_dispositivo int generated always as identity,
    id_usuario     int                             not null,
    identificador  uuid                            not null,
    plataforma     usuarios.plataforma_dispositivo not null,
    fabricante     text                            not null,
    modelo         text                            not null,
    versao_so      text                            not null,
    created_at     timestamptz                     not null default now(),
    updated_at     timestamptz                     not null default now(),
    constraint pk_dispositivo primary key (id_dispositivo),
    constraint fk_dispositivo_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint uq_dispositivo_usuario_identificador unique (id_usuario, identificador),
    constraint ck_dispositivo_fabricante check (documento.texto_valido(fabricante, 1, 64)),
    constraint ck_dispositivo_modelo check (documento.texto_valido(modelo, 1, 64)),
    constraint ck_dispositivo_versao_so check (documento.texto_valido(versao_so, 1, 16))
);

create table usuarios.sessao
(
    id_sessao        int generated always as identity,
    id_dispositivo   int         not null,
    endereco_ip      inet        not null,
    ultimo_acesso_em timestamptz not null default now(),
    expira_em        timestamptz not null,
    revogada_em      timestamptz,
    created_at       timestamptz not null default now(),
    updated_at       timestamptz not null default now(),
    constraint pk_sessao primary key (id_sessao),
    constraint fk_sessao_dispositivo foreign key (id_dispositivo) references usuarios.dispositivo (id_dispositivo) on delete cascade,
    constraint ck_sessao_expira_em check (expira_em > created_at),
    constraint ck_sessao_revogada_em check (revogada_em is null or revogada_em >= created_at),
    constraint ck_sessao_ultimo_acesso_em check (ultimo_acesso_em >= created_at)
);

create index ix_sessao_dispositivo on usuarios.sessao (id_dispositivo) where revogada_em is null;
create index ix_sessao_expira_em on usuarios.sessao (expira_em);

create view usuarios.sessao_ativa as
select s.*
from usuarios.sessao s
where s.revogada_em is null
  and s.expira_em > now();

create table usuarios.refresh_token
(
    id_sessao     int         not null,
    hash_atual    bytea       not null,
    expira_em     timestamptz not null,
    renovado_em   timestamptz,
    created_at    timestamptz not null default now(),
    updated_at    timestamptz not null default now(),
    constraint pk_refresh_token primary key (id_sessao),
    constraint fk_refresh_token_sessao foreign key (id_sessao) references usuarios.sessao (id_sessao) on delete cascade,
    constraint uq_refresh_token_hash_atual unique (hash_atual),
    constraint ck_refresh_token_hash_atual check (length(hash_atual) = 32),
    constraint ck_refresh_token_expira_em check (expira_em > created_at),
    constraint ck_refresh_token_renovado_em check (renovado_em is null or renovado_em >= created_at)
);

create table usuarios.refresh_token_usado
(
    hash      bytea       not null,
    id_sessao int         not null,
    usado_em  timestamptz not null default now(),
    constraint pk_refresh_token_usado primary key (hash),
    constraint fk_refresh_token_usado_refresh_token foreign key (id_sessao) references usuarios.refresh_token (id_sessao) on delete cascade,
    constraint ck_refresh_token_usado_hash check (length(hash) = 32)
);

create index ix_refresh_token_usado_sessao on usuarios.refresh_token_usado (id_sessao);

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
    constraint fk_vinculo_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa) on delete cascade,
    constraint uq_vinculo unique (id_pessoa_fisica, id_pessoa_juridica)
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
    constraint fk_pessoa_contato_pessoa foreign key (id_pessoa) references pessoas.pessoa (id_pessoa) on delete cascade,
    constraint ck_pessoa_contato_telefone check (documento.telefone_valido(telefone)),
    constraint ck_pessoa_contato_email check (documento.email_valido(email))
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
    constraint fk_pessoa_titularidade_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa) on delete cascade,
    constraint ck_pessoa_titularidade_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) = 1)
);

create unique index uq_pessoa_titularidade on pessoas.pessoa_titularidade (id_pessoa, coalesce(id_usuario, 0), coalesce(id_pessoa_juridica, 0));
create index ix_pessoa_titularidade_usuario on pessoas.pessoa_titularidade (id_usuario);
create index ix_pessoa_titularidade_pessoa_juridica on pessoas.pessoa_titularidade (id_pessoa_juridica);

create table enderecos.estado
(
    id_estado  int generated always as identity,
    sigla      text        not null,
    nome       text        not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint pk_estado primary key (id_estado),
    constraint uq_estado_sigla unique (sigla),
    constraint ck_estado_sigla check (documento.uf_valida(sigla)),
    constraint ck_estado_nome check (documento.texto_valido(nome, 1, 50))
);

create table enderecos.cidade
(
    id_cidade  int generated always as identity,
    id_estado  int         not null,
    nome       text        not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint pk_cidade primary key (id_cidade),
    constraint fk_cidade_estado foreign key (id_estado) references enderecos.estado (id_estado),
    constraint ck_cidade_nome check (documento.texto_valido(nome, 1, 100))
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
    constraint fk_bairro_cidade foreign key (id_cidade) references enderecos.cidade (id_cidade),
    constraint ck_bairro_nome check (documento.texto_valido(nome, 1, 100))
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
    constraint fk_logradouro_bairro foreign key (id_bairro) references enderecos.bairro (id_bairro),
    constraint ck_logradouro_cep check (documento.cep_valido(cep)),
    constraint ck_logradouro_nome check (documento.texto_valido(nome, 1, 200))
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
    constraint fk_endereco_logradouro foreign key (id_logradouro) references enderecos.logradouro (id_logradouro),
    constraint ck_endereco_numero check (documento.texto_valido(numero, 1, 20)),
    constraint ck_endereco_complemento check (complemento is null or documento.texto_valido(complemento, 1, 100))
);

create unique index uq_endereco on enderecos.endereco (id_logradouro, numero, coalesce(complemento, ''));

create type imoveis.tipo_imovel as enum ('apartamento', 'casa', 'sala_comercial', 'galpao', 'outro');
create type imoveis.categoria_imovel as enum ('residencial', 'comercial', 'alto_padrao');

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
    constraint fk_imovel_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa),
    constraint uq_imovel_uuid unique (uuid),
    constraint ck_imovel_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) = 1),
    constraint ck_imovel_descricao check (descricao is null or documento.texto_valido(descricao, 1, 200))
);

create unique index uq_imovel_endereco_titular on imoveis.imovel (id_endereco, coalesce(id_usuario, 0), coalesce(id_pessoa_juridica, 0));
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
    constraint fk_tipo_ambiente_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint ck_tipo_ambiente_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) <= 1),
    constraint ck_tipo_ambiente_nome check (documento.texto_valido(nome, 1, 100))
);

create unique index uq_tipo_ambiente_nome on catalogo.tipo_ambiente (coalesce(id_pessoa_juridica, 0), coalesce(id_usuario, 0), lower(nome));
create index ix_tipo_ambiente_pessoa_juridica on catalogo.tipo_ambiente (id_pessoa_juridica);
create index ix_tipo_ambiente_usuario on catalogo.tipo_ambiente (id_usuario);

create table catalogo.tipo_item
(
    id_tipo_item       int generated always as identity,
    id_pessoa_juridica int,
    id_usuario         int,
    nome               text        not null,
    padrao             boolean     not null default false,
    created_at         timestamptz not null default now(),
    updated_at         timestamptz not null default now(),
    constraint pk_tipo_item primary key (id_tipo_item),
    constraint fk_tipo_item_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa) on delete cascade,
    constraint fk_tipo_item_usuario foreign key (id_usuario) references usuarios.usuario (id_usuario) on delete cascade,
    constraint ck_tipo_item_titular check (num_nonnulls(id_usuario, id_pessoa_juridica) <= 1),
    constraint ck_tipo_item_nome check (documento.texto_valido(nome, 1, 100))
);

create unique index uq_tipo_item_nome on catalogo.tipo_item (coalesce(id_pessoa_juridica, 0), coalesce(id_usuario, 0), lower(nome));
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
    constraint uq_vistoria_uuid unique (uuid),
    constraint uq_vistoria_id_imovel unique (id_vistoria, id_imovel),
    constraint fk_vistoria_usuario foreign key (id_usuario_responsavel) references usuarios.usuario (id_usuario),
    constraint fk_vistoria_pessoa_juridica foreign key (id_pessoa_juridica) references pessoas.pessoa_juridica (id_pessoa),
    constraint fk_vistoria_imovel foreign key (id_imovel) references imoveis.imovel (id_imovel),
    constraint fk_vistoria_entrada foreign key (id_vistoria_entrada, id_imovel) references vistorias.vistoria (id_vistoria, id_imovel),
    constraint ck_vistoria_observacoes check (observacoes_iniciais is null or
                                              documento.texto_valido(observacoes_iniciais, 1, 2000)),
    constraint ck_vistoria_entrada check (id_vistoria_entrada is null or tipo = 'saida'),
    constraint ck_vistoria_finalizada check ((status = 'finalizada') = (finalizada_em is not null)),
    constraint ck_vistoria_datas check (finalizada_em is null or finalizada_em >= iniciada_em)
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
    constraint uq_ambiente_uuid unique (uuid),
    constraint fk_ambiente_vistoria foreign key (id_vistoria) references vistorias.vistoria (id_vistoria) on delete cascade,
    constraint fk_ambiente_tipo_ambiente foreign key (id_tipo_ambiente) references catalogo.tipo_ambiente (id_tipo_ambiente),
    constraint fk_ambiente_origem foreign key (id_ambiente_origem) references vistorias.ambiente (id_ambiente) on delete set null,
    constraint ck_ambiente_nome check (documento.texto_valido(nome, 1, 100)),
    constraint ck_ambiente_observacoes check (observacoes is null or documento.texto_valido(observacoes, 1, 2000)),
    constraint ck_ambiente_origem check (id_ambiente_origem is null or id_ambiente_origem <> id_ambiente)
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
    constraint uq_item_uuid unique (uuid),
    constraint uq_item_id_ambiente unique (id_item, id_ambiente),
    constraint fk_item_ambiente foreign key (id_ambiente) references vistorias.ambiente (id_ambiente) on delete cascade,
    constraint fk_item_tipo_item foreign key (id_tipo_item) references catalogo.tipo_item (id_tipo_item),
    constraint fk_item_origem foreign key (id_item_origem) references vistorias.item (id_item) on delete set null,
    constraint ck_item_nome check (documento.texto_valido(nome, 1, 100)),
    constraint ck_item_descricao check (descricao is null or documento.texto_valido(descricao, 1, 500)),
    constraint ck_item_material check (material is null or documento.texto_valido(material, 1, 100)),
    constraint ck_item_observacoes check (observacoes is null or documento.texto_valido(observacoes, 1, 2000)),
    constraint ck_item_origem check (id_item_origem is null or id_item_origem <> id_item)
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
    constraint uq_evidencia_uuid unique (uuid),
    constraint uq_evidencia_caminho_arquivo unique (caminho_arquivo),
    constraint fk_evidencia_ambiente foreign key (id_ambiente) references vistorias.ambiente (id_ambiente) on delete cascade,
    constraint fk_evidencia_item foreign key (id_item, id_ambiente) references vistorias.item (id_item, id_ambiente) on delete cascade,
    constraint ck_evidencia_caminho_arquivo check (documento.texto_valido(caminho_arquivo, 1, 500)),
    constraint ck_evidencia_tipo_arquivo check (tipo_arquivo in ('image/jpeg', 'image/png')),
    constraint ck_evidencia_tamanho check (tamanho_bytes between 1 and 52428800),
    constraint ck_evidencia_descricao check (descricao is null or documento.texto_valido(descricao, 1, 500))
);

create index ix_evidencia_ambiente on vistorias.evidencia (id_ambiente);
create index ix_evidencia_item on vistorias.evidencia (id_item);
