package com.botoni.vsr.exception.enums.constraint;

import com.botoni.vsr.exception.lib.constraint.Constraint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum RuleConstraint implements Constraint {

    RN_SESSAO_ATIVA("rn_sessao_ativa", HttpStatus.UNAUTHORIZED, "A sessão foi encerrada ou expirou. Faça login novamente."),
    RN_REFRESH_TOKEN_RENOVADO("rn_refresh_token_renovado", HttpStatus.CONFLICT, "O refresh token já foi renovado por outra requisição."),
    RN_SENHA_ALTERADA("rn_senha_alterada", HttpStatus.CONFLICT, "A senha foi alterada por outra requisição. Tente novamente."),
    RN_ESTADO_EXISTENTE("rn_estado_existente", HttpStatus.UNPROCESSABLE_CONTENT, "O estado informado não existe."),
    RN_IMOVEL_VISTORIA_ANDAMENTO("rn_imovel_vistoria_andamento", HttpStatus.CONFLICT, "O imóvel possui uma vistoria em andamento e não pode ser removido."),
    RN_IMOVEL_ATIVO("rn_imovel_ativo", HttpStatus.UNPROCESSABLE_CONTENT, "O imóvel informado foi removido."),
    RN_VISTORIA_EM_ANDAMENTO("rn_vistoria_em_andamento", HttpStatus.CONFLICT, "A vistoria não está em andamento: foi finalizada ou removida."),
    RN_VISTORIA_ENTRADA("rn_vistoria_entrada", HttpStatus.UNPROCESSABLE_CONTENT, "A vistoria de entrada deve ser do tipo entrada e estar finalizada."),
    RN_AMBIENTE_ORIGEM("rn_ambiente_origem", HttpStatus.UNPROCESSABLE_CONTENT, "O ambiente de origem deve pertencer à vistoria de entrada."),
    RN_PESSOA_TITULAR("rn_pessoa_titular", HttpStatus.UNPROCESSABLE_CONTENT, "A pessoa informada não pertence ao seu cadastro."),
    RN_PESSOA_TITULAR_FIXO("rn_pessoa_titular_fixo", HttpStatus.UNPROCESSABLE_CONTENT, "O titular do cadastro da pessoa não pode ser alterado."),
    RN_CATALOGO_PERSONALIZADO("rn_catalogo_personalizado", HttpStatus.UNPROCESSABLE_CONTENT, "Só é possível criar tipos personalizados do próprio usuário."),
    RN_CATALOGO_ITEM("rn_catalogo_item", HttpStatus.UNPROCESSABLE_CONTENT, "O tipo de ambiente personalizado só recebe itens padrão ou do mesmo usuário."),
    RN_EMPRESA_ADMINISTRADOR("rn_empresa_administrador", HttpStatus.CONFLICT, "A empresa precisa de ao menos um sócio, representante ou MEI ativo."),
    RN_ITEM_ORIGEM("rn_item_origem", HttpStatus.UNPROCESSABLE_CONTENT, "O item de origem deve pertencer ao ambiente de origem.");

    private final String constraint;
    private final HttpStatus status;
    private final String message;
}
