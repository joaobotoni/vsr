create trigger tg_pessoa_updated_at
    before update
    on pessoas.pessoa
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_fisica_updated_at
    before update
    on pessoas.pessoa_fisica
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_juridica_updated_at
    before update
    on pessoas.pessoa_juridica
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_usuario_updated_at
    before update
    on usuarios.usuario
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_credencial_local_updated_at
    before update
    on usuarios.credencial_local
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_credencial_social_updated_at
    before update
    on usuarios.credencial_social
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_dispositivo_updated_at
    before update
    on usuarios.dispositivo
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_sessao_updated_at
    before update
    on usuarios.sessao
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_refresh_token_updated_at
    before update
    on usuarios.refresh_token
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_vinculo_updated_at
    before update
    on vinculos.vinculo_pessoa_empresa
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_contato_updated_at
    before update
    on pessoas.pessoa_contato
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_pessoa_titularidade_updated_at
    before update
    on pessoas.pessoa_titularidade
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_estado_updated_at
    before update
    on enderecos.estado
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_cidade_updated_at
    before update
    on enderecos.cidade
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_bairro_updated_at
    before update
    on enderecos.bairro
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_logradouro_updated_at
    before update
    on enderecos.logradouro
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_endereco_updated_at
    before update
    on enderecos.endereco
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_imovel_updated_at
    before update
    on imoveis.imovel
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_tipo_ambiente_updated_at
    before update
    on catalogo.tipo_ambiente
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_tipo_item_updated_at
    before update
    on catalogo.tipo_item
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_vistoria_updated_at
    before update
    on vistorias.vistoria
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_vistoria_pessoa_updated_at
    before update
    on vistorias.vistoria_pessoa
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_ambiente_updated_at
    before update
    on vistorias.ambiente
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_item_updated_at
    before update
    on vistorias.item
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();

create trigger tg_evidencia_updated_at
    before update
    on vistorias.evidencia
    for each row when (old is distinct from new)
execute function public.tg_set_updated_at();
