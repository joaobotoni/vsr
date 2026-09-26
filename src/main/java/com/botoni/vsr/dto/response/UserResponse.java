package com.botoni.vsr.dto.response;

import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;

public record UserResponse(Integer id, String name, Cpf cpf, Email email) {
}