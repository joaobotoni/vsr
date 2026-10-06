package com.botoni.vsr.dto.response;

import com.botoni.vsr.vo.Email;

public record UserResponse(
        Integer id,
        String name,
        Email email
) {
}
