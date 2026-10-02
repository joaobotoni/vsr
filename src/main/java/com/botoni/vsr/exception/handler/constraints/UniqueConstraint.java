package com.botoni.vsr.exception.handler.constraints;

import com.botoni.vsr.exception.lib.constraint.Constraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum UniqueConstraint implements Constraint {

    UQ_PESSOA_FISICA_CPF("uq_pessoa_fisica_cpf", "Já existe um cadastro com o CPF informado."),
    UQ_PESSOA_JURIDICA_CNPJ("uq_pessoa_juridica_cnpj", "Já existe um cadastro com o CNPJ informado."),
    UQ_USUARIO_EMAIL("uq_usuario_email", "Já existe um usuário cadastrado com o e-mail informado."),
    UQ_USUARIO_PESSOA("uq_usuario_pessoa", "Esta pessoa já possui um usuário cadastrado."),
    PK_CREDENCIAL_LOCAL("pk_credencial_local", "O usuário já possui uma credencial local cadastrada."),
    UQ_CREDENCIAL_SOCIAL_IDENTIDADE("uq_credencial_social_identidade", "Esta conta já está vinculada a outro usuário."),
    PK_CREDENCIAL_SOCIAL("pk_credencial_social", "O usuário já possui uma conta vinculada a este provedor."),
    PK_PESSOA_CONTATO("pk_pessoa_contato", "Esta pessoa já possui um contato cadastrado."),
    UQ_VINCULO("uq_vinculo", "Esta pessoa já possui vínculo com a empresa."),
    UQ_PESSOA_TITULARIDADE("uq_pessoa_titularidade", "Esta pessoa já consta no cadastro do titular."),
    UQ_ESTADO_SIGLA("uq_estado_sigla", "Já existe um estado com a sigla informada."),
    UQ_CIDADE_NOME("uq_cidade_nome", "Já existe uma cidade com este nome no estado informado."),
    UQ_BAIRRO_NOME("uq_bairro_nome", "Já existe um bairro com este nome na cidade informada."),
    UQ_LOGRADOURO("uq_logradouro", "Já existe um logradouro com este nome e CEP no bairro informado."),
    UQ_ENDERECO("uq_endereco", "Já existe um endereço com este número e complemento no logradouro informado."),
    UQ_IMOVEL_UUID("uq_imovel_uuid", "Já existe um imóvel com o identificador informado."),
    UQ_IMOVEL_ENDERECO_TITULAR("uq_imovel_endereco_titular", "Já existe um imóvel cadastrado neste endereço."),
    PK_IMOVEL_PROPRIETARIO("pk_imovel_proprietario", "Esta pessoa já consta como proprietária do imóvel."),
    UQ_TIPO_AMBIENTE_NOME("uq_tipo_ambiente_nome", "Já existe um tipo de ambiente com este nome."),
    UQ_TIPO_ITEM_NOME("uq_tipo_item_nome", "Já existe um tipo de item com este nome."),
    PK_TIPO_AMBIENTE_ITEM("pk_tipo_ambiente_item", "Este item já está associado ao tipo de ambiente."),
    UQ_VISTORIA_UUID("uq_vistoria_uuid", "Já existe uma vistoria com o identificador informado."),
    PK_VISTORIA_PESSOA("pk_vistoria_pessoa", "Esta pessoa já está vinculada à vistoria."),
    UQ_AMBIENTE_UUID("uq_ambiente_uuid", "Já existe um ambiente com o identificador informado."),
    UQ_AMBIENTE_NOME("uq_ambiente_nome", "Já existe um ambiente com este nome nesta vistoria."),
    UQ_ITEM_UUID("uq_item_uuid", "Já existe um item com o identificador informado."),
    UQ_ITEM_NOME("uq_item_nome", "Já existe um item com este nome neste ambiente."),
    UQ_EVIDENCIA_UUID("uq_evidencia_uuid", "Já existe uma evidência com o identificador informado."),
    UQ_EVIDENCIA_CAMINHO_ARQUIVO("uq_evidencia_caminho_arquivo", "Este arquivo já foi enviado anteriormente.");

    private final String constraint;
    private final String message;

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }
}
