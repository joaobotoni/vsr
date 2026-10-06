package com.botoni.vsr.exception.enums.constraint;

import com.botoni.vsr.exception.lib.constraint.Constraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CheckConstraint implements Constraint {

    CK_PESSOA_NOME("ck_pessoa_nome", text("O nome", 200)),
    CK_PESSOA_FISICA_TIPO("ck_pessoa_fisica_tipo", "O tipo da pessoa é incompatível com o cadastro de pessoa física."),
    CK_PESSOA_FISICA_CPF("ck_pessoa_fisica_cpf", "O CPF informado é inválido."),
    CK_PESSOA_JURIDICA_TIPO("ck_pessoa_juridica_tipo", "O tipo da pessoa é incompatível com o cadastro de pessoa jurídica."),
    CK_PESSOA_JURIDICA_CNPJ("ck_pessoa_juridica_cnpj", "O CNPJ informado é inválido."),
    CK_PESSOA_JURIDICA_FANTASIA("ck_pessoa_juridica_fantasia", text("O nome fantasia", 200)),
    CK_USUARIO_EMAIL("ck_usuario_email", "O endereço de e-mail informado é inválido."),
    CK_CREDENCIAL_LOCAL_HASH("ck_credencial_local_hash", "O formato do hash da senha não é suportado."),
    CK_DISPOSITIVO_FABRICANTE("ck_dispositivo_fabricante", text("O fabricante do dispositivo", 64)),
    CK_DISPOSITIVO_MODELO("ck_dispositivo_modelo", text("O modelo do dispositivo", 64)),
    CK_DISPOSITIVO_VERSAO_SO("ck_dispositivo_versao_so", text("A versão do sistema operacional", 16)),
    CK_SESSAO_EXPIRA_EM("ck_sessao_expira_em", "A data de expiração da sessão deve ser posterior à data de criação."),
    CK_SESSAO_REVOGADA_EM("ck_sessao_revogada_em", "A data de revogação da sessão não pode ser anterior à data de criação."),
    CK_SESSAO_ULTIMO_ACESSO_EM("ck_sessao_ultimo_acesso_em", "A data do último acesso da sessão não pode ser anterior à data de criação."),
    CK_REFRESH_TOKEN_HASH_ATUAL("ck_refresh_token_hash_atual", "O hash do refresh token deve conter exatamente 32 bytes."),
    CK_REFRESH_TOKEN_HASH_ANTERIOR("ck_refresh_token_hash_anterior", "O hash anterior do refresh token deve conter exatamente 32 bytes."),
    CK_REFRESH_TOKEN_HASHES_DISTINTOS("ck_refresh_token_hashes_distintos", "O novo refresh token deve ser diferente do anterior."),
    CK_REFRESH_TOKEN_EXPIRA_EM("ck_refresh_token_expira_em", "A data de expiração do refresh token deve ser posterior à data de criação."),
    CK_REFRESH_TOKEN_RENOVADO_EM("ck_refresh_token_renovado_em", "A data de renovação do refresh token não pode ser anterior à data de criação."),
    CK_REFRESH_TOKEN_RENOVACAO("ck_refresh_token_renovacao", "O hash anterior e a data de renovação do refresh token devem ser informados em conjunto."),
    CK_PESSOA_CONTATO_TELEFONE("ck_pessoa_contato_telefone", "O número de telefone informado é inválido."),
    CK_PESSOA_CONTATO_EMAIL("ck_pessoa_contato_email", "O e-mail de contato informado é inválido."),
    CK_PESSOA_TITULARIDADE_TITULAR("ck_pessoa_titularidade_titular", "A titularidade deve ser atribuída a um único titular: usuário ou empresa."),
    CK_ESTADO_SIGLA("ck_estado_sigla", "A sigla do estado informada é inválida."),
    CK_ESTADO_NOME("ck_estado_nome", text("O nome do estado", 50)),
    CK_CIDADE_NOME("ck_cidade_nome", text("O nome da cidade", 100)),
    CK_BAIRRO_NOME("ck_bairro_nome", text("O nome do bairro", 100)),
    CK_LOGRADOURO_CEP("ck_logradouro_cep", "O CEP informado é inválido."),
    CK_LOGRADOURO_NOME("ck_logradouro_nome", text("O nome do logradouro", 200)),
    CK_ENDERECO_NUMERO("ck_endereco_numero", text("O número do endereço", 20)),
    CK_ENDERECO_COMPLEMENTO("ck_endereco_complemento", text("O complemento do endereço", 100)),
    CK_IMOVEL_TITULAR("ck_imovel_titular", "O imóvel deve estar vinculado a um único titular: usuário ou empresa."),
    CK_IMOVEL_DESCRICAO("ck_imovel_descricao", text("A descrição do imóvel", 200)),
    CK_TIPO_AMBIENTE_TITULAR("ck_tipo_ambiente_titular", "O tipo de ambiente pode estar vinculado a, no máximo, um titular: usuário ou empresa."),
    CK_TIPO_AMBIENTE_NOME("ck_tipo_ambiente_nome", text("O nome do tipo de ambiente", 100)),
    CK_TIPO_ITEM_TITULAR("ck_tipo_item_titular", "O tipo de item pode estar vinculado a, no máximo, um titular: usuário ou empresa."),
    CK_TIPO_ITEM_NOME("ck_tipo_item_nome", text("O nome do tipo de item", 100)),
    CK_VISTORIA_OBSERVACOES("ck_vistoria_observacoes", text("As observações iniciais da vistoria", 2000)),
    CK_VISTORIA_ENTRADA("ck_vistoria_entrada", "Apenas vistorias de saída podem estar vinculadas a uma vistoria de entrada."),
    CK_VISTORIA_FINALIZADA("ck_vistoria_finalizada", "O status da vistoria é incompatível com a data de finalização informada."),
    CK_VISTORIA_DATAS("ck_vistoria_datas", "A data de finalização não pode ser anterior à data de início da vistoria."),
    CK_AMBIENTE_NOME("ck_ambiente_nome", text("O nome do ambiente", 100)),
    CK_AMBIENTE_OBSERVACOES("ck_ambiente_observacoes", text("As observações do ambiente", 2000)),
    CK_AMBIENTE_ORIGEM("ck_ambiente_origem", "Um ambiente não pode ser definido como sua própria origem."),
    CK_ITEM_NOME("ck_item_nome", text("O nome do item", 100)),
    CK_ITEM_DESCRICAO("ck_item_descricao", text("A descrição do item", 500)),
    CK_ITEM_MATERIAL("ck_item_material", text("O material do item", 100)),
    CK_ITEM_OBSERVACOES("ck_item_observacoes", text("As observações do item", 2000)),
    CK_ITEM_ORIGEM("ck_item_origem", "Um item não pode ser definido como sua própria origem."),
    CK_EVIDENCIA_CAMINHO_ARQUIVO("ck_evidencia_caminho_arquivo", text("O caminho do arquivo", 500)),
    CK_EVIDENCIA_TIPO_ARQUIVO("ck_evidencia_tipo_arquivo", "Formato de arquivo não suportado. São aceitas apenas imagens JPEG ou PNG."),
    CK_EVIDENCIA_TAMANHO("ck_evidencia_tamanho", "O arquivo excede o tamanho máximo permitido de 50 MB."),
    CK_EVIDENCIA_DESCRICAO("ck_evidencia_descricao", text("A descrição da evidência", 500));

    private final String constraint;
    private final String message;

    @Override
    public HttpStatus status() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }

    private static String text(String field, int max) {
        return String.format("%s deve conter entre %d e %d caracteres, sem espaços nas extremidades.", field, 1, max);
    }
}
