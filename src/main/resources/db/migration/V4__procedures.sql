create procedure usuarios.registrar_dispositivo(p_id_usuario int, p_identificador uuid,
                                                p_plataforma text, p_fabricante text,
                                                p_modelo text, p_versao_so text)
    language sql as
$$
insert into usuarios.dispositivo (id_usuario, identificador, plataforma, fabricante, modelo, versao_so)
values (p_id_usuario, p_identificador, p_plataforma::usuarios.plataforma_dispositivo, p_fabricante, p_modelo, p_versao_so)
on conflict (id_usuario, identificador) do
update
    set plataforma = excluded.plataforma,
    fabricante = excluded.fabricante,
    modelo = excluded.modelo,
    versao_so = excluded.versao_so;
$$;

create procedure usuarios.registrar_acesso_sessao(p_id_sessao int)
    language plpgsql as
$$
begin
    perform
1
    from usuarios.sessao
    where id_sessao = p_id_sessao
      and revogada_em is null
      and expira_em > now();

    if
not found then
        raise exception 'violates check constraint "rn_sessao_ativa"'
            using errcode = 'check_violation', constraint = 'rn_sessao_ativa';
end if;

update usuarios.sessao
set ultimo_acesso_em = now()
where id_sessao = p_id_sessao
  and ultimo_acesso_em < now() - interval '5 minutes';
end;
$$;

create procedure usuarios.revogar_sessoes_usuario(p_id_usuario int, p_id_sessao_mantida int)
    language sql as
$$
update usuarios.sessao s
set revogada_em = now() from usuarios.dispositivo d
where d.id_dispositivo = s.id_dispositivo
  and d.id_usuario = p_id_usuario
  and s.id_sessao <> p_id_sessao_mantida
  and s.revogada_em is null
  and s.expira_em
    > now();
$$;

create procedure usuarios.revogar_sessoes_dispositivo(p_id_dispositivo int)
    language sql as
$$
update usuarios.sessao
set revogada_em = now()
where id_dispositivo = p_id_dispositivo
  and revogada_em is null
  and expira_em > now();
$$;

create procedure usuarios.revogar_sessao(p_id_sessao int)
    language sql as
$$
update usuarios.sessao
set revogada_em = now()
where id_sessao = p_id_sessao
  and revogada_em is null
  and expira_em > now();
$$;

create procedure usuarios.emitir_refresh_token(p_id_sessao int, p_hash bytea)
    language sql as
$$
insert into usuarios.refresh_token (id_sessao, hash_atual, expira_em)
select id_sessao, p_hash, expira_em
from usuarios.sessao
where id_sessao = p_id_sessao;
$$;

create procedure usuarios.renovar_refresh_token(p_id_sessao int, p_hash_atual bytea, p_hash_novo bytea)
    language plpgsql as
$$
begin
update usuarios.refresh_token
set hash_atual  = p_hash_novo,
    renovado_em = now()
where id_sessao = p_id_sessao
  and hash_atual = p_hash_atual;

if
not found then
        raise exception 'violates check constraint "rn_refresh_token_renovado"'
            using errcode = 'check_violation', constraint = 'rn_refresh_token_renovado';
end if;

insert into usuarios.refresh_token_usado (hash, id_sessao)
values (p_hash_atual, p_id_sessao);
end;
$$;

create procedure usuarios.trocar_senha(p_id_usuario int, p_hash_atual text, p_hash_novo text)
    language plpgsql as
$$
begin
update usuarios.credencial_local
set senha_hash          = p_hash_novo,
    senha_atualizada_em = now()
where id_usuario = p_id_usuario
  and senha_hash = p_hash_atual;

if
not found then
        raise exception 'violates check constraint "rn_senha_alterada"'
            using errcode = 'check_violation', constraint = 'rn_senha_alterada';
end if;
end;
$$;

create procedure rotinas.limpar_sessoes_expiradas()
    language sql as
$$
delete
from usuarios.sessao
where expira_em <= now();
$$;
