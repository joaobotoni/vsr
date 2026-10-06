package com.botoni.vsr.exception.enums.constraint;

import com.botoni.vsr.exception.lib.constraint.Constraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum ForeignKeyConstraint implements Constraint {

    FK_PESSOA_FISICA_PESSOA("fk_pessoa_fisica_pessoa", "A pessoa informada não existe ou não é do tipo pessoa física."),
    FK_PESSOA_JURIDICA_PESSOA("fk_pessoa_juridica_pessoa", "A pessoa informada não existe ou não é do tipo pessoa jurídica."),
    FK_USUARIO_PESSOA("fk_usuario_pessoa", "A pessoa física informada para o usuário não existe."),
    FK_CREDENCIAL_LOCAL_USUARIO("fk_credencial_local_usuario", "O usuário informado para a credencial local não existe."),
    FK_CREDENCIAL_SOCIAL_USUARIO("fk_credencial_social_usuario", "O usuário informado para a credencial social não existe."),
    FK_DISPOSITIVO_USUARIO("fk_dispositivo_usuario", "O usuário informado para o dispositivo não existe."),
    FK_SESSAO_DISPOSITIVO("fk_sessao_dispositivo", "O dispositivo informado para a sessão não existe."),
    FK_REFRESH_TOKEN_SESSAO("fk_refresh_token_sessao", "A sessão informada para o refresh token não existe."),
    FK_VINCULO_PESSOA_FISICA("fk_vinculo_pessoa_fisica", "A pessoa física informada para o vínculo não existe."),
    FK_VINCULO_PESSOA_JURIDICA("fk_vinculo_pessoa_juridica", "A empresa informada para o vínculo não existe."),
    FK_PESSOA_CONTATO_PESSOA("fk_pessoa_contato_pessoa", "A pessoa informada para o contato não existe."),
    FK_PESSOA_TITULARIDADE_PESSOA("fk_pessoa_titularidade_pessoa", "A pessoa informada para a titularidade não existe."),
    FK_PESSOA_TITULARIDADE_USUARIO("fk_pessoa_titularidade_usuario", "O usuário titular informado não existe."),
    FK_PESSOA_TITULARIDADE_PESSOA_JURIDICA("fk_pessoa_titularidade_pessoa_juridica", "A empresa titular informada não existe."),
    FK_CIDADE_ESTADO("fk_cidade_estado", "O estado não existe ou não pode ser excluído, pois possui cidades cadastradas."),
    FK_BAIRRO_CIDADE("fk_bairro_cidade", "A cidade não existe ou não pode ser excluída, pois possui bairros cadastrados."),
    FK_LOGRADOURO_BAIRRO("fk_logradouro_bairro", "O bairro não existe ou não pode ser excluído, pois possui logradouros cadastrados."),
    FK_ENDERECO_LOGRADOURO("fk_endereco_logradouro", "O logradouro não existe ou não pode ser excluído, pois possui endereços cadastrados."),
    FK_IMOVEL_ENDERECO("fk_imovel_endereco", "O endereço não existe ou não pode ser excluído, pois está vinculado a imóveis."),
    FK_IMOVEL_USUARIO("fk_imovel_usuario", "O usuário não existe ou não pode ser excluído, pois é titular de imóveis."),
    FK_IMOVEL_PESSOA_JURIDICA("fk_imovel_pessoa_juridica", "A empresa não existe ou não pode ser excluída, pois é titular de imóveis."),
    FK_IMOVEL_PROPRIETARIO_IMOVEL("fk_imovel_proprietario_imovel", "O imóvel informado para o proprietário não existe."),
    FK_IMOVEL_PROPRIETARIO_PESSOA("fk_imovel_proprietario_pessoa", "A pessoa não existe ou não pode ser excluída, pois consta como proprietária de imóveis."),
    FK_TIPO_AMBIENTE_PESSOA_JURIDICA("fk_tipo_ambiente_pessoa_juridica", "A empresa titular do tipo de ambiente não existe."),
    FK_TIPO_AMBIENTE_USUARIO("fk_tipo_ambiente_usuario", "O usuário titular do tipo de ambiente não existe."),
    FK_TIPO_ITEM_PESSOA_JURIDICA("fk_tipo_item_pessoa_juridica", "A empresa titular do tipo de item não existe."),
    FK_TIPO_ITEM_USUARIO("fk_tipo_item_usuario", "O usuário titular do tipo de item não existe."),
    FK_TIPO_AMBIENTE_ITEM_TIPO_AMBIENTE("fk_tipo_ambiente_item_tipo_ambiente", "O tipo de ambiente informado não existe."),
    FK_TIPO_AMBIENTE_ITEM_TIPO_ITEM("fk_tipo_ambiente_item_tipo_item", "O tipo de item informado não existe."),
    FK_VISTORIA_USUARIO("fk_vistoria_usuario", "O usuário não existe ou não pode ser excluído, pois é responsável por vistorias."),
    FK_VISTORIA_PESSOA_JURIDICA("fk_vistoria_pessoa_juridica", "A empresa não existe ou não pode ser excluída, pois possui vistorias registradas."),
    FK_VISTORIA_IMOVEL("fk_vistoria_imovel", "O imóvel não existe ou não pode ser excluído, pois possui vistorias registradas."),
    FK_VISTORIA_ENTRADA("fk_vistoria_entrada", "A vistoria de entrada não existe, pertence a outro imóvel ou possui uma vistoria de saída vinculada."),
    FK_VISTORIA_PESSOA_VISTORIA("fk_vistoria_pessoa_vistoria", "A vistoria informada para a pessoa vinculada não existe."),
    FK_VISTORIA_PESSOA_PESSOA("fk_vistoria_pessoa_pessoa", "A pessoa não existe ou não pode ser excluída, pois está vinculada a vistorias."),
    FK_AMBIENTE_VISTORIA("fk_ambiente_vistoria", "A vistoria informada para o ambiente não existe."),
    FK_AMBIENTE_TIPO_AMBIENTE("fk_ambiente_tipo_ambiente", "O tipo de ambiente não existe ou não pode ser excluído, pois está em uso."),
    FK_AMBIENTE_ORIGEM("fk_ambiente_origem", "O ambiente de origem informado não existe."),
    FK_ITEM_AMBIENTE("fk_item_ambiente", "O ambiente informado para o item não existe."),
    FK_ITEM_TIPO_ITEM("fk_item_tipo_item", "O tipo de item não existe ou não pode ser excluído, pois está em uso."),
    FK_ITEM_ORIGEM("fk_item_origem", "O item de origem informado não existe."),
    FK_EVIDENCIA_AMBIENTE("fk_evidencia_ambiente", "O ambiente informado para a evidência não existe."),
    FK_EVIDENCIA_ITEM("fk_evidencia_item", "O item informado não pertence ao ambiente da evidência.");

    private final String constraint;
    private final String message;

    @Override
    public HttpStatus status() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }
}
