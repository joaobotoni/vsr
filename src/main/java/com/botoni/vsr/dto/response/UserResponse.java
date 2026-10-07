package com.botoni.vsr.dto.response;

import com.botoni.vsr.vo.Email;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        Email email
) {
}
