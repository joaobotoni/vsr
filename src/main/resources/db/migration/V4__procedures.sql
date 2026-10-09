create procedure rotinas.violar(
    p_regra text
)
    language plpgsql
as
$$
begin
    raise exception 'violates check constraint "%"', p_regra
        using errcode = 'check_violation', constraint = p_regra;
end;
$$;

create procedure usuarios.registrar_dispositivo(
    p_id_usuario int,
    p_identificador uuid,
    p_plataforma text,
    p_fabricante text,
    p_modelo text,
    p_versao_so text
)
    language plpgsql
as
$$
declare
    v_plataforma usuarios.plataforma_dispositivo := p_plataforma;
begin
    insert into usuarios.dispositivo (
        id_usuario,
        identificador,
        plataforma,
        fabricante,
        modelo,
        versao_so
    )
    values (
        p_id_usuario,
        p_identificador,
        v_plataforma,
        p_fabricante,
        p_modelo,
        p_versao_so
    )
    on conflict (id_usuario, identificador) do nothing;

    update usuarios.dispositivo
    set (
        plataforma,
        fabricante,
        modelo,
        versao_so
    ) = (
        v_plataforma,
        p_fabricante,
        p_modelo,
        p_versao_so
    )
    where id_usuario = p_id_usuario
      and identificador = p_identificador
      and (
          plataforma,
          fabricante,
          modelo,
          versao_so
      ) is distinct from (
          v_plataforma,
          p_fabricante,
          p_modelo,
          p_versao_so
      );
end;
$$;

create procedure usuarios.registrar_acesso_sessao(
    p_id_sessao int
)
    language plpgsql
as
$$
begin
    perform 1
    from usuarios.sessao_ativa
    where id_sessao = p_id_sessao;

    if not found then
        call rotinas.violar('rn_sessao_ativa');
    end if;

    update usuarios.sessao
    set ultimo_acesso_em = now()
    where id_sessao = p_id_sessao
      and ultimo_acesso_em < now() - interval '5 minutes';
end;
$$;

create procedure usuarios.revogar_sessao(
    p_id_sessao int
)
    language plpgsql
as
$$
begin
    update usuarios.sessao
    set revogada_em = now()
    where id_sessao = p_id_sessao
      and revogada_em is null
      and expira_em > now();
end;
$$;

create procedure usuarios.revogar_sessoes_dispositivo(
    p_id_dispositivo int
)
    language plpgsql
as
$$
begin
    update usuarios.sessao
    set revogada_em = now()
    where id_dispositivo = p_id_dispositivo
      and revogada_em is null
      and expira_em > now();
end;
$$;

create procedure usuarios.revogar_sessoes_usuario(
    p_id_usuario int,
    p_id_sessao_mantida int
)
    language plpgsql
as
$$
begin
    update usuarios.sessao s
    set revogada_em = now()
    from usuarios.dispositivo d
    where d.id_dispositivo = s.id_dispositivo
      and d.id_usuario = p_id_usuario
      and s.id_sessao is distinct from p_id_sessao_mantida
      and s.revogada_em is null
      and s.expira_em > now();
end;
$$;

create procedure usuarios.emitir_refresh_token(
    p_id_sessao int,
    p_hash bytea
)
    language plpgsql
as
$$
begin
    perform 1
    from usuarios.sessao_ativa
    where id_sessao = p_id_sessao;

    if not found then
        call rotinas.violar('rn_sessao_ativa');
    end if;

    insert into usuarios.refresh_token (
        id_sessao,
        hash_atual,
        expira_em
    )
    select
        id_sessao,
        p_hash,
        expira_em
    from usuarios.sessao
    where id_sessao = p_id_sessao;
end;
$$;

create procedure usuarios.renovar_refresh_token(
    p_id_sessao int,
    p_hash_atual bytea,
    p_hash_novo bytea
)
    language plpgsql
as
$$
begin
    perform 1
    from usuarios.refresh_token r
    join usuarios.sessao_ativa s on s.id_sessao = r.id_sessao
    where r.id_sessao = p_id_sessao
      and r.expira_em > now();

    if not found then
        call rotinas.violar('rn_sessao_ativa');
    end if;

    update usuarios.refresh_token
    set (
        hash_atual,
        renovado_em
    ) = (
        p_hash_novo,
        now()
    )
    where id_sessao = p_id_sessao
      and hash_atual = p_hash_atual;

    if not found then
        call rotinas.violar('rn_refresh_token_renovado');
    end if;

    insert into usuarios.refresh_token_usado (
        hash,
        id_sessao
    )
    values (
        p_hash_atual,
        p_id_sessao
    );
end;
$$;

create procedure usuarios.trocar_senha(
    p_id_usuario int,
    p_hash_atual text,
    p_hash_novo text
)
    language plpgsql
as
$$
begin
    update usuarios.credencial_local
    set (
        senha_hash,
        senha_atualizada_em
    ) = (
        p_hash_novo,
        now()
    )
    where id_usuario = p_id_usuario
      and senha_hash = p_hash_atual;

    if not found then
        call rotinas.violar('rn_senha_alterada');
    end if;
end;
$$;

create procedure rotinas.limpar_sessoes_expiradas()
    language plpgsql
as
$$
begin
    delete from usuarios.sessao
    where expira_em <= now();
end;
$$;
