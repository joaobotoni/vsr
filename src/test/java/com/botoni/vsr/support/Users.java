package com.botoni.vsr.support;

import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.vo.Email;

public final class Users {

    public static final String EMAIL = "ana@vsr.com";
    public static final java.util.UUID UUID = java.util.UUID.fromString("3f1c9a52-7d4e-4b8a-9c21-5e6f7a8b9c0d");

    private Users() {
    }

    public static User ana() {
        return User.builder().id(1).uuid(UUID).email(Email.of(EMAIL)).build();
    }

    public static Principal principal() {
        return Principal.from(LocalCredential.builder().id(1).user(ana()).build());
    }
}
