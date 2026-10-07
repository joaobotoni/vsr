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

insert into catalogo.tipo_item (nome, padrao)
values ('Piso', true),
       ('Rodapé', true),
       ('Paredes', true),
       ('Pintura', true),
       ('Teto', true),
       ('Portas', true),
       ('Fechaduras', true),
       ('Janelas', true),
       ('Vidros', true),
       ('Tomadas', true),
       ('Interruptores', true),
       ('Iluminação', true),
       ('Revestimento', true),
       ('Armários', true),
       ('Bancada', true),
       ('Pia', true),
       ('Torneira', true),
       ('Ralo', true),
       ('Box', true),
       ('Vaso sanitário', true),
       ('Chuveiro', true),
       ('Espelho', true),
       ('Registro', true),
       ('Tanque', true);

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
