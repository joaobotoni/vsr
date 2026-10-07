create function public.tg_set_updated_at()
    returns trigger
    language plpgsql
as
$$
begin
    new.updated_at := now();
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
    v_ciclo int := p_peso_maximo - 1;
    v_soma  int := 0;
    v_peso  int;
    v_valor int;
    v_resto int;
    i       int;
begin
    for i in 1..v_tam
        loop
            v_peso := ((v_tam - i) % v_ciclo) + 2;
            v_valor := ascii(substr(p_base, i, 1)) - 48;
            v_soma := v_soma + v_valor * v_peso;
        end loop;

    v_resto := v_soma % 11;

    if v_resto < 2 then
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
    if p_cpf is null or p_cpf !~ '^[0-9]{11}$' then
        return false;
    end if;

    if p_cpf = repeat(left(p_cpf, 1), 11) then
        return false;
    end if;

    if documento.dv_modulo11(substr(p_cpf, 1, 9), 11) <> ascii(substr(p_cpf, 10, 1)) - 48 then
        return false;
    end if;

    if documento.dv_modulo11(substr(p_cpf, 1, 10), 11) <> ascii(substr(p_cpf, 11, 1)) - 48 then
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
    if p_cnpj is null or p_cnpj !~ '^[0-9A-Z]{12}[0-9]{2}$' then
        return false;
    end if;

    if p_cnpj = repeat(left(p_cnpj, 1), 14) then
        return false;
    end if;

    if documento.dv_modulo11(substr(p_cnpj, 1, 12), 9) <> ascii(substr(p_cnpj, 13, 1)) - 48 then
        return false;
    end if;

    if documento.dv_modulo11(substr(p_cnpj, 1, 13), 9) <> ascii(substr(p_cnpj, 14, 1)) - 48 then
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

create function documento.email_valido(p_email text)
    returns boolean
    language sql immutable parallel safe
as
$$
select p_email is not null
           and p_email = lower(btrim(p_email))
           and length(p_email) between 6 and 254
           and p_email ~ '^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$';
$$;
