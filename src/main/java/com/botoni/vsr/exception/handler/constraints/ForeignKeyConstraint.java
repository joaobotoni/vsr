package com.botoni.vsr.exception.handler.constraints;

import com.botoni.vsr.exception.lib.constraint.Constraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ForeignKeyConstraint implements Constraint {

    FK_IMOVEL_PROPRIETARIO_PESSOA("fk_imovel_proprietario_pessoa", "Não é possível excluir a pessoa, pois ela consta como proprietária de imóveis."),
    FK_VISTORIA_PESSOA_PESSOA("fk_vistoria_pessoa_pessoa", "Não é possível excluir a pessoa, pois ela está vinculada a vistorias."),
    FK_VISTORIA_IMOVEL("fk_vistoria_imovel", "Não é possível excluir o imóvel, pois ele possui vistorias registradas."),
    FK_VISTORIA_USUARIO("fk_vistoria_usuario", "Não é possível excluir o usuário, pois ele é responsável por vistorias."),
    FK_VISTORIA_PESSOA_JURIDICA("fk_vistoria_pessoa_juridica", "Não é possível excluir a empresa, pois ela possui vistorias registradas."),
    FK_IMOVEL_ENDERECO("fk_imovel_endereco", "Não é possível excluir o endereço, pois ele está vinculado a imóveis."),
    FK_IMOVEL_USUARIO("fk_imovel_usuario", "Não é possível excluir o usuário, pois ele é titular de imóveis."),
    FK_IMOVEL_PESSOA_JURIDICA("fk_imovel_pessoa_juridica", "Não é possível excluir a empresa, pois ela é titular de imóveis."),
    FK_CIDADE_ESTADO("fk_cidade_estado", "Não é possível excluir o estado, pois ele possui cidades cadastradas."),
    FK_BAIRRO_CIDADE("fk_bairro_cidade", "Não é possível excluir a cidade, pois ela possui bairros cadastrados."),
    FK_LOGRADOURO_BAIRRO("fk_logradouro_bairro", "Não é possível excluir o bairro, pois ele possui logradouros cadastrados."),
    FK_ENDERECO_LOGRADOURO("fk_endereco_logradouro", "Não é possível excluir o logradouro, pois ele possui endereços cadastrados."),
    FK_AMBIENTE_TIPO_AMBIENTE("fk_ambiente_tipo_ambiente", "Não é possível excluir o tipo de ambiente, pois ele está em uso."),
    FK_ITEM_TIPO_ITEM("fk_item_tipo_item", "Não é possível excluir o tipo de item, pois ele está em uso."),
    FK_VISTORIA_ENTRADA("fk_vistoria_entrada", "A vistoria de entrada deve referir-se ao mesmo imóvel da vistoria de saída.");

    private final String constraint;
    private final String message;

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
