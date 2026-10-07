package com.botoni.vsr.support;

import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.security.Principal;
import com.botoni.vsr.vo.Email;

public final class Users {

    public static final String EMAIL = "ana@vsr.com";

    private Users() {
    }

    public static User ana() {
        return User.builder().id(1).email(Email.of(EMAIL)).build();
    }

    public static Principal principal() {
        return Principal.from(ana());
    }
}
