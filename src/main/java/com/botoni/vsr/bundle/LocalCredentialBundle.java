package com.botoni.vsr.bundle;

import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.shared.vo.Password;

public record LocalCredentialBundle(User user, Password password) {
}
