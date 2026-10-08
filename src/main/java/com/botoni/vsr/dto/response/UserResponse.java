package com.botoni.vsr.dto.response;

import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;

import java.util.UUID;

public record UserResponse(
        UUID id,
        Name name,
        Email email
) {
}
