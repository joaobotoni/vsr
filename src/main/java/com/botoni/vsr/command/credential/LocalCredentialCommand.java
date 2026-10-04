package com.botoni.vsr.command.credential;

import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.vo.Password;

public record LocalCredentialCommand(User user, Password password) {
}
